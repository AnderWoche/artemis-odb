package com.artemis;

import com.artemis.utils.Bag;
import com.artemis.utils.NettyByteBufUtil;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

import java.util.HashMap;

public class MultiWorld implements ChannelInboundHandler {

    private ChannelHandlerContext ctx;

    World currentWorld;

    final HashMap<Class<? extends BaseSystem>, BaseSystem> systemsMap = new HashMap<Class<? extends BaseSystem>, BaseSystem>();
    final Bag<BaseSystem> systems = new Bag<BaseSystem>(BaseSystem.class);
    final HashMap<String, BaseSystem> systemNameHashMap = new HashMap<String, BaseSystem>();

    final Bag<MultiEntitySubscription> multiEntitySubscriptions = new Bag<MultiEntitySubscription>(MultiEntitySubscription.class);

    final Bag<Object> autoInjectObjects = new Bag<Object>(Object.class);

//    private final Bag<Resizable> resizableSystemsArray = new Bag<>();


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
        this.systemNameHashMap.put(baseSystem.getSystemIdentifier(), baseSystem);
//        if (baseSystem instanceof Resizable) {
//            this.resizableSystemsArray.add((Resizable) baseSystem);
//        }
//        if (baseSystem instanceof InputProcessor) {
//            SpaceExplorer.addInputProcessor(3, (InputProcessor) baseSystem);
//        }
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

        // inject World systems
        for(BaseSystem baseSystem : world.getSystems()) {
            baseSystem.setChannelHandlerContext(this.ctx);
        }

        // inject MultiWorld Systems
        for (BaseSystem baseSystem : this.systems) {
            baseSystem.setWorld(world);
            baseSystem.setChannelHandlerContext(this.ctx);
            world.inject(baseSystem);
        }
        for (MultiEntitySubscription entitySubscription : this.multiEntitySubscriptions) {
            entitySubscription.changeWorld(world);
        }
        for (Object o : this.autoInjectObjects) {
            world.inject(o);
        }

        //init system if it is not initialized
        for (BaseSystem baseSystem : systems) {
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

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        for(BaseSystem baseSystem : this.getSystems()) {
            baseSystem.setChannelHandlerContext(this.ctx);
        }
        if(this.currentWorld != null) {
            for(BaseSystem baseSystem : this.currentWorld.getSystems()) {
                baseSystem.setChannelHandlerContext(this.ctx);
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        String systemName = NettyByteBufUtil.readUTF16String(byteBuf);
        BaseSystem networkedSystem = this.systemNameHashMap.get(systemName);

        networkedSystem.read(byteBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        ctx.fireChannelWritabilityChanged();
    }
}
