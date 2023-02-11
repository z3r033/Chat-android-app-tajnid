package com.zerophi.tajn.models;

public class modelmessages {

   String message , date ;
   int sender_id , recipient_id;
    int message_id;

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getRecipient_id() {
        return recipient_id;
    }

    public void setRecipient_id(int recipient_id) {
        this.recipient_id = recipient_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
