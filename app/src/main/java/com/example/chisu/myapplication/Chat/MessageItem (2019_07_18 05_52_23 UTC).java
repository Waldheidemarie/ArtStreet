package com.example.chisu.myapplication.Chat;

/**
 * Created by jisu7 on 2018-03-08.
 */

//채팅방의 메시지 아이템을 표시하는 클래스.
public class MessageItem {

    private String fromName, message;

    //메시지가 누구의 것인지 구분하는 블리언 변수.
    private boolean isSelf;

    public MessageItem() {

    }

    public MessageItem(String fromName, String message, boolean isSelf) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

}
