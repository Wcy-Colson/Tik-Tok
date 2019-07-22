package com.example.tik_tok.bean;

import com.google.gson.annotations.SerializedName;

public class PostVideoResponse {

    @SerializedName("success")boolean success;
    @SerializedName("item")Feed item;

    public void setSuccess(boolean s){
        this.success = s;
    }

    public void setItem(Feed s){
        this.item = s;
    }

    public boolean getSuccess(){
        return success;
    }
    public Feed getItem(){
        return item;
    }

}