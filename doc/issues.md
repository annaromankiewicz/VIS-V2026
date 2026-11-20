# Known Issues

These issues were found while writing the documentation. **None of them have been fixed.**
Line numbers refer to the files after the comments were added.
Severity: **high** = the build fails or the program misbehaves · **medium** = wrong behaviour in some situations · **low** = style or cleanup.

## Environment_SocketServer (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| E1 | **high** | `Environment_SocketServer.h` (uncommitted change) | The includes `<netinet/in.h>`, `<unistd.h>`, `<arpa/inet.h>`, `<string.h>` and `<pthread.h>` were removed, and the build fails | `Environment_SocketServer.cpp` uses `sockaddr_in`, `INADDR_ANY`, `htons`, `close`, `inet_ntop`, `strerror` and `pthread_create`. `c++ -fsyntax-only` fails with *"unknown type name 'sockaddr_in'"*, *"use of undeclared identifier 'close'"* and more. Either put the includes back in the header or add them to the `.cpp`. |
| E2 | **high** | `.cpp` `ClientCommunication()` line 132, `Communicate()` line 262 | The client socket leaks on a normal disconnect | `close(clientSocket)` is only called when the server shuts down. When a client disconnects normally (`recv` returns 0) or an error occurs, the socket is never closed, because `close(_socket)` at the end of `Communicate()` is commented out. Each such client leaves one open file descriptor. |
| E3 | **high** | `.cpp` `Communicate()` line 253 | An unknown command gets no reply | If the message matches no command, nothing is sent. `rVal` still holds the `recv` byte count, so the loop simply continues. A client that waits for the `#` terminator (the Java `readResponse()`) then blocks forever. |
| E4 | medium | `.cpp` `Communicate()` line 178 | It assumes one `recv()` returns exactly one message | TCP is a byte stream. One request can arrive in several pieces, and two requests can arrive in one `recv()`. The server compares the raw buffer with the command strings and never looks for the `#` delimiter, so split or merged messages are not recognised. |
| E5 | medium | `.cpp` constructor lines 41–42 | A failed `listen()` does not stop the server | `perror` is printed, but the program continues without a listening socket. `socket()` and `bind()` call `exit(EXIT_FAILURE)` in the same situation. |
| E6 | medium | `.cpp` `Communicate()` line 183 | `perror("Connection closed ...")` | When `recv` returns 0, `errno` is not set, so `perror` appends a random or stale error text. `cout` / `cerr` would fit better. |
| E7 | medium | `DummySensor.h` `ReadValues()` / `mDist` | Possible data race | Several client threads can call `ReadValues()` on the same `DummySensor` at the same time. The generator is `thread_local`, but `mDist` is shared, and `uniform_int_distribution::operator()` is non-const. Strictly speaking, this is a data race. |
| E8 | medium | `.cpp` `main()` line 296 / detached threads | Threads can outlive the server object | Client threads are detached and access `server->mListening`. They usually end within 500 ms after a shutdown, but nothing guarantees that they have finished before `server` is destroyed at the end of `main()`. |
| E9 | low | `.cpp` lines 18 and 145 | Copy-paste log messages | The constructor prints `"Primitive_SocketServer::Primitive_SocketServer()"`, and `Communicate()` prints `"Primitive_SocketServer::Commnicate()"` (also misspelled). |
| E10 | low | `.cpp` line 238 | Wrong comment | The comment on `if (message == "reqAll()#")` was copied from `reqEnvData` ("check if message starts with reqEnvData(..."). |
| E11 | low | `.cpp` lines 120, 221, 241, `.h` line 64 | Unused variables | `clientAddr` in `ClientCommunication()`, `sensorsLen` in the reqEnvData and reqAll branches, and the member `port` are never used. The local `port` in `Communicate()` also shadows the member. |
| E12 | low | `.h` lines 63–64 | Naming convention | `passive_socket` and `port` do not use the `m` prefix that `mSensors` and `mListening` use. |
| E13 | low | `DummySensor.h` lines 9–12 | Include guard is empty | `#endif` follows directly after `#define VIS_2026_DUMMYSENSOR_H`, so the guard protects nothing. Only `#pragma once` does the work. One of the two should be removed. |
| E14 | low | `.h` line 15 | `using namespace std;` in a header | It leaks `std` into every file that includes the header. The `.cpp` relies on it (`cout`, `string`). |
| E15 | low | `.cpp` line 286 | `atoi` without validation | An invalid port string becomes 0, and the server then binds to a random port. |

## Primitive_SocketServer (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| P1 | **high** | `Primitive_SocketServer.cpp` `Communicate()` lines 82–84 | There is no `break` when the client disconnects | When `recv` returns 0, the code only prints a message and continues. `string(buffer, 0)` is empty, an `"Echo: "` is sent to the closed socket, and the loop repeats. The result is an endless loop, or the process is killed by `SIGPIPE`. The Environment server already has the `break`. |
| P2 | medium | `.cpp` constructor line 32 | A failed `listen()` does not stop the server | Same as E5. |
| P3 | low | `.cpp` `Communicate()` | Same `perror` and log-text problems as E6 and E9 | `perror("Connection closed ...")` is used without an `errno`, and the log text has the typo "Commnicate". |
| P4 | low | `Primitive_SocketServer.h` line 19 | `using namespace std;` in a header | Same as E14. |

## Primitive_SocketServer (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PJ1 | medium | `Primitive_SocketServer.java` line 53 | `drop` does not drop the connection | The `drop` branch only prints a message. There is no `break`, so the connection stays open and the client also gets no reply. |
| PJ2 | low | line 49 | The `rVal == 0` branch is unreachable | `InputStream.read(byte[])` with a non-empty buffer blocks until at least 1 byte arrives or returns -1. It never returns 0. |
| PJ3 | low | `main()` | No `try`/`finally` | If an `IOException` occurs, the sockets and streams are not closed. |

## Primitive_SocketClient (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PC1 | medium | `Primitive_SocketClient.cpp` line 45 | The `inet_pton` result is only checked for `-1` | `inet_pton` returns **0** for an invalid address string such as `"abc"`. That case is not detected, and `connect` then uses an undefined address. |
| PC2 | low | includes | `<iostream>` is included twice, and `<semaphore.h>` and `<pthread.h>` are not used | |

## Primitive_SocketClient (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PCJ1 | medium | `Primitive_SocketClient.java` lines 46–47 | The client crashes when the server closes the connection | `in.read()` returns -1, and `new String(byteBuffer, 0, -1)` throws `StringIndexOutOfBoundsException`. |
| PCJ2 | low | constructor line 22 | All the work happens in the constructor | The whole communication loop runs inside the constructor. Your own comment already notes the missing try/catch. Resources are not closed if an exception occurs. |
| PCJ3 | low | `main()` | The unused variable `client` | |

## Environment_SocketClient (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| EC1 | **high** | `envDataFabric()` line 125 | The "no such datatype" check never matches, and the client crashes | `cleanResponse()` has already removed the `#`, so `"no such datatype#"` is never equal to the response. `Long.parseLong("no such datatype")` then throws `NumberFormatException`. The comment "is never happening" is not true: it happens whenever an unknown sensor is requested. |
| EC2 | medium | `reqEnvData()` line 85 | Possible `NullPointerException` | If `envDataFabric()` returns `null` (once EC1 is fixed), `envData.setmSensorName(_type)` throws an NPE. |
| EC3 | medium | constructor line 40 | The socket is never closed | `activeSocket` is a local variable. Neither the socket nor the streams are ever closed, and there is no `close()` method. |
| EC4 | low | `readResponse()` line 175 | Byte-to-char cast | `(char) c` only works for ASCII. Non-ASCII characters would be decoded wrongly. This is fine for the current protocol. |
| EC5 | low | line 55 and others | `getBytes()` has no charset | It uses the platform default charset. `StandardCharsets.US_ASCII` or `UTF_8` would make it explicit. |
| EC6 | low | `main()` line 221 | The loop runs only once | `listening = false` at the end of the first iteration. This may be intentional for testing. |
| EC7 | low | `main()` | `main` is not indented like the other methods | Formatting only. |

## IEnvService

No issues found.

## General

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| G1 | low | all in-scope files | German and English comments are mixed | The guidelines recommend English. Your existing German `//` comments (e.g. "erste socket", "irgendein freier port") were left unchanged. |
| G2 | low | `// adresse in apv4`, `// irgendein freier port` | Inaccurate comments | It should be "IPv4", and the port is the one passed on the command line, not "any free port". |
