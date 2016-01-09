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
public enum ResponseType implements Serializable {
    DOWNLOAD,
    UPLOAD,
    OK,
    REMOVE, 
    NONE, 
    FINISH_DOWNLOAD, 
    EXIT,
    REMOVE_LOCAL,
    CHECK_FILE_EXISTS,
    CONNECT_TCP,
    LOGIN,
    STOP_UPLOAD,
    ALREADY_EXISTS,
    VIEW_CHECK_FILE
}
