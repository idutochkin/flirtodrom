package com.example.run18.withtoolbar;

public class FeedUser {

    private String user_id;
    private String name;
    private String like_status;
    private String like_status_too;
    private String picture;
    private Integer age;

    public String getUserId() {
        return user_id;
    }
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLikeStatus() {
        return like_status;
    }
    public void setLikeStatus(String like_status) {
        this.like_status = like_status;
    }

    public String getLikeStatusToo() {
        return like_status_too;
    }
    public void setLikeStatusToo(String like_status_too) {
        this.like_status_too = like_status_too;
    }

    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
}