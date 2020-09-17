package com.artemis.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MassageWriter extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		int massageLenght = msg.readableBytes();
		
		out.writeInt(massageLenght);
		out.writeBytes(msg);
	}

}
