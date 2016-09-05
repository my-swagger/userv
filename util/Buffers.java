package com.userv.util;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * 
 * @author Bot
 * @author Graham
 * @author `Discardedx2
 */
public final class Buffers {

	private static final char[] CHARACTERS = { '\u20AC', '\0', '\u201A', '\u0192', '\u201E', '\u2026', '\u2020', '\u2021',
			'\u02C6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017D', '\0', '\0', '\u2018', '\u2019', '\u201C',
			'\u201D', '\u2022', '\u2013', '\u2014', '\u02DC', '\u2122', '\u0161', '\u203A', '\u0153', '\0', '\u017E',
			'\u0178'
	};

	public static String getJagexString(ByteBuffer buf) {
		StringBuilder bldr = new StringBuilder();
		int b;
		while ((b = buf.get()) != 0) {
			if (b >= 127 && b < 160) {
				char curChar = CHARACTERS[b - 128];
				if (curChar != 0) {
					bldr.append(curChar);
				}
			} else {
				bldr.append((char) b);
			}
		}
		return bldr.toString();
	}

	public static int getTriByte(ByteBuffer buf) {
		return ((buf.get() & 0xFF) << 16) | ((buf.get() & 0xFF) << 8) | (buf.get() & 0xFF);
	}

	public static void putTriByte(ByteBuffer buf, int value) {
		buf.put((byte) (value >> 16));
		buf.put((byte) (value >> 8));
		buf.put((byte) value);
	}

	public static int getCrcChecksum(ByteBuffer buffer) {
		Checksum crc = new CRC32();
		for (int i = 0; i < buffer.limit(); i++) {
			crc.update(buffer.get(i));
		}
		return (int) crc.getValue();
	}

	public static byte[] getWhirlpoolDigest(ByteBuffer buf) {
		byte[] bytes = new byte[buf.limit()];
		buf.get(bytes);
		return Whirlpool.whirlpool(bytes, 0, bytes.length);
	}

	public static String toString(ByteBuffer buffer) {
		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < buffer.limit(); i++) {
			String hex = Integer.toHexString(buffer.get(i) & 0xFF).toUpperCase();
			if (hex.length() == 1)
				hex = "0" + hex;

			builder.append("0x").append(hex);
			if (i != buffer.limit() - 1) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public static String readString(ByteBuffer buffer) {
		StringBuilder bldr = new StringBuilder();
		while (buffer.hasRemaining()) {
			char c = (char) (buffer.get() & 0xFF);
			if (c == 0) {
				break;
			}
			bldr.append(c);
		}
		return bldr.toString();
	}

	public static String readRS2String(ChannelBuffer in) {
		StringBuilder bldr = new StringBuilder();
		while (in.readable()) {
			char c = (char) in.readUnsignedByte();
			if (c == 0) {
				break;
			}
			bldr.append(c);
		}
		return bldr.toString();
	}

	public static void readFully(FileChannel channel, ByteBuffer buffer, long ptr) throws IOException {
		while (buffer.remaining() > 0) {
			long read = channel.read(buffer, ptr);
			if (read == -1) {
				throw new EOFException();
			} else {
				ptr += read;
			}
		}
	}

	public static ByteBuffer encryptRSA(ByteBuffer buffer, BigInteger modulus, BigInteger key) {
		byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);

		BigInteger in = new BigInteger(bytes);
		BigInteger out = in.modPow(key, modulus);

		return ByteBuffer.wrap(out.toByteArray());
	}
}