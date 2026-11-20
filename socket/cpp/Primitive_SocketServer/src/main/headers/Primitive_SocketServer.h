/**
 * @file Primitive_SocketServer.h
 * @brief Declaration of the Primitive_SocketServer class.
 */
#pragma once

#include <iostream> // cout, cin
#include <sys/socket.h> // socket, bind, listen, accept
#include <netinet/in.h> // IPPROTO_TCP, sockaddr_in,
                        // htons/ntohs, INADDR_ANY
#include <unistd.h> // close
#include <arpa/inet.h> // inet_ntop/inet_atop
#include <string.h> // strlen
#include <semaphore.h> // sem_init
#include <pthread.h>

#define BUFFER_SIZE 1024  ///< size of the receive buffer in bytes

using namespace std;

/**
 * @brief TCP echo server that handles one client at a time.
 *
 * Creates a passive socket, waits for clients and echoes every
 * received message. Supports the admin commands "drop" and "shutdown".
 */
class Primitive_SocketServer {
public:
    /**
     * @brief Creates the passive socket, binds it to the port
     *        and starts listening.
     * @param _port port the server is reachable on
     */
    Primitive_SocketServer(int _port);
    /**
     * @brief Closes the passive socket.
     */
    ~Primitive_SocketServer();
    /**
     * @brief Waits for incoming clients and starts the communication
     *        until a client sends "shutdown".
     */
    void Accept();
private:
    int passive_socket;  ///< passive socket waiting for new clients
    /**
     * @brief Handles the communication with one connected client.
     * @param _socket communication socket returned by accept()
     * @return false if the client sent "shutdown", true otherwise
     */
    bool Communicate(int _socket);
    int port;  ///< port the server listens on

};

