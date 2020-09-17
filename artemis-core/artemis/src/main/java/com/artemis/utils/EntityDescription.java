package com.artemis.utils;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.annotations.ClientOnly;
import com.artemis.NettyWorld;
import com.artemis.netty.NettyWorldType;
import com.artemis.utils.reflect.ClassReflection;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code EntityBuilder} is Good for Entitys that gets frecuently often
 * created. if you want create an Entity only ocne or a few times you can use
 * just a static method or a {@code CustomEntity}
 *
 * @author David Humann (Moldiy)
 */
public abstract class EntityDescription {

    public interface ComponentInitializer<T extends Component> {
        void build(T component);
    }

    private List<Class<? extends Component>> componentClassArray = new ArrayList<Class<? extends Component>>();
    private List<ComponentInitializer<? extends Component>> componentInitializerArray = new ArrayList<ComponentInitializer<? extends Component>>();

    protected <T extends Component> void add(Class<T> component, ComponentInitializer<T> initializer) {
        this.componentClassArray.add(component);
        this.componentInitializerArray.add(initializer);
    }

//    /**
//     * do here some Operations there took LONG time to process like load a Texture
//     * or someThing. this method get Called from a loading Thread. During this Method
//     * the entity is Null/Not created
//     */
//	protected abstract void loadEntity();

    protected void buildEntity(EntityEdit entityEdit) {
        int size = this.componentClassArray.size();

        // init Components

        // If World instanceof NettyWorld and the world is a server the ClientOnly Components get Ignored.
        World world = entityEdit.getEntity().getWorld();
        if (world instanceof NettyWorld) {
            NettyWorld nettyWorld = (NettyWorld) world;
            if (nettyWorld.getWorldType() == NettyWorldType.Server) {
                for (int i = 0; i < size; i++) {
                    Class<? extends Component> c = this.componentClassArray.get(i);
                    if (!ClassReflection.isAnnotationPresent(c, ClientOnly.class)) {
                        ComponentInitializer initializer = this.componentInitializerArray.get(i);
                        if (initializer != null) {
                            initializer.build(entityEdit.create(c));
                        } else {
                            entityEdit.create(c);
                        }
                    }
                }
                return;
            }
        }

        for (int i = 0; i < size; i++) {
            Class<? extends Component> c = this.componentClassArray.get(i);
            ComponentInitializer initializer = this.componentInitializerArray.get(i);
            if (initializer != null) {
                initializer.build(entityEdit.create(c));
            } else {
                entityEdit.create(c);
            }
        }


    }

    public int create(World world) {
        int entityId = world.create();
        this.buildEntity(world.edit(entityId));
        return entityId;
    }

    public Entity createEntity(World world) {
        Entity entity = world.createEntity();
        this.buildEntity(entity.edit());
        return entity;
    }

}

