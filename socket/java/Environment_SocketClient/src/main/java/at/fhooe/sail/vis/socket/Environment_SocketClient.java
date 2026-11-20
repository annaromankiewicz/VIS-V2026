package at.fhooe.sail.vis.socket;

import at.fhooe.sail.vis.general.EnvData;
import at.fhooe.sail.vis.general.IEnvService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;


public class Environment_SocketClient implements IEnvService {

    byte[] byteBuffer = new byte[1024];
    InputStream in;
    OutputStream out;


    public Environment_SocketClient(String _ip, int _port) throws IOException {

        // communication with TCP C++ server -> TCP/IPv6

        Socket activeSocket = new Socket(_ip, _port);
        in = activeSocket.getInputStream();
        out = activeSocket.getOutputStream();

    }


    @Override
    public String[] reqEnvDataTypes() throws IOException {
        // send "reqEnvDataTypes()#"
        out.write("reqEnvDataTypes()#".getBytes());
        out.flush();

        String response = readResponse();

        // light;noise;air#
        response = cleanResponse(response);
        // light;noise;air
        String[] sensors = response.split(";"); // message (light;noise;air#) -> single elements saved in array
        return sensors;
    }

    @Override
    public EnvData reqEnvData(String _type) throws IOException {
        // reqEnvData(<datatype>)#
        out.write(("reqEnvData("+ _type +")#").getBytes());
        out.flush();

        String response = readResponse();
        // 123456789|123;235;23#
        response = cleanResponse(response);
        // 123456789|123;235;23

        EnvData envData = envDataFabric(response);
        envData.setmSensorName(_type);
        return envData;
    }

    @Override
    public EnvData[] reqAll() throws IOException {
        out.write("reqAll()#".getBytes());
        out.flush();

        String response = readResponse();
        // 123456789|light;123|noise;42|air … #
        response = cleanResponse(response);

        String[] responseSplited = response.split("[|]");   // {timestamp, light;123, noise;42, air …}
        EnvData[] data = new EnvData[responseSplited.length-1];
        long timestamp = Long.parseLong(responseSplited[0]);

        for (int i = 1; i < responseSplited.length; i++) {
            data[i-1] = envDataFabricAll(responseSplited[i]);
            data[i-1].setmTimestamp(timestamp);
        }
        return data;

        // call method - EnvData fabric

    }


    private EnvData envDataFabric(String _response) {
        // 123456789|123;235;23
        String[] responseSplited = _response.split("[|;]"); // {timestamp, value, value, value}
        int[] values = new int[responseSplited.length-1]; // {value, value, value}
        long timestamp = Long.parseLong(responseSplited[0]);

        for (int i = 1; i< responseSplited.length; i++) {
            values[i-1] = Integer.parseInt(responseSplited[i]);
        }
        return new EnvData(timestamp, null, values);
    }

    private EnvData envDataFabricAll(String response) {
        String[] responseSplited = response.split("[|;]"); // {name, value, value, value}
        int[] values = new int[responseSplited.length-1];
        for (int i = 1; i< responseSplited.length; i++) {
            values[i-1] = Integer.parseInt(responseSplited[i]);
        }
        return new EnvData(0, responseSplited[0], values);
    }


    private String cleanResponse(String response) {
        StringBuilder rsp = new StringBuilder(response);
        rsp.deleteCharAt(rsp.length()-1);       // delete hash
        return rsp.toString();
    }


    public String readResponse() throws IOException {
        byte[] byteBuffer = new byte[1024];
        int rVal = in.read(byteBuffer);             // check rVal ?
            return new String(byteBuffer, 0, rVal);
    }



public static void main(String[] _argv) throws IOException {

    System.out.println("Starting Environment_SocketClient ...");

    if (_argv.length != 2) {
        System.out.println("Usage Environment_SocketClient <IP> <PORT>");
        return;
    }

    int port = Integer.parseInt(_argv[1]);
    IEnvService service = new Environment_SocketClient(_argv[0], port);
    boolean listening = true;

    System.out.println("Start while....");

    while (listening) {
        String[] sensors = service.reqEnvDataTypes();       // {light, noise, air}
        for (String sensor : sensors) {                     // iterate and request data of reqEnvData(<datatype>)#
            EnvData dataO = service.reqEnvData(sensor);
            System.out.print(dataO);
            System.out.println();
        } // for sensors
        System.out.println();
        System.out.println();
        EnvData[] dataOs = service.reqAll();
        for (EnvData dataO : dataOs) {
            System.out.println(dataO);
        } // for data
        try {
            Thread.sleep(1000);
        } catch (Exception _e) {
            _e.printStackTrace();
        }
        listening = false;
    } // while true

    System.out.println("End while....");
}



}
