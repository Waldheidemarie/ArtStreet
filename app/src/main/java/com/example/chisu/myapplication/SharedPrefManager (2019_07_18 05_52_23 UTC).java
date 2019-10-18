package com.example.chisu.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *  클라이언트 차원에서 유저의 로그인 세션을 유지하기 위해 sharedpreference를 차용했다.
 *  sharedpreference 안에는 로그인한 유저의 정보를 담는다.
 *  sharedpreference 관련한 데이터를 한번에 처리하기 위해 이 클래스를 만들었다.
 */

public class SharedPrefManager {

    //저장할 데이터들의 형태 정의(보통 스트링이 편함)
    //sharedpreference의 이름도 아예 정의. 하나만 쓰기 때문에 일일이 입력하는 것보다 이게 낫다.
    public static final String SHARED_PREF_NAME = "simplifiedcodingsharedpref";
    public static final String KEY_USERNAME = "keyusername";
    private static final String KEY_EMAIL = "keyemail";
    private static final String KEY_GENDER = "keygender";
    private static final String KEY_ID = "keyid";

    // 클래스, Context 객체 생성
    private static SharedPrefManager mInstance;
     static Context mCtx;

    //생성자
    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    // 예외처리 메소드(클래스가 없을 경우) 정의
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) { //sharedpreferencemanager 객체가 null이라면 새로 생성한다.
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //유저 로그인을 위한 메소드.
    //이 메소드는 sharedpreference에 유저의 데이터를 저장한다.
    public void userLogin(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_GENDER, user.getGender());
        editor.apply();
    }

    //이 메소드는 유저가 이미 로그인했는지 아닌지 체크한다.
    //sp에 들어있는 username이 null인지 아닌지 체크해서 true 혹은 false를 리턴한다.
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //sp의 username이 null이면(비로그인) false를 리턴하고, null이 아니면(로그인) true를 리턴한다.
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    //이 메소드는 로그인한 유저에게 실행된다.
    //sp에 저장되어 있던 정보를 꺼내는 메소드.
    //this method will give the logged in user
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new User(
                sharedPreferences.getInt(KEY_ID, -1),
                sharedPreferences.getString(KEY_USERNAME, null),
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_GENDER, null)
        );
    }

    //이 메소드는 유저가 로그아웃할 때 사용하는 메소드다.
    //로그아웃을 하므로 sp에 있던 정보를 싹 지우고 저장한다.
    //this method will logout the user
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
