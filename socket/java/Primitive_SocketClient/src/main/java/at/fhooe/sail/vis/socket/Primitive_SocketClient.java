package at.fhooe.sail.vis.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @brief Interactive TCP client: sends lines typed by the user and prints the reply.
 *
 * The whole communication runs inside the constructor until "quit" is typed.
 */
public class Primitive_SocketClient {
	
	/**
	 * @brief Connects to the server and sends user input until "quit" is typed.
	 * @param _ip IP address or host name of the server
	 * @param _port port the server is reachable on
	 * @throws IOException if the connection or the communication fails
	 */
	public Primitive_SocketClient(String _ip, int _port) throws IOException { // try and catch and not like this
		Socket activeSocket = new Socket(_ip, _port);
		InputStream in = activeSocket.getInputStream();
		OutputStream out = activeSocket.getOutputStream();

		byte[] byteBuffer = new byte[1024];
		Scanner scan = new Scanner(System.in);

		while (true) {
			System.out.println("Eingabe: ");
			String msg = scan.nextLine();
			if (msg.length() == 0) {
				System.out.println("No empty messages!");
				continue;
			} else {
				if (msg.equalsIgnoreCase("quit")) {
					System.out.println("shutting down client...");
					break;
				}

				out.write(msg.getBytes());
				out.flush();

				// read() blocks until data arrives; assumes the whole reply fits into one read
				int rVal = in.read(byteBuffer);
				String reply = new String(byteBuffer, 0, rVal);
				System.out.println("message from server: " + reply);
			}


		} // while true
		out.close();
		in.close();
		activeSocket.close();
	}

	/**
	 * @brief Starts the client with the address given on the command line.
	 * @param _argv [0] IP address of the server, [1] port of the server
	 * @throws IOException if the communication with the server fails
	 */
	public static void main(String[] _argv) throws IOException {
        System.out.println("Starting Primitive_SocketClient ...");

		if (_argv.length != 2) {
			System.out.println("Usage Primitive_SocketClient <IP> <PORT>");
			return;
		}

		int port = Integer.parseInt(_argv[1]);
		Primitive_SocketClient client = new Primitive_SocketClient(_argv[0], port);

        System.out.println("Stopping Primitive_SocketClient ...");
    }
}
