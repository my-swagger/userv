package com.userv;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * 
 * @author Bot
 * 
 */
public class DefaultDecoder extends FrameDecoder {

	/**
	 * My UpdateServer instance
	 */
	private UpdateServer server;

	DefaultDecoder(UpdateServer server) {
		this.server = server;
	}

	@Override
	protected Object decode(ChannelHandlerContext arg0, Channel channel, ChannelBuffer in) throws Exception {
		Client client = server.getClient(channel);
		if (client == null || in.readableBytes() < 4) {
			return null;
		}
		Stage stage = client.getStage();
		int opcode = in.readUnsignedByte();
		if (stage == Stage.HANDSHAKE) {
			if (opcode != 15) {
				return null;
			} else {
				int version = in.readInt();
				ChannelBuffer out = ChannelBuffers.buffer(1);
				out.writeByte(version == server.getRevision() ? 0 : 6);
				if (version >= 562) {
					for (int key : server.getJS5Keys())
						out.writeInt(key);
				}
				channel.write(out);
				client.setStage(Stage.FILE_REQUESTS);
			}
		} else if (stage == Stage.FILE_REQUESTS) {
			short cache = in.readUnsignedByte();
			short file = in.readShort();
			if (opcode == 1) {
				client.submitRequest(new FileRequest(channel, cache, file));
			}
		}
		return null;
	}
}