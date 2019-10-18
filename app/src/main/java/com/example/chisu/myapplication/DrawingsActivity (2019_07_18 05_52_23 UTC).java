package com.example.chisu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//그린 그림, 수집한 그림을 나타내는 액티비티. 인텐트에 따라 내용이 달라지게 해 놓았다.
public class DrawingsActivity extends AppCompatActivity {

    Toolbar toolbar;

    //좋아요 한 사람들을 표시할 리사이클러뷰 선언
    RecyclerView recyclerView;

    List<DrawingsRecyclerviewItem> drawingsItems;

    //게시물을 구분할 pk를 담을 변수 선언.
    String gotUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawings);

        toolbar = findViewById(R.id.drawings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);

        //아티클 액티비티에서 넘어온 인텐트를 받는다. 어떤 게시물에 사람들이 좋아요를 했는지 구분하기 위해서.
        Intent intent = getIntent();
        gotUserName = intent.getStringExtra("gotUserName");

        //인텐트 여부에 따라 다른 메소드가 실행되도록 한다.
        if (intent.getStringExtra("isCollected").equals("yes")) {
            showLikeDrawings();
            getSupportActionBar().setTitle("수집한 그림");
        }
         else {
            showMadeDrawings();
            getSupportActionBar().setTitle("그린 그림");
        }

        //xml과 연결
        recyclerView = findViewById(R.id.drawings_recyclerView);
        recyclerView.setHasFixedSize(true);

        //리사이클러뷰에 리니어레이아웃매니저 설치
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));

        //코멘트아이템 리스트 초기화
        drawingsItems = new ArrayList<>();

        //어댑터 선언 및 초기화 - 여기서 어댑터의 아이템 리스트와 액티비티의 아이템 리스트를 연결.
        DrawingsRecyclerviewAdpater adapter = new DrawingsRecyclerviewAdpater(drawingsItems, DrawingsActivity.this);

        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void showLikeDrawings() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.DRAWING_URL + "showLikeDrawings",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(" 받기 완료~ : ", "");

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                drawingsItems.add(new DrawingsRecyclerviewItem(
                                        jsonObject.getString("drawingId"),
                                        jsonObject.getString("drawingImage"),
                                        jsonObject.getString("drawingUser")
                                ));
                            }
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
                        Log.e("에러발생~ : ", "");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userName", gotUserName);
                Log.e("아이디 전송 완료, 값 : ", gotUserName);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void showMadeDrawings() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.DRAWING_URL + "showMadeDrawings",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(" 받기 완료~ : ", "");

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                drawingsItems.add(new DrawingsRecyclerviewItem(
                                        jsonObject.getString("drawingId"),
                                        jsonObject.getString("drawingImage"),
                                        jsonObject.getString("drawingUser")
                                ));
                            }
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
                        Log.e("에러발생~ : ", "");
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userName", gotUserName);
                Log.e("아이디 전송 완료, 값 : ", gotUserName);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

}
