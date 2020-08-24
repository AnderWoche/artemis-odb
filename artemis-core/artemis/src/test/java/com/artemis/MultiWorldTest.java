package com.artemis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.artemis.annotations.All;
import com.artemis.annotations.SkipWire;
import com.artemis.systems.IteratingSystem;
import org.junit.Test;

import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.VoidEntitySystem;

public class MultiWorldTest {
    @All(WorldTest.AnComponent.class)
    public static class MultiSystem extends IteratingSystem {

        private ComponentMapper<WorldTest.AnComponent> mapper;

        @Override
        protected void process(int entityId) {
            WorldTest.AnComponent anComponent = mapper.get(entityId);
            anComponent.name = super.world.toString();
        }
    }

    @Test
    public void test_multiWorld_class() {
        MultiWorldConfiguration multiWorldConfiguration = new MultiWorldConfiguration();
        multiWorldConfiguration.with(new MultiSystem());

        MultiWorld multiWorld = new MultiWorld(multiWorldConfiguration);


        World w1 = new World(new WorldConfigurationBuilder().setMultiWorld(multiWorld).build());
        w1.createEntity().edit().create(WorldTest.AnComponent.class);
        w1.process();

        World w2 = new World(new WorldConfigurationBuilder().setMultiWorld(multiWorld).build());
        w2.createEntity().edit().create(WorldTest.AnComponent.class);
        w2.process();

        // process World 1
        w1.requestMultiWorldFocus();

        multiWorld.process();

        WorldTest.AnComponent anComponent = w1.getMapper(WorldTest.AnComponent.class).get(0);
        assertEquals(w1.toString(), anComponent.name);

        // Process World 2
        w2.requestMultiWorldFocus();

        multiWorld.process();

        anComponent = w2.getMapper(WorldTest.AnComponent.class).get(0);
        assertEquals(w2.toString(), anComponent.name);

    }


    private World world;

    @Test
    public void uniqie_component_ids_per_world() {
        World innerWorld = new World(new WorldConfiguration()
                .setSystem(new EmptySystem()));

        world = new World(new WorldConfiguration()
                .setSystem(new InnerWorldProcessingSystem(innerWorld)));

        world.createEntity().edit().create(ComponentX.class);
        innerWorld.createEntity().edit().create(ComponentY.class);
        innerWorld.createEntity().edit().create(ComponentX.class);

        world.process();

        ComponentType xOuterType = world.getComponentManager().typeFactory.getTypeFor(ComponentX.class);
        ComponentType xInnerType = innerWorld.getComponentManager().typeFactory.getTypeFor(ComponentX.class);
        int xIndexOuter = xOuterType.getIndex();
        int xIndexInner = xInnerType.getIndex();
        int yIndexInner =
                innerWorld.getComponentManager().typeFactory.getTypeFor(ComponentY.class).getIndex();

        assertEquals(xOuterType, world.getComponentManager().typeFactory.getTypeFor(xOuterType.getIndex()));
        assertEquals(xInnerType, innerWorld.getComponentManager().typeFactory.getTypeFor(xInnerType.getIndex()));
        assertNotEquals(xIndexOuter, xIndexInner);
        assertEquals(xIndexOuter, yIndexInner);
    }

    public static class EmptySystem
            extends VoidEntitySystem {

        @Override
        protected void processSystem() {
        }

    }

    public static class InnerWorldProcessingSystem
            extends VoidEntitySystem {

        @SkipWire
        private final World inner;

        public InnerWorldProcessingSystem(World inner) {
            super();
            this.inner = inner;
        }

        @Override
        protected void processSystem() {
            inner.delta = world.delta;
            inner.process();
        }

    }

    private static class VoidSystem extends VoidEntitySystem {
        @Override
        protected void processSystem() {
        }
    }
}
