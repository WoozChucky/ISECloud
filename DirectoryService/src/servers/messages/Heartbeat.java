/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.messages;

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
    private boolean _isAvailable;
    
    public Heartbeat(String host, int port, boolean master, boolean available)
    {
        this._host = host;
        this._port = port;
        this._msg = "Hey " + host + ":" + port + " is alive.";
        this._isMaster = master;
        this._isAvailable = available;
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

    /**
     * @return the _isMaster
     */
    public boolean IsMaster() {
        return _isMaster;
    }

    /**
     * @param _isMaster the _isMaster to set
     */
    public void setMaster(boolean _isMaster) {
        this._isMaster = _isMaster;
    }

    /**
     * @return the _isAvailable
     */
    public boolean IsAvailable() {
        return _isAvailable;
    }

    /**
     * @param _isAvailable the _isAvailable to set
     */
    public void setAvailable(boolean _isAvailable) {
        this._isAvailable = _isAvailable;
    }
    
    
}
