#pragma once

#include <iostream>
#include <vector>
#include <atomic>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <string.h>
#include <pthread.h>

#include "DummySensor.h"

#define BUFFER_SIZE 1024

using namespace std;

class Environment_SocketServer {
public:
    Environment_SocketServer(int _port);
    ~Environment_SocketServer();

    void Accept();
    static void* ClientCommunication(void* _parameter);

private:
    bool Communicate(int _socket);

    int passive_socket;
    int port;
    std::vector<DummySensor> mSensors;
    std::atomic<bool> mListening{true};     // if in one thread the msg is "shutdown" -> false 
};

struct socketParam {
    int socket;
    sockaddr saddr;
    Environment_SocketServer* server;   // the thread has no "this", so we pass the object
};