
# Documentation Overview

The comments follow `doc/docGuidelines`: Doxygen/JavaDoc in English, written from the existing code and comments.
Only comments were added. No code was changed.
Line numbers refer to the files **after** the comments were added.

Scope: Primitive_SocketServer (C++ and Java), Environment_SocketServer (C++, incl. `DummySensor.h`),
Primitive_SocketClient (C++ and Java), Environment_SocketClient (Java), `IEnvService`.

Legend: **Doxygen block** = `/** ... */` above the element · **`///<`** = trailing member comment · **`//`** = normal inline comment in the implementation.

## IEnvService (`general/EnvironmentI/src/main/java/at/fhooe/sail/vis/general/IEnvService.java`)

| Line | Element | What was added |
|---|---|---|
| 6–10 | `interface IEnvService` | JavaDoc: @brief + description (transport is up to the implementation) |
| 12–16 | `reqEnvDataTypes()` | @brief, @return (sensor names), @throws IOException |
| 18–23 | `reqEnvData(String _type)` | @brief, @param _type, @return, @throws IOException |
| 25–29 | `reqAll()` | @brief, @return (one EnvData per sensor, same timestamp), @throws IOException |

## Environment_SocketServer – C++

### `socket/cpp/Environment_SocketServer/src/main/headers/Environment_SocketServer.h`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 13 | `BUFFER_SIZE` | `///<` size of the receive buffer |
| 17–24 | `class Environment_SocketServer` | @brief + description: one thread per client, supported requests, `drop` / `shutdown` |
| 27–31 | `Environment_SocketServer(int _port)` | @brief (sensors, socket, bind, listen), @param _port |
| 33–35 | `~Environment_SocketServer()` | @brief |
| 38–43 | `Accept()` | @brief + description (poll with 500 ms timeout, closes passive socket) |
| 45–52 | `ClientCommunication(void*)` | @brief, why it is static, @param _parameter (deleted inside), @return nullptr |
| 56–60 | `Communicate(int _socket)` | @brief, @param _socket, @return false on "shutdown" |
| 63 | `passive_socket` | `///<` member description (-1 when closed) |
| 64 | `port` | `///<` member description |
| 65 | `mSensors` | `///<` member description |
| 66 | `mListening` | your existing `//` comment changed to `///<` and translated into a full sentence |
| 69–74 | `struct socketParam` | @brief + why the struct is needed (pthread takes one `void*`) |
| 76 | `socketParam::socket` | `///<` member description |
| 77 | `socketParam::saddr` | `///<` member description |
| 78 | `socketParam::server` | your existing `//` comment changed to `///<` |

### `socket/cpp/Environment_SocketServer/src/main/headers/DummySensor.h`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 19–24 | `class DummySensor` | @brief + description (random values in [min, max]) |
| 27–33 | `DummySensor(...)` | @brief, @param _name, _valueCount, _min, _max |
| 37–40 | `GetName()` | @brief, @return |
| 43–46 | `ReadValues()` | @brief, @return (format `v1;v2;...`) |
| 57 | `mName` | `///<` member description |
| 58 | `mValueCount` | `///<` member description |
| 59 | `mDist` | `///<` member description |
| 61–66 | `Generator()` | @brief, why it is `thread_local`, @return |

### `socket/cpp/Environment_SocketServer/src/main/cpp/Environment_SocketServer.cpp`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 13 | constructor – sensor list | `//` meaning of the DummySensor arguments |
| 38 | constructor – `listen()` | `//` meaning of the backlog 5 |
| 82–83 | `Accept()` – `new socketParam` | `//` why it is on the heap and who deletes it |
| 103 | `Accept()` – `pthread_detach()` | `//` why the thread is detached |
| 127–128 | `ClientCommunication()` – shutdown check | `//` when the server stops and how Accept() notices it |
| 149 | `Communicate()` – `getpeername()` | `//` reads client IP/port for logging |
| 163–164 | `Communicate()` – `poll()` | `//` why the 500 ms timeout is used |
| 203 | `Communicate()` – reqEnvDataTypes | `//` reply format |
| 219 | `Communicate()` – reqEnvData | `//` reply format incl. "no such datatype#" |
| 240 | `Communicate()` – reqAll | `//` reply format |
| 271–276 | `main(...)` | @brief, @param _argc, @param _argv, @return |

## Primitive_SocketServer – C++

### `socket/cpp/Primitive_SocketServer/src/main/headers/Primitive_SocketServer.h`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 17 | `BUFFER_SIZE` | `///<` size of the receive buffer |
| 21–26 | `class Primitive_SocketServer` | @brief + description (echo server, one client at a time) |
| 29–33 | `Primitive_SocketServer(int _port)` | @brief, @param _port |
| 35–37 | `~Primitive_SocketServer()` | @brief |
| 39–42 | `Accept()` | @brief |
| 45 | `passive_socket` | `///<` member description |
| 46–50 | `Communicate(int _socket)` | @brief, @param _socket, @return false on "shutdown" |
| 52 | `port` | `///<` member description |

### `socket/cpp/Primitive_SocketServer/src/main/cpp/Primitive_SocketServer.cpp`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 29 | constructor – `listen()` | `//` meaning of the backlog 5 |
| 46 | `Accept()` – loop | `//` only one client at a time |
| 64 | `Communicate()` – `getpeername()` | `//` reads client IP/port for logging |
| 117–122 | `main(...)` | @brief, @param _argc, @param _argv, @return |

## Primitive_SocketServer – Java (`socket/java/Primitive_SocketServer/src/main/java/at/fhooe/sail/vis/socket/Primitive_SocketServer.java`)

| Line | Element | What was added |
|---|---|---|
| 9–14 | `class Primitive_SocketServer` | JavaDoc @brief + description |
| 17–21 | `main(String[] _argv)` | @brief, @param _argv, @throws IOException |
| 35 | `main()` – accept loop | `//` outer loop = clients, inner loop = messages |

## Primitive_SocketClient – C++ (`socket/cpp/Primitive_SocketClient/src/main/cpp/Primitive_SocketClient.cpp`)

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 20–25 | `main(...)` | @brief, @param _argc, @param _argv, @return |
| 58 | `main()` – send loop | `//` one request/reply per loop, "quit" ends the client |
| 80 | `main()` – `recv()` | `//` blocks until the reply arrives, 0 = server closed the connection |

## Primitive_SocketClient – Java (`socket/java/Primitive_SocketClient/src/main/java/at/fhooe/sail/vis/socket/Primitive_SocketClient.java`)

| Line | Element | What was added |
|---|---|---|
| 9–13 | `class Primitive_SocketClient` | JavaDoc @brief + description |
| 16–21 | `Primitive_SocketClient(String _ip, int _port)` | @brief, @param _ip, @param _port, @throws IOException |
| 45 | constructor – `in.read()` | `//` blocking read, reply assumed to fit into one read |
| 58–62 | `main(String[] _argv)` | @brief, @param _argv, @throws IOException |

## Environment_SocketClient – Java (`socket/java/Environment_SocketClient/src/main/java/at/fhooe/sail/vis/socket/Environment_SocketClient.java`)

| Line | Element | What was added |
|---|---|---|
| 12–17 | `class Environment_SocketClient` | JavaDoc @brief + description (implements IEnvService over TCP) |
| 20–22 | `in` | member description |
| 24–26 | `out` | member description |
| 30–35 | `Environment_SocketClient(String _ip, int _port)` | @brief, @param _ip, @param _port, @throws IOException |
| 47–51 | `reqEnvDataTypes()` | @brief, @return, @throws IOException |
| 67–72 | `reqEnvData(String _type)` | @brief, @param _type, @return, @throws IOException |
| 89–93 | `reqAll()` | @brief, @return, @throws IOException |
| 118–123 | `envDataFabric(String _response)` | @brief, @param _response (format), @return |
| 139–143 | `envDataFabricAll(String response)` | @brief, @param response (format), @return |
| 154–158 | `cleanResponse(String response)` | @brief, @param response, @return |
| 166–170 | `readResponse()` | @brief, @return, @throws IOException |
| 183–187 | `main(String[] _argv)` | @brief, @param _argv, @throws IOException |
