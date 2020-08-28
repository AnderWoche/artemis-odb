package com.artemis;

import org.junit.Assert;
import org.junit.Test;

public class InjectionTest {

    public static class anObject {
        public ComponentMapper<WorldTest.AnComponent> mapper;
    }

    @Test
    public void multiWorld_inject_test() {
        anObject anObject = new anObject();

        MultiWorld multiWorld = new MultiWorld();

        multiWorld.addAutoObjectInject(anObject);

        WorldConfigurationBuilder builder = new WorldConfigurationBuilder();

        World w1 = new World(builder.setMultiWorld(multiWorld).build());
        w1.createEntity().edit().create(WorldTest.AnComponent.class);

        World w2 = new World(builder.setMultiWorld(multiWorld).build());

        w1.process();
        Assert.assertEquals(w1, multiWorld.getCurrentWorld());

        Assert.assertNotNull(anObject.mapper);

        boolean has = anObject.mapper.has(0);
        Assert.assertTrue(has);

        w2.process();
        Assert.assertEquals(w2, multiWorld.getCurrentWorld());


        Assert.assertNotNull(anObject.mapper);
        has = anObject.mapper.has(0);
        Assert.assertFalse(has);

    }

}
