package com.observeai.platform.realtime.neutrino.data.nice;



public class CxOneWebSocketMessage {
    /*Type of command*/
    public String command;

    /*Type of message returned*/
    public String messageType;

    /*Text message content*/
    public String message;

    public CxOneWebSocketMessage(String command, String messageType, String message){
        this.command = command;
        this.messageType = messageType;
        this.message = message;
    }
}

