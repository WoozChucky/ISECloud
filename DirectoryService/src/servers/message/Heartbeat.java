/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.message;

import java.io.Serializable;

/**
 *
 * @author nunol
 */
public class Heartbeat implements Serializable {
    
    private static final long serialVersionUID = 27015L;
    
    private int _port;
    private String _host;
    private String _msg;
    private boolean _isMaster;
    
    public Heartbeat(String host, int port, boolean master)
    {
        this._host = host;
        this._port = port;
        this._msg = "Hey " + host + ":" + port + " is alive.";
        this._isMaster = master;
    }

    /**
     * @return the _port
     */
    public int getPort() {
        return _port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this._port = port;
    }

    /**
     * @return the _host
     */
    public String getHost() {
        return _host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this._host = host;
    }

    /**
     * @return the _msg
     */
    public String getMsg() {
        return _msg;
    }
    
    
}
