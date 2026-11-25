package at.fhooe.sail.vis.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Primitive_RmiServer extends UnicastRemoteObject implements Primitive_RmiInterface {

    public static final String KEY = "Primitive_RmiServer-KEY";


    protected Primitive_RmiServer() throws RemoteException {
    }


    @Override
    public String requestData() throws RemoteException {
        return String.valueOf(System.currentTimeMillis());
    }


    public static void main(String[] _argv) {

        try {
            Primitive_RmiServer server = new Primitive_RmiServer();
            System.out.println("Starting Primitive_RmiServer ...");
            Registry reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT); // default port 1099 for rmi comm
            reg.rebind(KEY, server);      // name better not hardcoded

        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Startup of Primitive_RmiServer failed");
        }







    }

}
