package com.artemis.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class NettyByteBufUtil {

	public static final Charset UTF16Charset = StandardCharsets.UTF_16;

	public static ByteBuf writeUTF16String(ByteBuf byteBuf, String s) {
		ByteBuf stringByteBuffer = ByteBufAllocator.DEFAULT.buffer();

		stringByteBuffer.writeCharSequence(s, UTF16Charset);

		byteBuf.writeInt(stringByteBuffer.readableBytes());
		byteBuf.writeBytes(stringByteBuffer);

		stringByteBuffer.release();

		return byteBuf;
	}

	public static String readUTF16String(ByteBuf byteBuf) {
		int lenght = byteBuf.readInt();
		return (String) byteBuf.readCharSequence(lenght, UTF16Charset);
	}

}
