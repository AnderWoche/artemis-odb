package com.artemis.netty;

import com.artemis.*;
import com.artemis.utils.NettyByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ClientNettyWorld extends NettyWorld {

    public ClientNettyWorld(WorldConfiguration configuration) {
        super(configuration);
        super.worldType = NettyWorldType.Client;
    }

    @Override
    public int create() {
        return super.create();
    }

    @Override
    public int create(Archetype archetype) {
        return super.create(archetype);
    }

    @Override
    public Entity createEntity() {
        return super.createEntity();
    }

    @Override
    public Entity createEntity(Archetype archetype) {
        return super.createEntity(archetype);
    }


    @Override
    public void delete(int entityId) {
        super.delete(entityId);
    }

    @Override
    public void deleteEntity(Entity e) {
        super.deleteEntity(e);
    }
}
