package com.example.chisu.myapplication;

/**
 * 코드가 재사용될 수 있도록 잘 조직하는 것은 매우 중요하다.
 * 그것이 우리가 다른 직무에 다른 클래스를 만드는 이유다.
 * 이 클래스는 유저를 위한 클래스다.
 * 이 클래스는 유저의 특성과 관련한 생성자와 getter를 포함한다.
 */

public class User {

    //db의 user2 테이블과 호환될 정보들
    private int id;
    private String username, email, gender;

    //생성자
    public User(int id, String username, String email, String gender) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.gender = gender;
    }

    //getter들
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

}
