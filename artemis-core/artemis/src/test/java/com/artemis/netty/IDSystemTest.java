package com.artemis.netty;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.net.SocketAddress;

public class IDSystemTest {

    @Test
    public void idSystem_test() {
        IDSystem idSystem = new IDSystem();

        int id = idSystem.registerChannel(new ChannelHandlerContextImpl());

        Assert.assertEquals(1, id);

        id = idSystem.registerChannel(new ChannelHandlerContextImpl());

        Assert.assertEquals(2, id);


        idSystem.unregisterChannel(1);

        id = idSystem.registerChannel(new ChannelHandlerContextImpl());

        Assert.assertEquals(1, id);
    }

}
