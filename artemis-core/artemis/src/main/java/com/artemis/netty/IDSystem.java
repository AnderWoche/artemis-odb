package com.artemis.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

class IDSystem {

    private Queue<Integer> freeClientIds;
    private AtomicInteger clientID = new AtomicInteger(0);
    private HashMap<Integer, ChannelHandlerContext> registeredChannels;
    private HashMap<ChannelHandlerContext, Integer> registeredChannelsReverted;

    public IDSystem() {
        this.freeClientIds = (Queue<Integer>) Collections.synchronizedList(new LinkedList<Integer>());

        HashMap<Integer, ChannelHandlerContext> map = new HashMap<Integer, ChannelHandlerContext>();
        this.registeredChannels = (HashMap<Integer, ChannelHandlerContext>) Collections.synchronizedMap(map);

        HashMap<ChannelHandlerContext, Integer> map2 = new HashMap<ChannelHandlerContext, Integer>();
        this.registeredChannelsReverted = (HashMap<ChannelHandlerContext, Integer>) Collections.synchronizedMap(map2);
    }

    private int obtainID() {
        if (!freeClientIds.isEmpty()) {
            return freeClientIds.poll();
        }
        return this.clientID.incrementAndGet();
    }

    public int registerChannel(ChannelHandlerContext ctx) {
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
