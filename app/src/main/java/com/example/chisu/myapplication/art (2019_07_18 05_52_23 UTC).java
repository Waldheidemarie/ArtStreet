package com.example.chisu.myapplication;

/**
 * Created by jisu7 on 2018-01-30.
 */

//작품의 객체.
public class art {

    //Data Variables

    private int productId;

    private String title;
    private String user;
    private String userImage;
    private String desc;
    private String created;
    private String image;
    private String recom;
    private String value;
    private String commentNum;

    private String latitude;
    private String longtitude;

    public art(){

    }

    public art(int id, String title, String user, String userImage, String created, String image, String recom, String value, String commentNum) {
        this.productId = id;
        this.title = title;
        this.user = user;
        this.userImage = userImage;
        this.created = created;
        this.image = image;
        this.recom = recom;
        this.value = value;
        this.commentNum = commentNum;
    }

    //Getters and Setters
    public String getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }
    public String getUser() {
        return user;
    }
    public int getProductId() {
        return productId;
    }
    public String getLatitude() {
        return latitude;
    }
    public String getLongtitude() {
        return longtitude;
    }

    public String getUserImage() {
        return userImage;
    }
    public String getRecom() {
        return recom;
    }
    public String getDesc() {
        return desc;
    }
    public String getCreated() {
        return created;
    }
    public String getValue() {
        return value;
    }
    public String getCommentNum() {
        return commentNum;
    }


    public void setImage(String image) {
        this.image = image;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
    public void setRecom(String recom) {
        this.recom = recom;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public void setCreated(String created) {
        this.created = created;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

}
