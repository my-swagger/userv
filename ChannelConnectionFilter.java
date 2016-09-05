package com.userv;

/**
 * 
 * @author Bot
 * 
 */
public interface ChannelConnectionFilter {

	public boolean accept(Client client);
	
}