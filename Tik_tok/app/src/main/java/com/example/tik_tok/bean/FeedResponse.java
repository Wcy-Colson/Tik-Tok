package com.example.tik_tok.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedResponse {

    @SerializedName("success") boolean success;
    @SerializedName("feeds")  List<Feed> feeds;


    public void setSuccess(boolean s){
        success =s;
    }

    public void setFeeds(List<Feed> s){
        feeds =s ;
    }

    public boolean getSuccess(){
        return success;
    }

    public List<Feed> getFeeds(){
        return feeds;
    }

}
