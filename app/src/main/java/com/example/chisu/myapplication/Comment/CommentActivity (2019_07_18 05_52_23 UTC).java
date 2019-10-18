package com.example.chisu.myapplication.Comment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chisu.myapplication.Article;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.SharedPrefManager;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    Toolbar toolbar;

    //코멘트들을 표시할 리사이클러뷰 선언
    RecyclerView recyclerView;

    static List<CommentRecyclerviewItem> commentItems;

    //게시물을 구분할 pk를 담을 변수 선언.
    static String itemId;

    static CommentRecyclerViewAdapter adapter;

    EditText commentText;
    com.github.siyamed.shapeimageview.CircularImageView addCommentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        toolbar = findViewById(R.id.commentToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);
        getSupportActionBar().setTitle("댓글");

        //인텐트로 게시글의 아이디 넘겨받기
        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");

        commentText = findViewById(R.id.editTextComment);
        addCommentBtn = findViewById(R.id.addCommentBtn);

        //intent에 write라는 이름을 가진 내용이 있다면
        if (intent.hasExtra("write")) {
            commentText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        }

        //xml과 연결
        recyclerView = findViewById(R.id.commentRecyclerView);
        recyclerView.setHasFixedSize(true);

        //일반적인 리니어레이아웃 매니저(아래의 경우)는 자동으로 크기를 조절하는 경우가 있다.
        //그래서 크기가 원하는 대로 안나오는 경우가 있는데, 아래처럼 하면 개선될 수 있다.
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(llm);

        //리사이클러뷰에 리니어레이아웃매니저 설치-위 코드로
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //코멘트아이템 리스트 초기화
        commentItems = new ArrayList<>();

        //어댑터 선언 및 초기화 - 여기서 어댑터의 아이템 리스트와 액티비티의 아이템 리스트를 연결.
        adapter = new CommentRecyclerViewAdapter(commentItems, getApplicationContext());

        recyclerView.setAdapter(adapter);

        loadComment();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Article.textViewComment.invalidate();

    }


    //보내기 버튼을 눌러 댓글을 전송하는 메소드.
    public void addComment(View v) {
        //애딧텍스트에서 글자를 가져온다.

        final String gotComment = commentText.getText().toString();

        //댓글이 비어 있다면 경고를 띄우고 중단.
       if (TextUtils.isEmpty(gotComment)) {
            commentText.setError("빈 댓글은 올릴 수 없습니다.");
            commentText.requestFocus();
            return;
        }

        //이름을 보내기 위해 sp에서 따온다.
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.COMMENT_URL + "add",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //전송이 완료됐으면 갱신하기 위해 다 지우고 새로 받아온다..면 낭비가 크니까
                        //sql에서 하나만 받아오도록 하자.

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                commentItems.add(new CommentRecyclerviewItem(
                                        jsonObject.getString("userimage"),
                                        jsonObject.getString("username"),
                                        jsonObject.getString("created"),
                                        jsonObject.getString("content"),
                                        jsonObject.getString("id")
                                ));

                                Toast.makeText(getApplicationContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                            adapter.notifyDataSetChanged();
                            Log.e("아이템 리스트에 싣고 노티파이까지 완료", "");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("댓글 보는 중에 에러 발생", e.getMessage());
                        }

                        commentText.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("에러 발생, 내용 : ", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("itemId", itemId);
                params.put("userName", username);
                params.put("content", gotComment);
                Log.e("댓글 정보 전송 완료, 값 : ", itemId + "," + gotComment);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    //게시물에 등록된 댓글을 사용자가 액티비티에 들어갔을 때 보여주는 메소드.
    //게시물 id를 구별용으로 보내준다.
    private void loadComment() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.COMMENT_URL + "check",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("코멘트 받기 완료~ : ", itemId);

                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i=0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonObject = array.getJSONObject(i);

                                //댓글 아이템 리스트에 댓글 아이템을 생성해서 추가한다.
                                //해당 댓글의 정보는 넘어온 json에서 받아 저장한다.
                                commentItems.add(new CommentRecyclerviewItem(
                                        jsonObject.getString("userimage"),
                                        jsonObject.getString("username"),
                                        jsonObject.getString("created"),
                                        jsonObject.getString("content"),
                                        jsonObject.getString("id")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                            Log.e("아이템 리스트에 싣고 노티파이까지 완료", "");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("댓글 보는 중에 에러 발생", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("코멘트 에러발생~ : ", itemId);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("itemId", itemId);
                Log.e("코멘트 아이디 전송 완료, 값 : ", itemId);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
