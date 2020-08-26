package com.artemis;

import com.artemis.utils.Bag;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;

import java.util.HashMap;

public class MultiWorld {

    private World currentWorld;

    private final HashMap<Class<? extends BaseSystem>, BaseSystem> systemsMap = new HashMap<>();
    private final Bag<BaseSystem> systems = new Bag<>(BaseSystem.class);

    private final Bag<MultiEntitySubscription> multiEntitySubscriptions = new Bag<>(MultiEntitySubscription.class);

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


    protected <T extends BaseSystem> void registerMultiBaseSystem(T staticBaseSystem) {
        this.injectObject(staticBaseSystem);
        this.systems.add(staticBaseSystem);
        this.systemsMap.put(staticBaseSystem.getClass(), staticBaseSystem);
//        if (staticBaseSystem instanceof Resizable) {
//            this.resizableSystemsArray.add((Resizable) staticBaseSystem);
//        }
//        if (staticBaseSystem instanceof InputProcessor) {
//            SpaceExplorer.addInputProcessor(3, (InputProcessor) staticBaseSystem);
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
    }

    public void process() {
        BaseSystem[] data = this.systems.getData();
        for (int i = 0, s = this.systems.size(); i < s; i++) {
            data[i].process();
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

}
