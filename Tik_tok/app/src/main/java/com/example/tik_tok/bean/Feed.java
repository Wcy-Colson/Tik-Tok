package com.example.tik_tok.bean;

import com.google.gson.annotations.SerializedName;

public class Feed {

    // TODO-C2 (1) Implement your Feed Bean here according to the response json

    @SerializedName("student_id")String student_id;
    @SerializedName("user_name")String user_name;
    @SerializedName("image_url")String image_url;
    @SerializedName("video_url")String video_url;

    public void setImage_url(String s){
        this.image_url = s;
    }

    public void setUser_name(String s){
        this.user_name = s;
    }

    public void setStudent_id(String s){
        this.student_id = s;
    }

    public void setVideo_url(String s){
        this.video_url = s;
    }

    public String getStudent_id(){
        return student_id;
    }

    public String getUser_name(){
        return user_name;
    }

    public String getImage_url(){
        return image_url;
    }

    public String getVideo_url(){
        return video_url;
    }
}
