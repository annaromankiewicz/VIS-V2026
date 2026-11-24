# Known Issues

These issues were found while writing the documentation. Issues marked ✅ **resolved** have been fixed; all others are still open.
Resolved in the second pass: E3, E5, E9–E13, P1, P2, PJ1, PJ2, PC1, PC2, EC1, EC2, EC3, EC7 (see `doc/documentationOverview.md`, rows marked **(fix)**).
Line numbers of open issues refer to the files after the comments were added and may have shifted slightly because of the fixes.
Severity: **high** = the build fails or the program misbehaves · **medium** = wrong behaviour in some situations · **low** = style or cleanup.

## Environment_SocketServer (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| E1 | **high** | `Environment_SocketServer.h` (uncommitted change) | The includes `<netinet/in.h>`, `<unistd.h>`, `<arpa/inet.h>`, `<string.h>` and `<pthread.h>` were removed, and the build fails | `Environment_SocketServer.cpp` uses `sockaddr_in`, `INADDR_ANY`, `htons`, `close`, `inet_ntop`, `strerror` and `pthread_create`. `c++ -fsyntax-only` fails with *"unknown type name 'sockaddr_in'"*, *"use of undeclared identifier 'close'"* and more. Either put the includes back in the header or add them to the `.cpp`. |
| E2 | ~~high~~ ✅ **resolved** | `.cpp` `Communicate()` line 266 | ~~The client socket leaks on a normal disconnect~~ | Resolved by your change: the drop branch no longer closes the socket, `ClientCommunication()` no longer closes it, and `close(_socket)` at the end of `Communicate()` is active. Every way out of the loop now closes the socket exactly once. |
| E3 | ~~high~~ ✅ **resolved** | `.cpp` `Communicate()` | ~~An unknown command gets no reply~~ | The request checks are now an `if / else if` chain. A final `else` replies `"unknown command#"`, so a client waiting for `#` no longer blocks. Documented in the class @brief and with a `//` comment. |
| E4 | medium | `.cpp` `Communicate()` line 183 | It assumes one `recv()` returns exactly one message | TCP is a byte stream. One request can arrive in several pieces, and two requests can arrive in one `recv()`. The server compares the raw buffer with the command strings and never looks for the `#` delimiter, so split or merged messages are not recognised. |
| E5 | ~~medium~~ ✅ **resolved** | `.cpp` constructor | ~~A failed `listen()` does not stop the server~~ | After `perror`, the passive socket is closed and the program stops with `exit(EXIT_FAILURE)`, like `socket()` and `bind()`. |
| E6 | medium | `.cpp` `Communicate()` line 188 | `perror("Connection closed ...")` | When `recv` returns 0, `errno` is not set, so `perror` appends a random or stale error text. `cout` / `cerr` would fit better. |
| E7 | medium | `DummySensor.h` `ReadValues()` / `mDist` | Possible data race | Several client threads can call `ReadValues()` on the same `DummySensor` at the same time. The generator is `thread_local`, but `mDist` is shared, and `uniform_int_distribution::operator()` is non-const. Strictly speaking, this is a data race. |
| E8 | medium | `.cpp` `main()` line 300 / detached threads | Threads can outlive the server object | Client threads are detached and access `server->mListening`. They usually end within 500 ms after a shutdown, but nothing guarantees that they have finished before `server` is destroyed at the end of `main()`. |
| E9 | ~~low~~ ✅ **resolved** | `.cpp` constructor, `Communicate()` | ~~Copy-paste log messages~~ | They now print `"Environment_SocketServer::Environment_SocketServer()"` and `"Environment_SocketServer::Communicate()"`. |
| E10 | ~~low~~ ✅ **resolved** | `.cpp` `Communicate()` | ~~Wrong comment~~ | Changed to `// check if the message is exactly "reqAll()#"`. |
| E11 | ~~low~~ ✅ **resolved** | `.cpp`, `.h` | ~~Unused variables~~ | `clientAddr`, both unused `sensorsLen` and the member `port` (with its assignment) were removed. The local `port` in `Communicate()` is now `peerPort`, so nothing is shadowed. |
| E12 | ~~low~~ ✅ **resolved** | `.h` | ~~Naming convention~~ | `passive_socket` was renamed to `mPassiveSocket` everywhere. `port` no longer exists (E11). |
| E13 | ~~low~~ ✅ **resolved** | `DummySensor.h` | ~~Include guard is empty~~ | The empty `#ifndef/#define/#endif` block was removed. `#pragma once` is left, as in the other headers. |
| E14 | low | `.h` line 15 | `using namespace std;` in a header | It leaks `std` into every file that includes the header. The `.cpp` relies on it (`cout`, `string`). |
| E15 | low | `.cpp` line 290 | `atoi` without validation | An invalid port string becomes 0, and the server then binds to a random port. |

## Primitive_SocketServer (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| P1 | ~~high~~ ✅ **resolved** | `Primitive_SocketServer.cpp` `Communicate()` | ~~There is no `break` when the client disconnects~~ | A `break` (with a `//` comment) was added to the `rVal == 0` branch. The server now closes the socket and goes back to `accept()`. |
| P2 | ~~medium~~ ✅ **resolved** | `.cpp` constructor | ~~A failed `listen()` does not stop the server~~ | Same fix as E5. |
| P3 | low | `.cpp` `Communicate()` | Same `perror` and log-text problems as E6 and E9 | `perror("Connection closed ...")` is used without an `errno`, and the log text has the typo "Commnicate". Still open: E9 fixed only the Environment server. |
| P4 | low | `Primitive_SocketServer.h` line 19 | `using namespace std;` in a header | Same as E14. |

## Primitive_SocketServer (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PJ1 | ~~medium~~ ✅ **resolved** | `Primitive_SocketServer.java` | ~~`drop` does not drop the connection~~ | The `drop` branch now ends with `break`. The streams and the socket are closed, and the server waits for the next client. The class JavaDoc mentions `drop`. |
| PJ2 | ~~low~~ ✅ **resolved** | `main()` | ~~The `rVal == 0` branch is unreachable~~ | The branch was removed. A `//` comment explains that `read()` returns at least 1 byte or -1. |
| PJ3 | low | `main()` | No `try`/`finally` | If an `IOException` occurs, the sockets and streams are not closed. |

## Primitive_SocketClient (C++)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PC1 | ~~medium~~ ✅ **resolved** | `Primitive_SocketClient.cpp` | ~~The `inet_pton` result is only checked for `-1`~~ | A return value of 0 now prints `invalid IP address: <ip>` (no `perror`, because `errno` is not set), closes the socket and exits with `EXIT_FAILURE`. A `//` comment explains 1 / 0 / -1. |
| PC2 | ~~low~~ ✅ **resolved** | includes | ~~`<iostream>` is included twice, and `<semaphore.h>` and `<pthread.h>` are not used~~ | The duplicate and the two unused includes were removed. |

## Primitive_SocketClient (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| PCJ1 | medium | `Primitive_SocketClient.java` lines 46–47 | The client crashes when the server closes the connection | `in.read()` returns -1, and `new String(byteBuffer, 0, -1)` throws `StringIndexOutOfBoundsException`. |
| PCJ2 | low | constructor line 22 | All the work happens in the constructor | The whole communication loop runs inside the constructor. Your own comment already notes the missing try/catch. Resources are not closed if an exception occurs. |
| PCJ3 | low | `main()` | The unused variable `client` | |

## Environment_SocketClient (Java)

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| EC1 | ~~high~~ ✅ **resolved** | `envDataFabric()` | ~~The "no such datatype" check never matches, and the client crashes~~ | It now compares with `"no such datatype"` (without `#`) and returns `null`. The wrong comment was replaced. |
| EC2 | ~~medium~~ ✅ **resolved** | `reqEnvData()` | ~~Possible `NullPointerException`~~ | `setmSensorName()` is only called for a non-null result. `@return` documents the `null` case, and `main` prints `no such datatype: <type>`. |
| EC3 | ~~medium~~ ✅ **resolved** | class, constructor | ~~The socket is never closed~~ | The socket is stored in the new member `mSocket`. The class implements `AutoCloseable` with a documented `close()`, and `main` uses try-with-resources. `IEnvService` is unchanged. |
| EC4 | low | `readResponse()` line 175 | Byte-to-char cast | `(char) c` only works for ASCII. Non-ASCII characters would be decoded wrongly. This is fine for the current protocol. |
| EC5 | low | line 55 and others | `getBytes()` has no charset | It uses the platform default charset. `StandardCharsets.US_ASCII` or `UTF_8` would make it explicit. |
| EC6 | low | `main()` line 221 | The loop runs only once | `listening = false` at the end of the first iteration. This may be intentional for testing. |
| EC7 | ~~low~~ ✅ **resolved** | `main()` | ~~`main` is not indented like the other methods~~ | Re-indented. |

## IEnvService

No issues found.

## General

| # | Severity | Where | Issue | Explanation |
|---|---|---|---|---|
| G1 | low | all in-scope files | German and English comments are mixed | The guidelines recommend English. Your existing German `//` comments (e.g. "erste socket", "irgendein freier port") were left unchanged. |
| G2 | low | `// adresse in apv4`, `// irgendein freier port` | Inaccurate comments | It should be "IPv4", and the port is the one passed on the command line, not "any free port". |
