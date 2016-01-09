/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servers.messages;

/**
 *
 * @author nunol
 */
public enum ResponseType {
    DOWNLOAD,
    DOWNLOAD_READ,
    UPLOAD,
    OK,
    REMOVE, 
    NONE, 
    FINISH_DOWNLOAD, 
    EXIT,
    REMOVE_LOCAL,
    CHECK_FILE_EXISTS
}
