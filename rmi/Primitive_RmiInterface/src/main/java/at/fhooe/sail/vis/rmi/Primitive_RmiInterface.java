package at.fhooe.sail.vis.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Primitive_RmiInterface extends Remote {

    String KEY = "Primitive_RmiServer-KEY";     // broker and client need to know the

    public String requestData() throws RemoteException;
}