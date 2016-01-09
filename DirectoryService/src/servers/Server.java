/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers;

import java.util.Calendar;

/**
 *
 * @author nunol
 */
public class Server {
    private int _port;
    private String _host;
    private boolean _master;
    private int _nullheartbeats;
    private int _seconds;
    private boolean _isAvailable;
    
    public Server(String host, int port, boolean master)
    {
        this._host = host;
        this._port = port;
        this._master = master;
        this._nullheartbeats = 0;
        this._seconds = 
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY ) * 60 * 60 + 
                Calendar.getInstance().get(Calendar.MINUTE) * 60 +
                Calendar.getInstance().get(Calendar.SECOND);
        this._isAvailable = true;
    }

    /**
     * @return the _port
     */
    public int getPort() {
        return _port;
    }

    /**
     * @param _port the _port to set
     */
    public void setPort(int _port) {
        this._port = _port;
    }

    /**
     * @return the _host
     */
    public String getHost() {
        return _host;
    }

    /**
     * @param _host the _host to set
     */
    public void setHost(String _host) {
        this._host = _host;
    }

    /**
     * @return the _master
     */
    public boolean isMaster() {
        return _master;
    }

    /**
     * @param _master the _master to set
     */
    public void setMaster(boolean _master) {
        this._master = _master;
    }

    /**
     * @return the _nullheartbeats
     */
    public int getNullheartbeats() {
        return _nullheartbeats;
    }

    /**
     * @param _nullheartbeats the _nullheartbeats to set
     */
    public void setNullheartbeats(int _nullheartbeats) {
        this._nullheartbeats = _nullheartbeats;
    }

    /**
     * @return the _seconds
     */
    public int getSeconds() {
        return _seconds;
    }

    /**
     * @param _seconds the _seconds to set
     */
    public void setSeconds(int _seconds) {
        this._seconds = _seconds;
    }

    /**
     * @return the _isAvailable
     */
    public boolean setAvailable() {
        return _isAvailable;
    }

    /**
     * @param _isAvailable the _isAvailable to set
     */
    public void getAvailable(boolean _isAvailable) {
        this._isAvailable = _isAvailable;
    }
    
}
