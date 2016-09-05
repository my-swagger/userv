package com.userv;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * A class to provide an UpdateServer.
 * 
 * @author Bot
 * 
 */
public class UpdateServer {

	/**
	 * The operating port of the update server.
	 */
	private int port;
	/**
	 * The operating revision of the update server.
	 */
	private int revision;
	/**
	 * The working bootstrap of the update server.
	 */
	private ServerBootstrap bootstrap;
	/**
	 * Our ChannelHandler instance.
	 */
	private ChannelHandler channelHandler;
	/**
	 * Our FileRequestWorker instance
	 */
	private FileRequestWorker fileRequestWorker;
	/**
	 * Our ChannelConnectionFilter instance.
	 */
	private ChannelConnectionFilter filter;
	/**
	 * Our Map of clients from which we grab a client by their channel.
	 */
	private Map<Channel, Client> clients;
	/**
	 * Our JS5 Keys.
	 */
	private int[] js5Keys;

	/**
	 * <p>
	 * Create a new UpdateServer <code>Server</code> instance with the specified
	 * <code>port</code> and <code>revision<code>
	 * <p>
	 * 
	 * @param port
	 *            The port to listen on.
	 * @param revision
	 *            The revision that the update server will accept.
	 */
	private UpdateServer(int port, int revision) {
		this.port = port;
		this.revision = revision;
	}

	/**
	 * <p>
	 * Initiate the UpdateServer, starting up all needed resources.
	 * <p>
	 */
	private void init() {
		clients = new ConcurrentHashMap<Channel, Client>();
		channelHandler = new DefaultChannelHandler(this);
		fileRequestWorker = new DefaultFileRequestWorker();
		filter = (ChannelConnectionFilter) channelHandler;
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new DefaultPipelineFactory(this));
		bootstrap.bind(new InetSocketAddress(port));
		File f = new File("./js5.cfg");
		js5Keys = new int[27];
		try {
			if (f.exists()) {
				ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(f.toPath(), Charset.defaultCharset());
				int idx = 0;
				for (String line : lines) {
					line = line.replaceAll(" ", "");
					if (line.matches("[0-9]*")) {
						js5Keys[idx] = Integer.parseInt(line);
						idx++;
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread() {
			private LinkedList<FileRequest> requests = new LinkedList<FileRequest>();

			@Override
			public void run() {
				while (true) {
					for (Client client : clients.values()) {
						for (int i = 0; i < 5; i++) {
							FileRequest request = client.nextRequest();
							if (request == null) {
								break;
							}
							requests.add(request);
						}
					}
					fileRequestWorker.service(requests);
					requests.clear();
					try {
						sleep(400L);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * Set an option in the bootstrap.
	 * 
	 * @param option
	 *            The option to set
	 * @param value
	 *            The option's value.
	 * 
	 */
	public void setOption(String option, Object value) {
		bootstrap.setOption(option, value);
	}

	/**
	 * Set the file request worker
	 * 
	 * @param worker
	 *            The <code>FileRequestWorker<code> to set to.
	 */
	public void setRequestWorker(FileRequestWorker worker) {
		this.fileRequestWorker = worker;
	}

	/**
	 * Gets our ChannelConnectionFilter. By default it is a filter that always accepts new
	 * connections.
	 * 
	 * @return The current <code>ChannelConnectionFilter</code> instance.
	 */
	public ChannelConnectionFilter getFilter() {
		return filter;
	}

	/**
	 * Shut down UpdateServer and release the resources.
	 */
	public void shutdown() {
		bootstrap.releaseExternalResources();
		clients.clear();
	}

	/**
	 * Gets the UpdateServer's ChannelHandler
	 * 
	 * @return The <code>ChannelHandler</code> instance.
	 */
	public ChannelHandler getChannelHandler() {
		return channelHandler;
	}

	/**
	 * 
	 * @return The current revision.
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Gets a Client by the channel
	 * 
	 * @return The <code>Client</code> instance of the channel.
	 */
	public Client getClient(Channel channel) {
		return clients.get(channel);
	}

	/**
	 * Adds a client to our map.
	 */
	public void addClient(Client client) {
		clients.put(client.getChannel(), client);
	}

	/**
	 * Removes a client from our map
	 * 
	 * @param channel
	 *            The client's channel.
	 */
	public void removeClient(Channel channel) {
		clients.remove(channel);
	}

	/**
	 * Retreives the JS5 Keys read from /js5.cfg
	 * 
	 * @return The read JS5 keys.
	 */
	public int[] getJS5Keys() {
		return js5Keys;
	}

	/**
	 * Creates a new UpdateServer
	 * 
	 * @param port
	 *            The port to construct the UpdateServr with
	 * @return The newly constructed UpdateServer.
	 */
	private static UpdateServer create(int port, int revision) {
		return new UpdateServer(port, revision);
	}

	public static void main(String[] args) {
		int port = 443;
		int revision = 0;
		try {
			port = Integer.parseInt(args[0]);
			revision = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("Use as " + UpdateServer.class.getName() + " <port> <revision>");
			return;
		}
		UpdateServer server = UpdateServer.create(port, revision);
		server.init();
	}
}