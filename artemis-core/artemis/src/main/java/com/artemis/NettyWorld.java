package com.artemis;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.netty.NettyWorldType;
import com.artemis.utils.NettyByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

@Sharable
public abstract class NettyWorld extends World implements ChannelInboundHandler {

    protected NettyWorldType worldType;

    public NettyWorld(WorldConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {

    }

    /**
     * Override to implement
     * @param  byteBuf the massage
     */
    protected void read(ChannelHandlerContext ctx, ByteBuf byteBuf) {

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

        if(systemName.equals(this.getNettyWorldName())) {
            this.read(ctx, byteBuf);
            return;
        }

        BaseSystem networkedSystem = super.systemNameHashMap.get(systemName);

        networkedSystem.read(ctx, byteBuf);
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

    public NettyWorldType getWorldType() {
        return worldType;
    }

    public String getNettyWorldName() {
        return "NettyWorld";
    }
}
