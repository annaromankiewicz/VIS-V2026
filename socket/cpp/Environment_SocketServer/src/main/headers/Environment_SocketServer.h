/**
 * @file Environment_SocketServer.h
 * @brief Declaration of the Environment_SocketServer class and the thread parameter struct.
 */
#pragma once

#include <iostream>
#include <vector>
#include <atomic>
#include <sys/socket.h>
#include "DummySensor.h"

#define BUFFER_SIZE 1024  ///< size of the receive buffer in bytes

using namespace std;

/**
 * @brief Multi-threaded TCP server that provides the values of dummy sensors.
 *
 * Creates a passive socket and starts one thread per connected client.
 * Supported requests (terminated with '#'): "reqEnvDataTypes()#",
 * "reqEnvData(<type>)#" and "reqAll()#". Any other request is answered with
 * "unknown command#", so a client waiting for the '#' terminator never blocks.
 * The admin commands "drop" (close this connection) and "shutdown" (stop the
 * whole server) are also supported.
 */
class Environment_SocketServer {
public:
    /**
     * @brief Creates the dummy sensors, creates the passive socket, binds it
     *        to the port and starts listening.
     * @param _port port the server is reachable on
     */
    Environment_SocketServer(int _port);
    /**
     * @brief Closes the passive socket if it is still open.
     */
    ~Environment_SocketServer();

    /**
     * @brief Waits for incoming clients and starts a detached thread for each one.
     *
     * Uses poll() with a 500 ms timeout so that the loop can end as soon as
     * mListening is set to false. Closes the passive socket at the end.
     */
    void Accept();
    /**
     * @brief Thread function that handles one client.
     *
     * Must be static because pthread_create() needs a plain function pointer;
     * the server object is therefore passed inside the parameter struct.
     * @param _parameter pointer to a heap allocated socketParam; it is deleted here
     * @return always nullptr (no result is passed back to the creator)
     */
    static void* ClientCommunication(void* _parameter);

private:
    /**
     * @brief Handles the requests of one connected client until it disconnects.
     * @param _socket communication socket returned by accept()
     * @return false if the client sent "shutdown", true otherwise
     */
    bool Communicate(int _socket);

    int mPassiveSocket;  ///< passive socket waiting for new clients (-1 when closed)
    std::vector<DummySensor> mSensors;  ///< dummy sensors whose values are sent to the clients
    std::atomic<bool> mListening{true};     ///< true while the server runs; set to false by a "shutdown" in any client thread
};

/**
 * @brief Parameters passed to the client thread ClientCommunication().
 *
 * pthread_create() only allows one void* argument, so everything the thread
 * needs is packed into this struct.
 */
struct socketParam {
    int socket;  ///< communication socket of the client
    sockaddr saddr;  ///< address of the client, filled in by accept()
    Environment_SocketServer* server;   ///< server object, needed because the static thread function has no "this"
};