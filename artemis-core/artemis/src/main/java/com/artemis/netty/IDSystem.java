package com.artemis.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class IDSystem {

    private final BlockingQueue<Integer> freeClientIds;
    private final AtomicInteger clientID = new AtomicInteger(0);
    private final Map<Integer, ChannelHandlerContext> registeredChannels;
    private final Map<ChannelHandlerContext, Integer> registeredChannelsReverted;

    public IDSystem() {
        this.freeClientIds = new ArrayBlockingQueue<>(128);

        HashMap<Integer, ChannelHandlerContext> map = new HashMap<>();
        this.registeredChannels = Collections.synchronizedMap(map);

        HashMap<ChannelHandlerContext, Integer> map2 = new HashMap<>();
        this.registeredChannelsReverted = Collections.synchronizedMap(map2);
    }

    private int obtainID() {
        if (!freeClientIds.isEmpty()) {
            return freeClientIds.poll();
        }
        return this.clientID.incrementAndGet();
    }

    public int registerChannel(ChannelHandlerContext ctx) {
        assert ctx != null;
        int id = this.obtainID();
        this.registeredChannels.put(id, ctx);
        this.registeredChannelsReverted.put(ctx, id);
        return id;
    }

    public ChannelHandlerContext unregisterChannel(int id) {
        ChannelHandlerContext ctx = this.registeredChannels.remove(id);
        if(ctx != null) {
            this.registeredChannelsReverted.remove(ctx);
            this.freeClientIds.add(id);
            return ctx;
        }
        return null;
    }

    public Integer unregisterChannel(ChannelHandlerContext ctx) {
        Integer id = this.registeredChannelsReverted.remove(ctx);
        if(id != null) {
            this.registeredChannels.remove(id);
            this.freeClientIds.add(id);
            return id;
        }
        return null;
    }

    public ChannelHandlerContext getChannelChannel(int id) {
        return this.registeredChannels.get(id);
    }

}
