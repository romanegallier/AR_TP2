package v1;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;


/**
 * NIO elementary server
 * RICM4 TP
 * F. Boyer
 */

public class NioServer implements Runnable {

	public static int DEFAULT_SERVER_PORT = 8888;

	// The channel used to accept connections from server-side
	private ServerSocketChannel serverChannel;

	// Unblocking selector
	private Selector selector;

	// Ip address of the server
	private InetAddress hostAddress;

	// to complete


	/**
	 * NIO engine initialization for server side
	 * @param the host address and port of the server
	 * @throws IOException 
	 */
	public NioServer(int port) 
			throws IOException {

		// create a new selector
		selector = SelectorProvider.provider().openSelector();

		// create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// bind the server socket to the given address and port
		hostAddress = InetAddress.getByName("localhost");
		InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
		serverChannel.socket().bind(isa);

		// be notified when connection requests arrive
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}



	/**
	 * NIO engine mainloop
	 * Wait for selected events on registered channels
	 * Selected events for a given channel may be ACCEPT, CONNECT, READ, WRITE
	 * Selected events for a given channel may change over time
	 */
	public void run() {
		System.out.println("NioServer running");
		while (true) {
			try {

				selector.select();

				Iterator<?> selectedKeys = this.selector.selectedKeys().iterator();

				while (selectedKeys.hasNext()) {

					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					} else if (key.isAcceptable()) {
						handleAccept(key);

					} else if (key.isReadable()) {
						handleRead(key);

					} else if (key.isWritable()) {
						handleWrite(key);

					} else if (key.isConnectable()) {
						handleConnect(key);
					} else 
						System.out.println("  ---> unknow key=");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Accept a connection and make it non-blocking
	 * @param the key of the channel on which a connection is requested
	 */
	private void handleAccept(SelectionKey key) {
		SocketChannel socketChannel = null;
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		try {
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			// as if there was no accept done
			return;
		}

		// be notified when there is incoming data 
		try {
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			handleClose(socketChannel);
		}
	}


	/**
	 * Finish to establish a connection
	 * @param the key of the channel on which a connection is requested
	 */
	private void handleConnect(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}
		key.interestOps(SelectionKey.OP_READ);	
	}


	/**
	 * Close a channel 
	 * @param the key of the channel to close
	 */
	private void handleClose(SocketChannel socketChannel) {
		try{
			socketChannel.close();
		} catch (IOException e) {
			// nothing to do, the channel is already closed
		}
		socketChannel.keyFor(selector).cancel();
	}

	/**
	 * Handle incoming data event
	 * @param the key of the channel on which the incoming data waits to be received 
	 */
	private void handleRead(SelectionKey key) {
		// todo
	}


	/**
	 * Handle outgoing data event
	 * @param the key of the channel on which data can be sent 
	 */
	private void handleWrite(SelectionKey key) {
		// todo
	}

	/**
	 * Send data
	 * @param the key of the channel on which data that should be sent
	 * @param the data that should be sent
	 */
	public void send(SocketChannel socketChannel, byte[] data) {
		// todo
	}

	public static void main(String args[]){
		int serverPort = DEFAULT_SERVER_PORT;  
		String arg;

		try {
			for (int i = 0; i< args.length; i++) {
				arg = args[i];
				if (arg.equals("-p")){
					serverPort = new Integer(args[++i]).intValue();
				}
			}
		} catch (Exception e){
			System.out.println("Usage: NioServer [-p <serverPort>]");
			System.exit(0);
		}

		try{
			new Thread(new NioServer(serverPort)).start();

		} catch (IOException e){
			System.out.println("NioServer Exception " + e);
			e.printStackTrace();
		}
	}


}
