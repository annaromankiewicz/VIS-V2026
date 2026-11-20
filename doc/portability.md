# Portability: Unix-only Code and What Windows / macOS Need Instead

This is a report only. **No source code was changed.**
Scope: Primitive_SocketServer (C++ and Java), Environment_SocketServer (C++), Primitive_SocketClient (C++ and Java), Environment_SocketClient (Java), `IEnvService`.
Line numbers refer to the current state of the files.

## 1. Summary

macOS is a Unix system (POSIX/BSD), so the C++ code already compiles and runs there. The only difference from Linux that matters is `SIGPIPE` (§3).
The Java code is portable.

On **Windows**, it depends on the C++ toolchain. Gradle supports three on Windows: Visual C++, GCC via **Cygwin**, and GCC via MinGW ([Gradle docs](https://docs.gradle.org/current/userguide/building_cpp_projects.html), "Supported tool chain").

| Windows toolchain | Does the current code work? | Why |
|---|---|---|
| **Cygwin GCC** (most likely the lecturer's setup, see below) | ✅ **yes, unchanged** | Cygwin provides the POSIX API (`sys/socket.h`, `unistd.h`, `poll.h`, `pthread.h`) on top of Winsock. It initializes Winsock itself: *"Cygwin has to perform this initialization on the fly, as soon as the first socket-related function is called by the application"* ([Cygwin User's Guide, Sockets](https://cygwin.com/cygwin-ug-net/highlights.html)). So **no `WSAStartup`/`WSACleanup`** is needed, and `close()`, `poll()`, `pthread_*` and `perror()`/`errno` behave like on Unix. |
| WSL (Windows Subsystem for Linux) | ✅ yes, unchanged | It is a real Linux, so everything in §3 (Linux) applies. |
| Visual C++ (MSVC) or MinGW (native Windows) | ❌ does not compile | These use plain *Winsock*: there is no `sys/socket.h`, `poll.h` or `pthread.h` (MinGW has `pthread.h`/`unistd.h` but no socket headers). §2 lists what would have to change. |

**Why Cygwin is the likely lecturer setup:** the lecturer's own templates (`Demo_SocketServer.cpp`, `Demo_SocketClient.cpp`, `Hello/main.cpp`) include `<sys/socket.h>`, `<unistd.h>`, `<pthread.h>` and `<semaphore.h>`. They call `close()` and `perror()` and no `WSAStartup()`. That code only compiles on Windows with a POSIX layer such as Cygwin (or in WSL). The lecture slides also present the Unix variants (`pthread_*`, `perror`, `close`) and only mention Winsock as an alternative (e.g. `closesocket` *"or"* `close`, VL-02 slide 47).

**Conclusion:** if the code has to work in the course setup (Linux, macOS, Windows with Cygwin/WSL), **nothing has to be changed**, apart from SIGPIPE (§3, which also concerns macOS/Linux). §2 only matters if the code should be built natively with Visual C++ or MinGW.

| Component | Linux | macOS | Windows + Cygwin / WSL | Windows + MSVC / MinGW |
|---|---|---|---|---|
| Java: Primitive_SocketServer, Primitive_SocketClient, Environment_SocketClient, IEnvService | ✅ | ✅ | ✅ (charset note, §4) | ✅ (charset note, §4) |
| C++: Primitive_SocketServer, Primitive_SocketClient | ✅ | ✅ (SIGPIPE, §3) | ✅ | ❌ see §2 |
| C++: Environment_SocketServer | ✅ | ✅ (SIGPIPE, §3) | ✅ | ❌ see §2 |

## 1a. Target Platforms: Linux, macOS, Windows with Cygwin

The code must work on Linux, macOS and Windows with **Cygwin**. Every Unix call the C++ code uses, checked against Cygwin:

| Used in the code | Where | Works under Cygwin? | Note |
|---|---|---|---|
| `<sys/socket.h>`, `<netinet/in.h>`, `<arpa/inet.h>`, `socket`, `bind`, `listen`, `accept`, `connect`, `send`, `recv`, `getpeername`, `inet_ntop`, `inet_pton`, `htons`/`ntohs` | all C++ files | ✅ | Cygwin maps them to Winsock and initializes Winsock itself, so no `WSAStartup` is needed. |
| `<unistd.h>` → `close()` on sockets | ESS.cpp, PSS.cpp, PSC.cpp | ✅ | Under Cygwin, a socket is a normal file descriptor. |
| `<poll.h>` → `poll()` | ESS.cpp `Accept()`, `Communicate()` | ✅ | |
| `<pthread.h>` → `pthread_create`, `pthread_detach` | ESS.cpp `Accept()` | ✅ | |
| `<semaphore.h>` (included, not used) | PSS.h, PSC.cpp | ✅ | Exists in Cygwin. It could still be removed because it is unused. |
| `perror`, `errno`, `EINTR`, `strerror` | all error branches | ✅ | They behave as on Unix. |
| `std::atomic`, `thread_local`, `std::mt19937`, `std::random_device` | ESS.h, DummySensor.h | ✅ | Supported by Cygwin GCC (C++11). |
| `SIGPIPE` on `send()` to a closed client | all `send()` calls | ⚠️ same as Linux/macOS | The process can be killed. This is not Cygwin-specific; the fix is `signal(SIGPIPE, SIG_IGN);` in `main` (M1), which also works under Cygwin. |
| `exit`/`atoi`/`EXIT_FAILURE` (`<cstdlib>`), `perror` (`<cstdio>`), `time` (`<ctime>`) used **without their own include** | ESS.cpp, PSS.cpp, PSC.cpp | ✅ **fixed**: `<cstdio>`, `<cstdlib>` (and `<ctime>` in ESS.cpp) are now included explicitly | Previously: on macOS (clang/libc++), these came in indirectly through `<iostream>`/`<string>`. Cygwin uses GCC/libstdc++, where the indirect includes differ. It usually works, but adding `#include <cstdlib>`, `<cstdio>` and `<ctime>` explicitly avoids "not declared in this scope" errors with a different compiler. |
| Gradle build/run | `build.gradle.kts` | ✅ | `machines.windows` + `.exe` are already handled. Gradle finds Cygwin GCC only if the Cygwin `bin` folder is on `PATH`; the same is true when running the `.exe`, which needs `cygwin1.dll`. |

**Result:** with Cygwin, no Winsock-specific changes (§2) are needed. Recommended for all three platforms: the `SIGPIPE` line (M1) and the three explicit standard includes.
I could not test this on a real Cygwin installation; the table is based on the Cygwin and Gradle documentation and a review of the code.

## 2. C++: Changes Needed Only for Native Windows (Visual C++ / MinGW, not Cygwin)

*Not needed for the target platforms above; kept for reference only.*

Everything in this section is **not** needed with Cygwin or WSL.

File abbreviations: **ESS.cpp/.h** = `socket/cpp/Environment_SocketServer/src/main/cpp|headers/Environment_SocketServer.*`, **PSS.cpp/.h** = `socket/cpp/Primitive_SocketServer/src/main/cpp|headers/Primitive_SocketServer.*`, **PSC.cpp** = `socket/cpp/Primitive_SocketClient/src/main/cpp/Primitive_SocketClient.cpp`.

| # | Unix (current code) | Where | Needed on Windows |
|---|---|---|---|
| W1 | Socket headers `<sys/socket.h>`, `<netinet/in.h>`, `<arpa/inet.h>` | ESS.h:10, ESS.cpp:8, 10, PSS.h:8, 9, 12, PSC.cpp:7, 8, 11 | `#include <winsock2.h>` and `#include <ws2tcpip.h>` (the latter provides `inet_pton`, `inet_ntop` and `socklen_t`). `winsock2.h` must come **before** `<windows.h>` if both are used. |
| W2 | Sockets can be used right away (on Unix and under Cygwin, which initializes Winsock itself) | start of every `main` | **Native Winsock only:** `WSADATA wsa; WSAStartup(MAKEWORD(2,2), &wsa);` before the first socket call, and `WSACleanup();` at the end. Microsoft: *"The WSAStartup function must be the first Windows Sockets function called by an application or DLL"* ([WSAStartup docs](https://learn.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-wsastartup)). Without it, socket calls fail (error `WSANOTINITIALISED`). This is why the lecturer's Cygwin-based code does not need it. |
| W3 | `<unistd.h>` → `close(socket)` | ESS.cpp:9 (include), 62, 106, 115, 266; PSS.h:11 (include), PSS.cpp:40, 114; PSC.cpp:10 (include), 96 | `closesocket(socket)`. On Windows, `close()` only works for C runtime file handles, not for sockets. |
| W4 | A socket is an `int`, and errors are checked with `== -1` | `passive_socket` in ESS.h:63, PSS.h:45; `commSocket` in ESS.cpp:92, PSS.cpp:50, PSC.cpp:36; checks at ESS.cpp:30, 93, PSS.cpp:15, 51, PSC.cpp:37 | The type is `SOCKET` (an *unsigned* 64-bit value on x64). `socket()`/`accept()` return `INVALID_SOCKET` on error, and the other calls return `SOCKET_ERROR`. Comparing with `-1` happens to work for `SOCKET_ERROR`, but storing a `SOCKET` in an `int` truncates it. |
| W5 | `perror()`, `errno`, `EINTR` | every error branch, e.g. ESS.cpp:31, 41, 81, 185, 258, PSS.cpp:26, 82, PSC.cpp:38, 54; `errno == EINTR` at ESS.cpp:80, 177 | `perror()` itself **exists and compiles on every Windows toolchain**, and your message text is always printed. Under Cygwin, socket errors are reported through `errno` like on Unix, so `perror` is fully correct there. **Only with native Winsock** (MSVC/MinGW) is the appended error description not the socket error: *"Error codes set by Windows Sockets are not made available through the errno variable"* ([Microsoft docs](https://learn.microsoft.com/en-us/windows/win32/winsock/error-codes-errno-h-errno-and-wsagetlasterror-2)). There, print `WSAGetLastError()` instead to see the real cause. `EINTR` becomes `WSAEINTR`. |
| W6 | `<poll.h>`, `pollfd`, `poll(&pfd, 1, 500)` | ESS.cpp:6, 75–78, 170–173 | `WSAPoll(&pfd, 1, 500)` with `WSAPOLLFD` (declared in `winsock2.h`, Windows Vista+). `POLLIN` works the same. Alternative: `select()`. |
| W7 | `<pthread.h>`, `pthread_t`, `pthread_create`, `pthread_detach` | ESS.cpp:12, 102–110; the `static void* ClientCommunication(void*)` signature in ESS.h | **Recommended: `std::thread`** (`<thread>`, C++11), which works on Linux, macOS and Windows: `std::thread(&Environment_SocketServer::ClientCommunication, this, commSocket).detach();`. Then the `void*` signature, the `socketParam` struct and the `new`/`delete` are no longer needed. Windows-only alternative: `_beginthreadex` / `CreateThread`. |
| W8 | `strerror(rVal)` for the `pthread_create` error code | ESS.cpp:105 | Not needed with `std::thread`, which throws `std::system_error` if it cannot start a thread. |
| W9 | `<semaphore.h>`, `<pthread.h>`, `<string.h>` included but not used | PSS.h:13–15, PSC.cpp:12–14 | Remove them. `<semaphore.h>` and `<pthread.h>` do not exist with MSVC, so these unused includes alone already break the Windows build. |
| W10 | `send`/`recv` take a `size_t` length and return `ssize_t` | ESS.cpp:183, 218, 238, 252; PSS.cpp:80, 104; PSC.cpp:76, 83 | Windows uses `int` for both. `reply.length()` must be cast: `send(s, reply.c_str(), (int)reply.length(), 0)`. Otherwise MSVC gives a conversion warning. |
| W11 | `sockaddr`, `sockaddr_in`, `socklen_t`, `inet_ntop`, `inet_pton`, `htons`, `INADDR_ANY` | ESS.h:77, ESS.cpp:35, 91, 151–159; PSS.cpp:20, 63–71; PSC.cpp:43–46 | Same names in `winsock2.h` / `ws2tcpip.h`. **Nothing to change** except the includes (W1). |

## 3. macOS vs. Linux (both Unix)

| # | Topic | Where | Explanation and what is needed |
|---|---|---|---|
| M1 | **SIGPIPE** | every `send()`: ESS.cpp:218, 238, 252, PSS.cpp:104, PSC.cpp:76 | On Linux **and** macOS, a `send()` to a socket the other side has already closed raises the signal `SIGPIPE`. By default, that **kills the whole server**, including all other client threads, without any error message. Linux: pass `MSG_NOSIGNAL` as the last `send` argument. **macOS has no `MSG_NOSIGNAL`**; use `int on = 1; setsockopt(sock, SOL_SOCKET, SO_NOSIGPIPE, &on, sizeof(on));` per socket. The simplest solution for both is `signal(SIGPIPE, SIG_IGN);` once at the start of `main`, after which `send` just returns -1. Windows has no SIGPIPE. |
| M2 | Port still in use after a restart | before `::bind` in ESS.cpp:39, PSS.cpp:24 | After the server stops, Linux and macOS keep the port in the TIME_WAIT state for up to about 1–2 minutes, so a quick restart fails with "bind failed: Address already in use". Fix: `int on = 1; setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));` before `bind`. **Careful on Windows:** `SO_REUSEADDR` means something else there (another process can take over the port). Use `SO_EXCLUSIVEADDRUSE` instead. |

Everything else (headers, `poll`, pthreads, `close`) is identical on Linux and macOS.

## 4. Java (already portable, small notes)

`java.net.Socket` and `ServerSocket` behave the same on all three systems. No changes are required for it to run.

| # | Topic | Where | Explanation and what is needed |
|---|---|---|---|
| J1 | No charset for `getBytes()` / `new String(bytes, …)` | `Environment_SocketClient.java:55, 76, 96`; `Primitive_SocketClient.java:42, 47`; `Primitive_SocketServer.java:52, 62` | These use the platform default charset. Before JDK 18 that was Cp1252 on Windows and UTF-8 on macOS/Linux. This is harmless for the pure-ASCII protocol, but making it explicit avoids surprises: `getBytes(StandardCharsets.UTF_8)` and `new String(buf, 0, n, StandardCharsets.UTF_8)`. |
| J2 | Non-ASCII characters `´` in the source code | `Primitive_SocketServer.java:54, 57` | If the Windows build does not compile with UTF-8, the characters appear garbled. In Gradle: `tasks.withType<JavaCompile> { options.encoding = "UTF-8" }`. Alternatively, use a normal `'`. |

## 5. Build (Gradle `cpp-application`)

| # | Topic | Where | What is needed |
|---|---|---|---|
| B1 | Winsock library (native Windows only, not Cygwin) | `build.gradle.kts` of every C++ project | MSVC/MinGW need `Ws2_32.lib` linked, for example: `tasks.withType<LinkExecutable>().configureEach { if (Os.isFamily(Os.FAMILY_WINDOWS)) linkerArgs.add("Ws2_32.lib") }` with MSVC, or `"-lws2_32"` with MinGW/GCC. Without it, you get linker errors such as `unresolved external symbol __imp_socket`. |
| B2 | pthread on Linux | `build.gradle.kts` (Environment_SocketServer) | Linux may need `-pthread` for compiling and linking. macOS links it automatically, and with `std::thread` (W7) only `-pthread` on Linux remains. |
| B3 | `run` / `kill` tasks | `build.gradle.kts` | These already handle Windows (`.exe`, `netstat` / `taskkill`). **No change needed.** |

## 6. Recommended Approach (not implemented)

**For the course setup (Linux / macOS / Windows with Cygwin or WSL):** keep the current POSIX code and only add `signal(SIGPIPE, SIG_IGN);` at the start of `main` (M1).

**Only if native Visual C++ / MinGW support is wanted:**

1. Create one small header, e.g. `SocketCompat.h`, that hides the differences with `#ifdef _WIN32`:
   ```cpp
   #ifdef _WIN32
     #include <winsock2.h>
     #include <ws2tcpip.h>
     using socket_t = SOCKET;
     inline int  closeSocket(socket_t s)            { return closesocket(s); }
     inline int  lastSocketError()                  { return WSAGetLastError(); }
     inline int  pollSocket(WSAPOLLFD* p, int n, int ms) { return WSAPoll(p, n, ms); }
     struct NetInit { NetInit() { WSADATA w; WSAStartup(MAKEWORD(2,2), &w); } ~NetInit() { WSACleanup(); } };
     const socket_t INVALID_SOCK = INVALID_SOCKET;
   #else
     #include <sys/socket.h>
     #include <netinet/in.h>
     #include <arpa/inet.h>
     #include <unistd.h>
     #include <poll.h>
     #include <cerrno>
     #include <csignal>
     using socket_t = int;
     inline int  closeSocket(socket_t s)            { return close(s); }
     inline int  lastSocketError()                  { return errno; }
     inline int  pollSocket(pollfd* p, int n, int ms) { return poll(p, n, ms); }
     struct NetInit { NetInit() { signal(SIGPIPE, SIG_IGN); } };   // see M1
     const socket_t INVALID_SOCK = -1;
   #endif
   ```
2. In each `main`, create `NetInit net;` as the first line.
3. Replace `int` sockets with `socket_t`, `close` with `closeSocket`, `poll` with `pollSocket`, and `-1` checks of `socket()`/`accept()` with `INVALID_SOCK`.
4. Replace `pthread_*` with `std::thread(...).detach()` (W7).
5. Remove the unused includes (W9) and add `Ws2_32` to the build (B1).

After that, the server and client code itself contains no `#ifdef` and compiles on Linux, macOS and Windows.
