package com.example.chisu.myapplication.Follow;

/**
 * Created by jisu7 on 2018-02-20.
 */

//댓글창의 리사이클러뷰의 아이템에 출력될 데이터를 위한 클래스 정의.

public class FollowRecyclerviewItem {

    private String userIcon;
    private String username;

    //아이템의 생성자
    public FollowRecyclerviewItem(String userIcon, String username){
        this.userIcon = userIcon;
        this.username = username;
    }

    public void setUserIcon(String icon) {
        userIcon = icon ;
    }
    public void setUsername(String username1) {
        username = username1 ;
    }

    public String getUserIcon() {
        return this.userIcon ;
    }
    public String getUsername() {
        return this.username ;
    }
}
