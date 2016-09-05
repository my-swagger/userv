package com.userv;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * 
 * @author Bot
 * 
 */
public class DefaultPipelineFactory implements ChannelPipelineFactory {

	/**
	 * My UpdateServer instance
	 */
	private UpdateServer server;

	DefaultPipelineFactory(UpdateServer server) {
		this.server = server;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new DefaultDecoder(server));
		pipeline.addLast("encoder", new DefaultEncoder());
		pipeline.addLast("handler", server.getChannelHandler());
		return pipeline;
	}
}