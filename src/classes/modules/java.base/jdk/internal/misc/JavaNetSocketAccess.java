package jdk.internal.misc;

import java.net.SocketImpl;
import java.net.ServerSocket;

public interface JavaNetSocketAccess {
    SocketImpl newSocketImpl(Class<? extends SocketImpl> clazz);
    ServerSocket newServerSocket(SocketImpl impl);
}
