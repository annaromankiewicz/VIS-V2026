
# Documentation Overview

The comments follow `doc/docGuidelines`: Doxygen/JavaDoc in English, written from the existing code and comments.
The first pass only added comments. The second pass fixed the issues E3, E5, E9–E13, P1, P2, PJ1, PJ2, PC1, PC2, EC1–EC3 and EC7 from `doc/issues.md` and documented every change in the same style. Rows marked **(fix)** belong to the second pass.
Line numbers refer to the current state of the files.

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
| 17–26 | `class Environment_SocketServer` | @brief + description: one thread per client, supported requests, **(fix, E3)** reply `"unknown command#"` for any other request, `drop` / `shutdown` |
| 29–34 | `Environment_SocketServer(int _port)` | @brief (sensors, socket, bind, listen), @param _port |
| 35–38 | `~Environment_SocketServer()` | @brief |
| 40–46 | `Accept()` | @brief + description (poll with 500 ms timeout, closes passive socket) |
| 47–55 | `ClientCommunication(void*)` | @brief, why it is static, @param _parameter (deleted inside), @return nullptr |
| 58–63 | `Communicate(int _socket)` | @brief, @param _socket, @return false on "shutdown" |
| 65 | `mPassiveSocket` | `///<` member description (-1 when closed). **(fix, E12)** renamed from `passive_socket` |
| – | ~~`port`~~ | **(fix, E11)** unused member removed |
| 66 | `mSensors` | `///<` member description |
| 67 | `mListening` | your existing `//` comment changed to `///<` and translated into a full sentence |
| 70–75 | `struct socketParam` | @brief + why the struct is needed (pthread takes one `void*`) |
| 77 | `socketParam::socket` | `///<` member description |
| 78 | `socketParam::saddr` | `///<` member description |
| 79 | `socketParam::server` | your existing `//` comment changed to `///<` |

### `socket/cpp/Environment_SocketServer/src/main/headers/DummySensor.h`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 9 | include guard | **(fix, E13)** empty `#ifndef/#define/#endif` block removed, only `#pragma once` is left |
| 13–18 | `class DummySensor` | @brief + description (random values in [min, max]) |
| 21–28 | `DummySensor(...)` | @brief, @param _name, _valueCount, _min, _max |
| 31–35 | `GetName()` | @brief, @return |
| 37–41 | `ReadValues()` | @brief, @return (format `v1;v2;...`) |
| 51 | `mName` | `///<` member description |
| 52 | `mValueCount` | `///<` member description |
| 53 | `mDist` | `///<` member description |
| 55–61 | `Generator()` | @brief, why it is `thread_local`, @return |

### `socket/cpp/Environment_SocketServer/src/main/cpp/Environment_SocketServer.cpp`

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 21 | constructor – sensor list | `//` meaning of the DummySensor arguments |
| 26 | constructor – log output | **(fix, E9)** prints `Environment_SocketServer::Environment_SocketServer()` |
| 45 | constructor – `listen()` | `//` meaning of the backlog 5 |
| 49–51 | constructor – `listen()` failed | **(fix, E5)** `//` why the server stops; `close()` + `exit(EXIT_FAILURE)` |
| 90–91 | `Accept()` – `new socketParam` | `//` why it is on the heap and who deletes it |
| 111 | `Accept()` – `pthread_detach()` | `//` why the thread is detached |
| 134–135 | `ClientCommunication()` – shutdown check | `//` when the server stops and how Accept() notices it |
| 151 | `Communicate()` – log output | **(fix, E9)** prints `Environment_SocketServer::Communicate()` |
| 155 | `Communicate()` – `getpeername()` | `//` reads client IP/port for logging; **(fix, E11)** local `port` renamed to `peerPort` |
| 169–170 | `Communicate()` – `poll()` | `//` why the 500 ms timeout is used |
| 208 | `Communicate()` – reqEnvDataTypes | `//` reply format |
| 224 | `Communicate()` – reqEnvData | `//` reply format incl. "no such datatype#" |
| 242–244 | `Communicate()` – reqAll | `//` reply format; **(fix, E10)** comment corrected to `check if the message is exactly "reqAll()#"` |
| 255–260 | `Communicate()` – unknown request | **(fix, E3)** requests are now an `if / else if` chain; the final `else` sends `"unknown command#"`, `//` why |
| 281–286 | `main(...)` | @brief, @param _argc, @param _argv, @return |

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
| 31 | constructor – `listen()` | `//` meaning of the backlog 5 |
| 35–37 | constructor – `listen()` failed | **(fix, P2)** `//` why the server stops; `close()` + `exit(EXIT_FAILURE)` |
| 51 | `Accept()` – loop | `//` only one client at a time |
| 69 | `Communicate()` – `getpeername()` | `//` reads client IP/port for logging |
| 89–90 | `Communicate()` – `recv()` returned 0 | **(fix, P1)** `//` why the loop stops; `break` added |
| 124–129 | `main(...)` | @brief, @param _argc, @param _argv, @return |

## Primitive_SocketServer – Java (`socket/java/Primitive_SocketServer/src/main/java/at/fhooe/sail/vis/socket/Primitive_SocketServer.java`)

| Line | Element | What was added |
|---|---|---|
| 9–14 | `class Primitive_SocketServer` | JavaDoc @brief + description; **(fix, PJ1)** now also mentions `drop` |
| 17–21 | `main(String[] _argv)` | @brief, @param _argv, @throws IOException |
| 35 | `main()` – accept loop | `//` outer loop = clients, inner loop = messages |
| 45–46 | `main()` – `in.read()` | **(fix, PJ2)** `//` read() never returns 0; the unreachable `rVal == 0` branch was removed |
| 55–57 | `main()` – `drop` | **(fix, PJ1)** `//` what happens after `break` (streams/socket closed, next client) |

## Primitive_SocketClient – C++ (`socket/cpp/Primitive_SocketClient/src/main/cpp/Primitive_SocketClient.cpp`)

| Line | Element | What was added |
|---|---|---|
| 1–4 | file | @file, @brief |
| 5–13 | includes | **(fix, PC2)** duplicate `<iostream>` and unused `<semaphore.h>` / `<pthread.h>` removed |
| 19–24 | `main(...)` | @brief, @param _argc, @param _argv, @return |
| 43–44 | `main()` – `inet_pton()` | **(fix, PC1)** `//` meaning of the return values 1 / 0 / -1; 0 is now handled ("invalid IP address") |
| 64 | `main()` – send loop | `//` one request/reply per loop, "quit" ends the client |
| 86 | `main()` – `recv()` | `//` blocks until the reply arrives, 0 = server closed the connection |

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
| 12–19 | `class Environment_SocketClient` | JavaDoc @brief + description (implements IEnvService over TCP); **(fix, EC3)** also implements `AutoCloseable` |
| 22–24 | `mSocket` | **(fix, EC3)** new member, so the socket can be closed later |
| 26–28 | `in` | member description |
| 30–32 | `out` | member description |
| 36–41 | `Environment_SocketClient(String _ip, int _port)` | @brief, @param _ip, @param _port, @throws IOException |
| 52–58 | `close()` | **(fix, EC3)** new method: @brief, @throws IOException, `//` socket closes its streams too |
| 65–69 | `reqEnvDataTypes()` | @brief, @return, @throws IOException |
| 85–91 | `reqEnvData(String _type)` | @brief, @param _type, @return (**(fix, EC2)** "or null if the server does not know the sensor"), @throws IOException |
| 104 | `reqEnvData()` – null check | **(fix, EC2)** name is only set if the reply was not "no such datatype#" |
| 110–114 | `reqAll()` | @brief, @return, @throws IOException |
| 139–144 | `envDataFabric(String _response)` | @brief, @param _response (format), @return |
| 146 | `envDataFabric()` – unknown sensor | **(fix, EC1)** compares with `"no such datatype"` (without `#`); wrong "is never happening" comment replaced |
| 161–165 | `envDataFabricAll(String response)` | @brief, @param response (format), @return |
| 176–180 | `cleanResponse(String response)` | @brief, @param response, @return |
| 188–192 | `readResponse()` | @brief, @return, @throws IOException |
| 205–209 | `main(String[] _argv)` | @brief, @param _argv, @throws IOException; **(fix, EC7)** indented like the other methods |
| 220 | `main()` – try-with-resources | **(fix, EC3)** `//` close() is called automatically; **(fix, EC2)** prints a message for a `null` reply |
