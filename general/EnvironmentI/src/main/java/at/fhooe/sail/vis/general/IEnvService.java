package at.fhooe.sail.vis.general;


import java.io.IOException;

public interface IEnvService {
    public String[] reqEnvDataTypes() throws IOException;
    public EnvData reqEnvData(String _type) throws IOException;
    public EnvData[] reqAll() throws IOException;
}
