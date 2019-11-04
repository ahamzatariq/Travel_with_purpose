package com.example.travelwithpurpose.models;

import com.example.travelwithpurpose.helpers.BlogPostId;

import java.sql.Timestamp;
import java.util.Date;

public class BlogPost extends com.example.travelwithpurpose.helpers.BlogPostId {

    public String desc;
    public String user_id;
    public String image_url;
    public String thumb;

    public String imageURI;

    public Date timestamp;

    public BlogPost(){

    }

    public BlogPost(String desc, String user_id, String image_url, String thumb, Date timestamp, String imageURI) {
        this.desc = desc;
        this.user_id = user_id;
        this.image_url = image_url;
        this.thumb = thumb;
        this.timestamp = timestamp;
        this.imageURI = imageURI;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDesc() {
        return desc;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
