package com.artemis.netty;

import com.artemis.NettyWorld;
import com.artemis.WorldConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerNettyWorld extends NettyWorld {

    protected final IDSystem idSystem = new IDSystem();

    public ServerNettyWorld(WorldConfiguration configuration) {
        super(configuration);
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.idSystem.registerChannel(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        this.idSystem.unregisterChannel(ctx);
    }

    /**
     * Override to implement
     *
     * @param byteBuf the massage
     */
    protected void read(ByteBuf byteBuf) {

    }

//    protected void write(ByteBuf byteBuf) {
//        // get Ctx and channel and other things from GLOBAL CALL
//
//        ByteBuf buffer = ctx.alloc().buffer();
//
//        // write System Name
//        NettyByteBufUtil.writeUTF16String(buffer, this.getSystemIdentifier());
//
//        // write Massage
//        buffer.writeBytes(byteBuf);
//
//        ctx.channel().writeAndFlush(buffer);
//    }

}
