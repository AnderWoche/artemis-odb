package com.artemis.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ArtemisODBServer {

    private final ServerNettyWorld serverNettyWorld;

    private Thread serverThread;

    public ArtemisODBServer(final ServerNettyWorld serverNettyWorld, int port) {
        this.serverNettyWorld = serverNettyWorld;

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst("decoder", new MassageReader());
                ch.pipeline().addFirst("encoder", new MassageWriter());

                ch.pipeline().addLast(serverNettyWorld);
            }
        });

        this.createServerThread();

        try {
            bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createServerThread() {
        this.serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long processBegin = System.currentTimeMillis() - 1;
                long delta;
                while (true) {
                    delta = System.currentTimeMillis() - processBegin;
                    processBegin = System.currentTimeMillis();
                    try {
                        long wait = 1000 / 120;
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serverNettyWorld.setDelta(delta);
                    serverNettyWorld.process();
                }
            }
        });
        this.serverThread.start();
    }

    public ServerNettyWorld getServerNettyWorld() {
        return serverNettyWorld;
    }

    public Thread getServerThread() {
        return serverThread;
    }
}
