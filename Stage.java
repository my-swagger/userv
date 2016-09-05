package com.userv;

/**
 * This class was made to tell the connection status of any client.
 * 
 * @author Bot
 * 
 */
public enum Stage {
	/**
	 * Our Handshake stage for Opcode 15.
	 */
	HANDSHAKE,
	/**
	 * Our Decoding stage for requests.
	 */
	FILE_REQUESTS;
}