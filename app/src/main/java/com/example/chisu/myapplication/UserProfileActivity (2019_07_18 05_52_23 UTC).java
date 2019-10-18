package com.example.chisu.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chisu.myapplication.Follow.FollowerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//다른 유저의 프로필을 볼 수 있는 액티비티.
public class UserProfileActivity extends AppCompatActivity {

    GridView gridView;
    profileGridViewAdapter gridViewAdapter;

    //프로필 관련 변수 선언
    TextView userName;
    TextView userDesc;
    TextView userTitle;
    TextView userScore;

    ImageView userProfileImage;
    ImageView userGradeImage;
    ImageView userBackImage;

    ImageButton followBtn;

    String gotUserName;
    String gotUserDesc;
    String gotProfileUrl;
    String gotUserTitle;
    String gotUserScore;

    //이 유저를 팔로우하고 있는 사람들의 수
    String gotFollowedUser;

    //이 유저가 팔로우하고 있는 사람들의 수
    String gotFollowingUser;

    //이 유저가 그린 그림의 갯수
    String gotMadeDrawingCount;

    String gotCollectedDrawingCount;

    RequestOptions requestOptions = new RequestOptions();

    String itemUser;

    //이미지 버튼에 팔로우 표시가 되어있는지 판별하는 boolean 변수.
    boolean followMark;

    //팔로워가 몇 명인지 알아내기 위한 변수
    int nowFollower;
    int nowValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userName = findViewById(R.id.userName);
        userDesc = findViewById(R.id.userDescription);
        userTitle = findViewById(R.id.rankText);
        userScore = findViewById(R.id.rankNumber);

        userProfileImage = findViewById(R.id.userProfileImage);
        userBackImage = findViewById(R.id.userBackImage);
        userBackImage.setColorFilter(Color.parseColor("#9b9b9b"), PorterDuff.Mode.MULTIPLY);
        userGradeImage = findViewById(R.id.rankImage);

        followBtn = findViewById(R.id.followButton);

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "버튼 클릭", Toast.LENGTH_SHORT).show();
                setFollowing();
            }
        });



        //Article 액티비티에서 넘어오는 인텐트
        Intent intent = getIntent();
        itemUser = intent.getStringExtra("itemUser");

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        //만약 유저의 이름과 현재 사용자의 닉네임이 같다면 보이지 팔로우 버튼을 무효화한다.
        if(itemUser.equals(username)){
            followBtn.setVisibility(View.INVISIBLE);
        }

        gridViewAdapter = new profileGridViewAdapter();
        gridView = findViewById(R.id.profileGridView);


        gridView.setAdapter(gridViewAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //서버에서 넘어오는 정보 받기
        loadProfile();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //초기 팔로우 여부 표시
        earlyFollowLoad();

    }

    //sp에서 유저 네임을 보내고, DB에서 해당하는 유저 정보를 받아와서 그 정보들을 프로필 창에 표시해주면 된다. 보내는 건 유저의 이름.
    //유저의 정보를 db에서 받아와서 그 정보를 클라이언트에 표시해주는 메소드.
    private void loadProfile() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.PROFILE_CHANGES + "?apicall=load",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //json 어레이는 json 객체가 될 수 없다. 진짜 개힘드네
                            JSONArray array = new JSONArray(response);

                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                gotUserName = product.getString("loadedUsername");
                                gotUserDesc = product.getString("loadedUserdesc");
                                gotProfileUrl = product.getString("loadedUserimage");
                                gotUserTitle = product.getString("loadedUsernickname");
                                gotUserScore = String.valueOf(product.getInt("loadedUserscore"));
                                gotFollowedUser = String.valueOf(product.getInt("loadedFollowedUser"));
                                gotFollowingUser = String.valueOf(product.getInt("loadedFollowingUser"));
                                gotMadeDrawingCount = String.valueOf(product.getInt("madeDrawingCount"));
                                gotCollectedDrawingCount = String.valueOf(product.getInt("collectedDrawingCount"));

                                requestOptions.placeholder(R.drawable.ic_account_circle_black_25dp);
                                requestOptions.error(R.drawable.ic_account_circle_black_25dp);

                                Glide.with(getApplicationContext())
                                        .setDefaultRequestOptions(requestOptions)
                                        .load(gotProfileUrl)
                                        .into(userProfileImage);

                                userName.setText(gotUserName);
                                userDesc.setText(gotUserDesc);
                                userScore.setText(gotUserScore);
                                userTitle.setText(gotUserTitle);

                                gridViewAdapter.addItem(gotFollowingUser, "팔로잉");
                                gridViewAdapter.addItem(gotFollowedUser, "팔로워");
//                                gridViewAdapter.addItem("0", "협업");
                                gridViewAdapter.addItem(gotMadeDrawingCount, "그린 그림");
                                gridViewAdapter.addItem(gotCollectedDrawingCount, "수집한 그림");
//                                gridViewAdapter.addItem("To.", "메시지 보내기");

                                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                                        switch (position) {
                                            case 0: //팔로우
                                                Intent intent0 = new Intent(getApplicationContext(), FollowerActivity.class);
                                                //팔로워를 보는지 팔로우하고 있는 사람들을 보는지 구분하기 위해 인텐트를 보낸다.
                                                intent0.putExtra("isFollower", "following");
                                                intent0.putExtra("gotUserName", gotUserName);
                                                startActivity(intent0);
                                                break;

                                            case 1: //팔로워
                                                Intent intent1 = new Intent(getApplicationContext(), FollowerActivity.class);
                                                //팔로워를 보는지 팔로우하고 있는 사람들을 보는지 구분하기 위해 인텐트를 보낸다.
                                                intent1.putExtra("gotUserName", gotUserName);
                                                intent1.putExtra("isFollower", "follower");
                                                startActivity(intent1);
                                                break;
                                            case 2: //그린 그림

                                                Intent intent3 = new Intent(getApplicationContext(), DrawingsActivity.class);
                                                //구분용 인텐트 나누기
                                                intent3.putExtra("gotUserName", gotUserName);
                                                intent3.putExtra("isCollected", "no");
                                                startActivity(intent3);
                                                break;

                                            case 3:  //수집한 그림
                                                Intent intent4 = new Intent(getApplicationContext(), DrawingsActivity.class);
                                                //구분용 인텐트 나누기
                                                intent4.putExtra("gotUserName", gotUserName);
                                                intent4.putExtra("isCollected", "yes");
                                                startActivity(intent4);
                                                break;
                                        }
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //역시 서버에서 넘어오지 못하는 것이었군.
                        Toast.makeText(getApplicationContext(), "에러 발생!", Toast.LENGTH_SHORT).show();
                        Log.e("에러 발생", error.getMessage());

                    }
                }) {
            //db에 구별용 아이디를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //유저네임이 비어 있으면, 즉 로그아웃 상태이면 아무것도 하지 않는다.
                params.put("username", itemUser);
                Log.e("보내기 완료", itemUser);

                return params;
            }

        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    String isFollowing;

    //초기 팔로우 모양 잡는 메소드. 여기서 팔로워 명수도 정해준다.
    public void earlyFollowLoad() {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        final String followedUser = itemUser;
        final String followingUser = username;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.FOLLOW_URL + "check",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        //response에 값이 있는지 없는지 체크해서 없으면 false로 바꿔주고
                        //즉 좋아요 취소 표시를 해 주고
                        //좋아요가 안 돼 있으므로 값은 그대로 표시. 아무것도 안하면 됨.
                        //no info라는 건 해당 게시물에 좋아요를 과거에 표시하지 않았다는 뜻이다.
                        //그러므로 좋아요를 표시한 글에는 리스트의 실제 좋아요 개수보다 1을 더 올려서 보여주면 된다.
                        if (response.equals("no info")) {

                            //정보가 없으면 표정을 일반표정으로 세팅.
                            followMark = false;
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.ic_person_add_white_24dp)
                                    .into(followBtn);

                            Log.e("이 작품에 과거에 팔로우를 안 했습니다..", "");

                        } else {
                            //이미 좋아요 했던 거라면 어쩔.. 아 이거 진짜 머리아프네
                            //옛날에 한 좋아요와 없었는데 새로 누른 좋아요를 구분해야 한다.
                            //있으면 웃는 모습으로 세팅.
                            followMark = true;
                            Glide.with(getApplicationContext())
                                    .load(R.drawable.ic_check_box_white_24dp)
                                    .into(followBtn);

                            Log.e("이 작품에 과거에 팔로우를 했습니다.", "");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("에러 발생했다!! 내용 : ", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("followedUser", followedUser);
                params.put("followingUser", followingUser);

                Log.e("초기 팔로우 데이터 전송 완료", followedUser + "," + followingUser);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    //팔로우 관련 메소드.
    public void setFollowing() {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        final String followedUser = itemUser;
        final String followingUser = username;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.FOLLOW_URL + "follow",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.e("트라이 진입. : ", "");

                            JSONArray array = new JSONArray(response);

                            for (int i = 0; i < array.length(); i++) {

                                JSONObject jsonobject = array.getJSONObject(i);

                                //정보를 넘겨 받는다.
                                nowFollower = jsonobject.getInt("followers");
                                nowValue = jsonobject.getInt("userScore");
                                isFollowing = jsonobject.getString("isFollow");
                                Log.i("정보 받기 완료. : ", isFollowing + nowFollower);

                                //정보가 없는 경우, 즉 맨 처음에 팔로우를 하지 않았던 경우.
                                //조건을 두 개를 주어야 할까?
                                if (isFollowing.equals("true")) {

                                    //정보가 없는 상태에서 그림이 팔로우 되어 있지 않다면(클릭 0회 함) 팔로우 상태로 만들어 준다. 즉 +1해준다.
                                    if (!followMark) {
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_check_box_white_24dp)
                                                .into(followBtn);

                                        Toast.makeText(getApplicationContext(), "팔로우합니다.", Toast.LENGTH_SHORT).show();

                                        //팔로우 버튼을 클릭하면 +1 되게 한다.

                                        gotFollowedUser = String.valueOf(Integer.parseInt(gotFollowedUser) + 1);
                                        gotUserScore = String.valueOf(nowValue);

                                        userScore.setText(gotUserScore);

                                        Log.e("팔로워 수 ", gotFollowedUser);

                                        gridViewAdapter.clear();

                                        gridViewAdapter.addItem(gotFollowingUser, "팔로잉");
                                        gridViewAdapter.addItem(gotFollowedUser, "팔로워");
//                                        gridViewAdapter.addItem("0", "협업");
                                        gridViewAdapter.addItem(gotMadeDrawingCount, "그린 그림");
                                        gridViewAdapter.addItem(gotCollectedDrawingCount, "수집한 그림");
//                                        gridViewAdapter.addItem("To.", "메시지 보내기");

                                        gridViewAdapter.notifyDataSetChanged();

                                        followMark = true;
                                        Log.e("팔로우합니다..", " ");

                                    }
                                }
                                //정보가 있는 경우
                                else {
                                    //정보가 있고 팔로우하고 있는 경우. 이 때 클릭하면 -1 해준다.
                                    if (followMark) {
                                        //이게 실행 되네?
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_person_add_white_24dp)
                                                .into(followBtn);

                                        Toast.makeText(getApplicationContext(), "팔로우 취소.", Toast.LENGTH_SHORT).show();

                                        nowFollower = Integer.parseInt(gotFollowedUser) - 1;

                                        gotUserScore = String.valueOf(nowValue);
                                        userScore.setText(gotUserScore);

                                        gotFollowedUser = String.valueOf(nowFollower);

                                        Log.e("팔로워 수 ", gotFollowedUser);
                                        gridViewAdapter.clear();

                                        gridViewAdapter.addItem(gotFollowingUser, "팔로우");
                                        gridViewAdapter.addItem(gotFollowedUser, "팔로워");
//                                        gridViewAdapter.addItem("0", "협업");
                                        gridViewAdapter.addItem(gotMadeDrawingCount, "그린 그림");
                                        gridViewAdapter.addItem(gotCollectedDrawingCount, "수집한 그림");
//                                        gridViewAdapter.addItem("To.", "메시지 보내기");

                                        gridViewAdapter.notifyDataSetChanged();

                                        followMark = false;
                                        Log.e("팔로우 취소.", String.valueOf(Article.isSmile));

                                    }
                                }
                            }
                            Log.i("리스트에 객체들 올리기 완료. : ", "");

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("에러 발생, 내용 : ", e.getMessage());
                            Toast.makeText(getApplicationContext(), "에러 발생 1", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("에러 발생했다!! 내용 : ", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("followedUser", followedUser);
                params.put("followingUser", followingUser);

                Log.e("send 메소드 완료했습니다, 값은 ", followedUser + "," + followingUser);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
