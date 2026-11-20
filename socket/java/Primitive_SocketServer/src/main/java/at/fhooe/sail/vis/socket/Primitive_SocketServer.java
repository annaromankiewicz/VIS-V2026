package at.fhooe.sail.vis.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @brief TCP echo server that handles one client at a time.
 *
 * Echoes every message with the prefix "ECHO: ". The command "shutdown"
 * stops the server.
 */
public class Primitive_SocketServer {

    /**
     * @brief Starts the echo server on the port given on the command line.
     * @param _argv [0] port the server is reachable on
     * @throws IOException if the port is already in use or the communication fails
     */
    public static void main(String[] _argv) throws IOException {
		System.out.println("Starting Primitive_SocketServer ... ");

        if(_argv.length != 1) {
            System.out.println("Usage: Primitive_SocketServer <PORT>");
            return;
        }
        int port = Integer.parseInt(_argv[0]);      // in c++ _argv[1]

        ServerSocket passiveSocket = new ServerSocket(port);        //  nice question for exam - port can be occupied allready
        System.out.println("Server is waiting for clients on port " + port);

        boolean accepting = true;
        // outer loop: one client after another; inner loop: messages of the current client
        while (accepting) {
            Socket activeSocket = passiveSocket.accept();
            System.out.println("Client connected...");

            InputStream in = activeSocket.getInputStream();
            OutputStream out = activeSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            while (true) {
                int rVal = in.read(buffer);
                if (rVal < 0) {
                    System.out.println("Connection killed...");
                    break;
                } else if (rVal == 0) {
                    System.out.println("Client disconnected...");
                } else {
                    String msg = new String(buffer, 0, rVal);
                    if (msg.equalsIgnoreCase("drop")) {
                        System.out.println("Received ´drop´ command...");
                    } else if (msg.equalsIgnoreCase("shutdown")) {
                        accepting = false;
                        System.out.println("Received ´shutdown´ command...");
                        break;
                    } else {
                        System.out.println("Message received: " + msg);
                        String reply = "ECHO: " + msg;
                        out.write(reply.getBytes());
                        out.flush();
                    }
                }
            } // while true
            out.close();
            in.close();
            activeSocket.close();
        } // while accepting
                     passiveSocket.close();


		System.out.println("Stopping Primitive_SocketServer ... ");
    } // main
}
