package com.artemis.netty;

import com.artemis.MultiWorldConfiguration;
import com.artemis.NettyWorld;
import com.artemis.WorldConfigurationBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

public class NettyWorldTest {

    public static ChannelFuture createNettyWorldServer(final ServerNettyWorld world, int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst("decoder", new MassageReader());
                ch.pipeline().addFirst("encoder", new MassageWriter());

                ch.pipeline().addLast(world);
            }

            ;
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    world.setDelta(1F / 30F);
                    world.process();
                }
            }
        }).start();
        return bootstrap.bind(port).sync();
    }

    public static ChannelFuture createNettyWorldClient(final ClientNettyWorld world, String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100_000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst("decoder", new MassageReader());
                ch.pipeline().addFirst("encoder", new MassageWriter());

                ch.pipeline().addLast(world);
            }

            ;
        });
        return bootstrap.connect(host, port);
    }

    @Test
    public void nettyWorld_server_client_test() throws InterruptedException {

        ServerNettyWorld worldServer = new ServerNettyWorld(new WorldConfigurationBuilder().build());
        ChannelFuture channelFutureServer = createNettyWorldServer(worldServer, 8694);
        channelFutureServer.sync();

        for (int i = 0; i < 100; i++) {
            ClientNettyWorld worldClient = new ClientNettyWorld(new WorldConfigurationBuilder().build());
            ChannelFuture channelFutureClient = createNettyWorldClient(worldClient, "localhost", 8694);
            channelFutureClient.sync();
        }

//        while (true)
//            Thread.sleep(4000);
    }

}
