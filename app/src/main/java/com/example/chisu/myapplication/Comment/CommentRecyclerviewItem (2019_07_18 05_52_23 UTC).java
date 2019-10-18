package com.example.chisu.myapplication.Comment;

/**
 * Created by jisu7 on 2018-02-21.
 */

//코멘트 액티비티의 리사이클러뷰에 들어가는 아이템 정의 클래스.
public class CommentRecyclerviewItem {

    private String commentUserIcon;
    private String commentUserName;
    private String commentContent;
    private String commentCreated;
    private String commentId;

    //아이템의 생성자
    public CommentRecyclerviewItem(String commentUserIcon, String username, String created, String content, String id){
        this.commentUserIcon = commentUserIcon;
        this.commentUserName = username;
        this.commentCreated = created;
        this.commentContent = content;
        this.commentId = id;
    }

    public void setCommentUserIcon(String icon) {
        commentUserIcon = icon ;
    }
    public void setCommentUsername(String username1) {
        commentUserName = username1 ;
    }
    public void setCommentContent(String content) {
        commentContent = content ;
    }
    public void setCommentCreated(String created) {
        commentCreated = created ;
    }


    public String getCommentUserIcon() {
        return this.commentUserIcon;
    }
    public String getCommentUsername() {
        return this.commentUserName ;
    }
    public String getCommentContent() {
        return this.commentContent;
    }
    public String getCommentCreated() {
        return this.commentCreated ;
    }
    public String getCommentId() {
        return this.commentId ;
    }


}
