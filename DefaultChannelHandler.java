package com.userv;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;

/**
 * 
 * @author Bot
 * 
 */
@Sharable
public class DefaultChannelHandler extends SimpleChannelHandler implements ChannelConnectionFilter {

	/**
	 * My server instance.
	 */
	private UpdateServer server;

	DefaultChannelHandler(UpdateServer server) {
		this.server = server;
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		server.removeClient(e.getChannel());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Client client = new Client(e.getChannel());
		if (!accept(client)) {
			return;
		}
		server.addClient(client);
	}

	@Override
	public boolean accept(Client client) {
		return true;
	}
}