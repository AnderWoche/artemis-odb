package com.artemis.utils;

import com.artemis.Component;
import com.artemis.EntityEdit;
import com.artemis.World;

import java.util.HashMap;

/**
 *
 * The {@code EntityBuilder} is Good for Entitys that gets frecuently often
 * created. if you want create an Entity only ocne or a few times you can use
 * just a static method or a {@code CustomEntity}
 *
 * @author David Humann (Moldiy)
 */
public abstract class EntityCreator {

    public static interface ComponentInitializer<T extends Component> {
        void build(T component);
    }

    protected final World world;

    protected static HashMap<Class<? extends Component>, ComponentInitializer<? extends Component>> defaultComponents = new HashMap<Class<? extends Component>, ComponentInitializer<? extends Component>>();

    protected HashMap<Class<? extends Component>, ComponentInitializer<? extends Component>> components = new HashMap<Class<? extends Component>, ComponentInitializer<? extends Component>>();

    /**
     * Transform Component Added as default
     */
    public EntityCreator(World world) {
        this.world = world;
    }

    protected <T extends Component> void add(Class<T> component, ComponentInitializer<T> initializer) {
        this.components.put(component, initializer);
    }

    /**
     * do here some Operations there took LOONG time to process like load a Texture
     * or someThing. this method get Called from a loding Thread. During this Method
     * the entity is Null/Not created
     */
//	protected abstract void loadEntity();

    protected abstract void buildEntity(EntityEdit entityEdit);

}

