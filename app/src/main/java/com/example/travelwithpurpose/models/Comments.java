package com.example.travelwithpurpose.models;

import com.example.travelwithpurpose.helpers.CommentId;

import java.util.Date;

public class Comments extends com.example.travelwithpurpose.helpers.CommentId {

    private String message, userID;
    private Date timestamp;

    public Comments(){

    }

    public Comments(String message, String userID, Date timestamp) {
        this.message = message;
        this.userID = userID;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getUserID() {
        return userID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
