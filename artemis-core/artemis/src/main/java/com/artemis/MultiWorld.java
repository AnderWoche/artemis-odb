package com.artemis;

import com.artemis.utils.Bag;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;

import java.util.HashMap;

public class MultiWorld {

    private float delta;

    World currentWorld;

    private final HashMap<Class<? extends BaseSystem>, BaseSystem> systemsMap = new HashMap<>();
    private final Bag<BaseSystem> systems = new Bag<>(BaseSystem.class);

    private final Bag<MultiEntitySubscription> multiEntitySubscriptions = new Bag<>(MultiEntitySubscription.class);

    private final Bag<Object> autoInjectObjects = new Bag<>(Object.class);

//    private final Bag<Resizable> resizableSystemsArray = new Bag<>();


    public MultiWorld() {
        this(new MultiWorldConfiguration());
    }

    public MultiWorld(MultiWorldConfiguration multiWorldConfiguration) {
        if(multiWorldConfiguration == null) multiWorldConfiguration = new MultiWorldConfiguration();
        this.initSystems(multiWorldConfiguration.systems);
    }

    protected void initSystems(Bag<BaseSystem> systems) {
        for (BaseSystem staticBaseSystem : systems) {
            staticBaseSystem.setMultiWorld(this);
            this.registerMultiBaseSystem(staticBaseSystem);
            staticBaseSystem.initialize();
        }
    }


    protected <T extends BaseSystem> void registerMultiBaseSystem(T baseSystem) {
        baseSystem.setMultiSystem();
        this.injectObject(baseSystem);
        this.systems.add(baseSystem);
        this.systemsMap.put(baseSystem.getClass(), baseSystem);
//        if (baseSystem instanceof Resizable) {
//            this.resizableSystemsArray.add((Resizable) baseSystem);
//        }
//        if (baseSystem instanceof InputProcessor) {
//            SpaceExplorer.addInputProcessor(3, (InputProcessor) baseSystem);
//        }
    }

    public synchronized void changeWorld(World world) {
        this.currentWorld = world;
        for (BaseSystem baseSystem : this.systems) {
            baseSystem.setWorld(world);
            world.inject(baseSystem);
        }
        for (MultiEntitySubscription entitySubscription : this.multiEntitySubscriptions) {
            entitySubscription.changeWorld(world);
        }
        for(Object o : this.autoInjectObjects) {
            world.inject(o);
        }
    }

    void process() {
        if(this.currentWorld != null) {
            BaseSystem[] data = this.systems.getData();
            for (int i = 0, s = this.systems.size(); i < s; i++) {
                BaseSystem baseSystems = data[i];
                if(baseSystems.isEnabled()) {
                    baseSystems.process();
                }
            }
        }
    }

    private void injectObject(Object object) {
        Class<?> currentClass = object.getClass();
        do {
            this.injectObjectWithClass(object, currentClass);

            currentClass = currentClass.getSuperclass();

        } while (currentClass != Object.class);
    }

    private void injectObjectWithClass(Object object, Class<?> searchClass) {
        Field[] fields = ClassReflection.getDeclaredFields(searchClass);
        for (Field f : fields) {
            if (f.getType() == MultiEntitySubscription.class) {
                try {
                    f.setAccessible(true);
                    MultiEntitySubscription subscription = (MultiEntitySubscription) f.get(object);
                    this.registerMultiEntitySubscription(subscription);
                } catch (NullPointerException e) {
                    System.out.println(object.getClass() + " mapper is null. MultiEntitySubscription can't be Null");
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerMultiEntitySubscription(MultiEntitySubscription staticEntitySubscription) {
        if (staticEntitySubscription == null) {
            throw new NullPointerException("StaticEntitySubscription is Null");
        }
        if (this.currentWorld != null) {
            staticEntitySubscription.changeWorld(this.currentWorld);
        }
        this.multiEntitySubscriptions.add(staticEntitySubscription);
    }


    public Bag<BaseSystem> getSystems() {
        return systems;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSystem> T getSystem(Class<T> staticBaseSystemClass) {
        return (T) this.systemsMap.get(staticBaseSystemClass);
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    /**
     * @param object the object that gets injected every world change.
     */
    public void addAutoObjectInject(Object object) {
        this.autoInjectObjects.add(object);
    }

}
