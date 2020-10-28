package com.artemis.utils;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.components.EntityNameComponent;

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

    private final String name;

    private final List<Class<? extends Component>> componentClassArray = new ArrayList<Class<? extends Component>>();
    private final List<ComponentInitializer<? extends Component>> componentInitializerArray = new ArrayList<ComponentInitializer<? extends Component>>();

    public EntityDescription(String name) {
        this.name = name;
    }

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

        World world = entityEdit.getEntity().getWorld();
        for (int i = 0; i < size; i++) {
            Class<? extends Component> c = this.componentClassArray.get(i);
            Component component = entityEdit.create(c);

            ComponentInitializer initializer = this.componentInitializerArray.get(i);
            if (initializer != null) {
                initializer.build(component);
            }
        }

        // init the EntityNameComponent to spawn a second entity from the name later.
        entityEdit.create(EntityNameComponent.class).name = this.name;
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

    public String getName() {
        return this.name;
    }
}

