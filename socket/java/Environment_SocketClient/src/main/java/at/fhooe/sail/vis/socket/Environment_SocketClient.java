package at.fhooe.sail.vis.socket;

import at.fhooe.sail.vis.general.EnvData;
import at.fhooe.sail.vis.general.IEnvService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * @brief TCP client for the C++ Environment_SocketServer.
 *
 * Implements IEnvService by sending text requests terminated with '#'
 * and parsing the server's replies into EnvData objects.
 */
public class Environment_SocketClient implements IEnvService {

    /**
     * stream for reading the server's replies
     */
    private InputStream in;
    /**
     * stream for sending requests to the server
     */
    private OutputStream out;


    /**
     * @brief Connects to the environment server.
     * @param _ip IP address or host name of the server
     * @param _port port the server is reachable on
     * @throws IOException if the connection cannot be established
     */
    public Environment_SocketClient(String _ip, int _port) throws IOException {

        // communication with TCP C++ server -> TCP/IPv6

        Socket activeSocket = new Socket(_ip, _port);
        in = activeSocket.getInputStream();
        out = activeSocket.getOutputStream();

    }


    /**
     * @brief Sends "reqEnvDataTypes()#" and parses the list of sensor names.
     * @return sensor names, e.g. {"light", "noise", "air"}
     * @throws IOException if sending or receiving fails
     */
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

    /**
     * @brief Sends "reqEnvData(<type>)#" and parses the reply.
     * @param _type name of the sensor
     * @return measurement of the sensor with name and timestamp set
     * @throws IOException if sending or receiving fails
     */
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

    /**
     * @brief Sends "reqAll()#" and parses the values of all sensors.
     * @return one EnvData per sensor, all with the timestamp of the reply
     * @throws IOException if sending or receiving fails
     */
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


    /**
     * @brief Creates an EnvData object from a reqEnvData reply.
     * @param _response reply without '#', format "<timestamp>|<v1>;<v2>;..."
     * @return EnvData with timestamp and values (sensor name is null),
     *         or null if the server answered "no such datatype#".
     */
    private EnvData envDataFabric(String _response) {
        if (_response.equalsIgnoreCase("no such datatype#")) {    // is never happening - parameter is from answer of server
            return null;
        }
        // 123456789|123;235;23
        String[] responseSplited = _response.split("[|;]"); // {timestamp, value, value, value}
        int[] values = new int[responseSplited.length-1]; // {value, value, value}
        long timestamp = Long.parseLong(responseSplited[0]);

        for (int i = 1; i< responseSplited.length; i++) {
            values[i-1] = Integer.parseInt(responseSplited[i]);
        }
        return new EnvData(timestamp, null, values);
    }

    /**
     * @brief Creates an EnvData object from one part of a reqAll reply.
     * @param response one sensor part, format "<name>;<v1>;<v2>;..."
     * @return EnvData with name and values (timestamp is 0 and set by the caller)
     */
    private EnvData envDataFabricAll(String response) {
        String[] responseSplited = response.split("[;]"); // {name, value, value, value}
        int[] values = new int[responseSplited.length-1];
        for (int i = 1; i< responseSplited.length; i++) {
            values[i-1] = Integer.parseInt(responseSplited[i]);
        }
        return new EnvData(0, responseSplited[0], values);
    }


    /**
     * @brief Removes the last character (the '#' terminator) of a reply.
     * @param response complete reply as returned by readResponse()
     * @return reply without the terminating '#'
     */
    private String cleanResponse(String response) {
        StringBuilder rsp = new StringBuilder(response);
        rsp.deleteCharAt(rsp.length()-1);       // delete hash
        return rsp.toString();
    }


    /**
     * @brief Reads one reply from the server, byte by byte, until '#'.
     * @return complete reply including the terminating '#'
     * @throws IOException if the server closes the connection before '#' arrives
     */
    private String readResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);        // bytes -> chars; better than bytebuffer because bytebuffer can be full before message is sent completely
            if (c == '#') return sb.toString();     // last character of message
        }
        throw new IOException("Connection closed by server before '#'");
    }



/**
 * @brief Connects to the server and prints all sensor data once.
 * @param _argv [0] IP address of the server, [1] port of the server
 * @throws IOException if the communication with the server fails
 */
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
