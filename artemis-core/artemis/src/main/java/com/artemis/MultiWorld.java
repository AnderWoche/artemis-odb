package com.artemis;

import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;

import java.util.HashMap;

public class MultiWorld {

    World currentWorld;

    final HashMap<Class<? extends BaseSystem>, BaseSystem> systemsMap = new HashMap<>();
    final Bag<BaseSystem> systems = new Bag<>(BaseSystem.class);

    final Bag<MultiEntitySubscription> multiEntitySubscriptions = new Bag<>(MultiEntitySubscription.class);

    final Bag<Object> autoInjectObjects = new Bag<>(Object.class);


    public MultiWorld() {
        this(new MultiWorldConfiguration());
    }

    public MultiWorld(MultiWorldConfiguration multiWorldConfiguration) {
        if (multiWorldConfiguration == null) multiWorldConfiguration = new MultiWorldConfiguration();
        this.initSystems(multiWorldConfiguration.systems);
    }

    protected void initSystems(Bag<BaseSystem> systems) {
        for (BaseSystem baseSystem : systems) {
            baseSystem.setMultiWorld(this);
            this.registerMultiBaseSystem(baseSystem);
        }
    }


    protected <T extends BaseSystem> void registerMultiBaseSystem(T baseSystem) {
        baseSystem.setMultiSystem();
        this.injectObject(baseSystem);
        this.systems.add(baseSystem);
        this.systemsMap.put(baseSystem.getClass(), baseSystem);
    }

    public synchronized void changeWorld(World world) {
        this.currentWorld = world;

        // Check for System overlap
        for (BaseSystem multiBaseSystem : world.getSystems()) {
            for (BaseSystem singleBaseSystem : this.systems) {
                if (multiBaseSystem.getClass().equals(singleBaseSystem.getClass())) {
                    throw new RuntimeException("you cant run a system on a MultiWorld and on a World. <" + multiBaseSystem.getClass() + ">");
                }
            }
        }

        // inject MultiWorld Systems
        for (BaseSystem baseSystem : this.systems) {
            baseSystem.setWorld(world);
            world.inject(baseSystem);
        }
        for (MultiEntitySubscription entitySubscription : this.multiEntitySubscriptions) {
            entitySubscription.changeWorld(world);
        }
        for (Object o : this.autoInjectObjects) {
            world.inject(o);
        }

        //init system if it is not initialized
        BaseSystem[] data = this.systems.getData();
        for (int i = 0, s = this.systems.size(); i < s; i++) {
            BaseSystem baseSystem = data[i];
            if (!baseSystem.isInitialized) {
                baseSystem.initialize();
                baseSystem.isInitialized = true;
            }
        }
    }

    void process() {
        if (this.currentWorld != null) {
            BaseSystem[] data = this.systems.getData();
            for (int i = 0, s = this.systems.size(); i < s; i++) {
                BaseSystem baseSystems = data[i];
                if (baseSystems.isEnabled()) {
                    baseSystems.process();
                }
            }
        }
    }

    public int getAmountOfEntityProcesses() {
        int iterationAmount = 0;
        for (BaseSystem baseSystem : this.systems) {
            if (baseSystem instanceof IteratingSystem) {
                IteratingSystem iteratingSystem = (IteratingSystem) baseSystem;
                iterationAmount += iteratingSystem.getSubscription().getEntities().size();
            }
        }
        return iterationAmount;
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

    /**
     * @param object the object that gets injected every world change.
     */
    public void addAutoObjectInject(Object object) {
        this.autoInjectObjects.add(object);
    }
}
