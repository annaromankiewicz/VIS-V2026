package at.fhooe.sail.vis.general;


import java.io.IOException;

/**
 * @brief Interface for requesting environment data from a server.
 *
 * The implementation decides how the data is transported (e.g. TCP sockets).
 */
public interface IEnvService {
    /**
     * @brief Requests the names of all sensors available on the server.
     * @return array with the sensor names, e.g. {"light", "noise", "air"}
     * @throws IOException if the communication with the server fails
     */
    public String[] reqEnvDataTypes() throws IOException;
    /**
     * @brief Requests the current values of one sensor.
     * @param _type name of the sensor, as returned by reqEnvDataTypes()
     * @return measurement of the requested sensor
     * @throws IOException if the communication with the server fails
     */
    public EnvData reqEnvData(String _type) throws IOException;
    /**
     * @brief Requests the current values of all sensors at once.
     * @return one measurement per sensor, all with the same timestamp
     * @throws IOException if the communication with the server fails
     */
    public EnvData[] reqAll() throws IOException;
}
