package com.userv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import com.userv.cache.Cache;
import com.userv.cache.FileStore;

/**
 * 
 * @author Bot
 * 
 */
public class FileRequest {

	/**
	 * The Cache instance used in serving requests.
	 */
	private static Cache cache;

	/**
	 * The channel of the overlying client.
	 */
	private Channel channel;

	/**
	 * The archive of the request.
	 */
	private short archive;

	/**
	 * The file within the arhicve of the cache.
	 */
	private short file;

	/**
	 * Here we statically check the cache instance. Note: If you are offended by this, I was
	 * too lazy to do it propery =).
	 */
	static {
		if (cache == null) {
			try {
				cache = new Cache(FileStore.open("./cache/"));
			} catch (FileNotFoundException e) {
				System.err.println("Please place the cache in the ./cache/ directory.");
				System.exit(0);
			}
		}
	}

	/**
	 * Constructs a new file request of the archive and file.
	 * 
	 * @param channel
	 *            The channel of the client, whom this request belongs to.
	 * @param archive
	 *            The requested archive
	 * @param file
	 *            The requested file in the archive
	 */
	public FileRequest(Channel channel, short archive, short file) {
		this.channel = channel;
		this.archive = archive;
		this.file = file;
	}

	/**
	 * <p>
	 * Writes the data from the <code>Cache</code> archive specified from:<br>
	 * <code>archive</code> and <code>file</code>
	 * </p>
	 */
	public void service() {
		ByteBuffer fileBuffer = null;
		ChannelBuffer out = ChannelBuffers.dynamicBuffer();
		try {
			if (archive == 255 && file == 255) {
				fileBuffer = cache.createChecksumTable().encode();
				out.writeByte(255);
				out.writeShort(255);
				out.writeByte(0);
				out.writeInt(fileBuffer.limit());
				out.writeBytes(fileBuffer);
				channel.write(out);
				return;
			}
			fileBuffer = cache.getStore().read(archive, file);
			int compression = fileBuffer.get() & 0xFF;
			int length = fileBuffer.getInt();
			int attributes = compression;
			byte[] cachePayload = new byte[compression != 0 ? length + 4 : length];
			System.arraycopy(fileBuffer.array(), 5, cachePayload, 0, cachePayload.length);
			out.writeByte(archive);
			out.writeShort(file);
			out.writeByte(attributes);
			out.writeInt(length);
			int offset = 8;
			for (byte aCachePayload : cachePayload) {
				if (offset == 512) {
					out.writeByte(255);
					offset = 1;
				}
				out.writeByte(aCachePayload);
				offset++;
			}
			channel.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}