package com.artemis.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;

public class MassageReader extends ReplayingDecoder<ByteBuf> {

	private final static int MAX_MASSSAGE_SIZE = 1000000; // this is 1MB

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		int nextMassageSize = in.readInt();

		if (nextMassageSize > MAX_MASSSAGE_SIZE) {
			throw new MaximumSizeExceeded("A Massage is Bigger than " + MAX_MASSSAGE_SIZE / 1000000
					+ "MB. Thats to mutch. Use File Transfer if this is a File");
		}

		out.add(in.readBytes(nextMassageSize));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof DecoderException) {
			cause.printStackTrace();
			ctx.close();
			return;
		}
		super.exceptionCaught(ctx, cause);
	}

	public static class MaximumSizeExceeded extends RuntimeException {
		private static final long serialVersionUID = -1833615422763957637L;

		public MaximumSizeExceeded(String s) {
			super(s);
		}

		public MaximumSizeExceeded() {
		}
	}

}
