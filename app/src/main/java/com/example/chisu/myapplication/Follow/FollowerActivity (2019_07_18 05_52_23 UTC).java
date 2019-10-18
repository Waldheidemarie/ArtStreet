package com.example.chisu.myapplication.Follow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 팔로워들과 팔로우하고 있는 사람들의 명단을 나타내주는 액티비티다.
 인텐트로 보내온 정보에 따라 해당 유저의 팔로워들을 보여줄 수도 있고,
 해당 유저가 팔로우하고 있는 사람들을 보여줄 수도 있다.
 */

public class FollowerActivity extends AppCompatActivity {

    Toolbar toolbar;

    //좋아요 한 사람들을 표시할 리사이클러뷰 선언
    RecyclerView recyclerView;

    List<FollowRecyclerviewItem> followItems;

    //게시물을 구분할 pk를 담을 변수 선언.
    String isFollower;

    String gotUsername;
    FollowRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        toolbar = findViewById(R.id.profile_change_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);

        //아티클 액티비티에서 넘어온 인텐트를 받는다. 어떤 게시물에 사람들이 좋아요를 했는지 구분하기 위해서.
        Intent intent = getIntent();
        isFollower = intent.getStringExtra("isFollower");
        gotUsername = intent.getStringExtra("gotUserName");

        //xml과 연결
        recyclerView = findViewById(R.id.followerRecyclerView);
        recyclerView.setHasFixedSize(true);

        //리사이클러뷰에 리니어레이아웃매니저 설치
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //코멘트아이템 리스트 초기화
        followItems = new ArrayList<>();

        //어댑터 선언 및 초기화 - 여기서 어댑터의 아이템 리스트와 액티비티의 아이템 리스트를 연결.
        adapter = new FollowRecyclerViewAdapter(followItems, getApplicationContext());

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //팔로워들을 본다면
        if (isFollower.equals("follower")){
            loadFollowers();
            getSupportActionBar().setTitle("팔로워");


        } else {

            loadFollowings();
            getSupportActionBar().setTitle("팔로잉");

        }
        Log.e("onStart 끝", "");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("onResume 끝", "");

    }

    //유저의 팔로워들을 로드하기.
    public void loadFollowers(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.FOLLOW_URL + "seeFollowers",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("좋아요 받기 완료~ : ", gotUsername);

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                followItems.add(new FollowRecyclerviewItem(
                                        jsonObject.getString("followedUserImage"),
                                        jsonObject.getString("followedUserName")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                            Log.e("data 받기 완료 : ", "ㅇㄴ");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("data 받는 중 에러 발생 ", e.getMessage());

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("좋아요 중에 에러발생~ : ", gotUsername);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("followedUser", gotUsername);
                Log.e("좋아요 아이디 전송 완료, 값 : ", gotUsername);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //유저가 팔로우하고 있는 사람들을 로드하기.
    public void loadFollowings(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.FOLLOW_URL + "seeFollowingUser",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("좋아요 받기 완료~ : ", gotUsername);

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                followItems.add(new FollowRecyclerviewItem(
                                        jsonObject.getString("followedUserImage"),
                                        jsonObject.getString("followedUserName")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                            Log.e("data 받기 완료 : ", "ㅇㄴ");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("data 받는 중 에러 발생 ", e.getMessage());

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("좋아요 중에 에러발생~ : ", gotUsername);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("followingUser", gotUsername);
                Log.e("좋아요 아이디 전송 완료, 값 : ", gotUsername);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
