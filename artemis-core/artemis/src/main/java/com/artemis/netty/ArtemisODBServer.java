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
                long delta = System.currentTimeMillis();
                while (true) {
                    delta = System.currentTimeMillis() - delta;
                    try {
                        Thread.sleep(1/120);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    serverNettyWorld.setDelta(1F / 30F);
                    serverNettyWorld.process();
                    delta = System.currentTimeMillis();
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
