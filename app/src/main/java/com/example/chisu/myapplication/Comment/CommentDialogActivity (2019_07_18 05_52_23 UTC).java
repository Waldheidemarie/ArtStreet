package com.example.chisu.myapplication.Comment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.SharedPrefManager;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

//코멘트 화면에서 댓글 하나를 꾸욱 눌렀을 때 나타나는 다이얼로그형 액티비티
public class CommentDialogActivity extends Activity {

    //서버에 구분용으로 보낼 댓글의 id 및 클라 내 구분용인 코멘트리스트 내 포지션
    String commentId;
    String commentUser;
    int itemPosition;
    String itemId;
    Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_comment_dialog);

        deleteBtn = findViewById(R.id.commentDeleteBtn);

        //코멘트 리사이클러뷰 어댑터에서 날아온 인텐트 받기.
        Intent intent = getIntent();
        commentId = intent.getStringExtra("commentId");
        commentUser = intent.getStringExtra("commentUser");
        itemPosition = intent.getIntExtra("itemPosition", 1);
        itemId = intent.getStringExtra("itemId");
        Log.e("잘 받았습니다.", commentId +"ㅡ"+ itemPosition + "-" + commentUser);

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        //만약 코멘트유저와 현재 사용자가 이름이 일치한다면
        if (commentUser.equals(username)){
           //아무고토 하지 말고 그대로 둔다.
        } else {
            //그러나 그렇지 않다면
            deleteBtn.setVisibility(View.GONE);
        }
    }

    //댓글을 쓴 유저의 프로파일로 가는 메소드.
    public void watchProfile(View v){

    }

    //롱클릭으로 선택한 댓글을 삭제하는 메소드.
    //데이터베이스에서만 삭제하는 게 아니라 여기에 존재하는 아이템 리스트에서도 삭제해야 한다. 그리고 노티파이.
    //이제 자신의 댓글이 아니면 삭제 버튼이 사라지게 해야 한다.
    //그러려면 아이템의 코멘트유저네임과 sp의 사용자 네임을 비교해서 일치하면 모두 띄우고,
    //그렇지 않으면 삭제버튼을 invisible로 만들면 된다.
    public void deleteComment(View v){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.COMMENT_URL + "delete",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //삭제가 됐다는 리스폰스를 받으면 이곳의 리스트에도 해당하는 포지션의 댓글을 삭제해 준다.
                        if (response.equals("delete")) {
                            CommentActivity.commentItems.remove(itemPosition);
                            CommentActivity.adapter.notifyDataSetChanged();
                            Log.e("댓글이 삭제되었습니다.", "ㅇ");
                            Toast.makeText(getApplicationContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        Log.e("잘 받았다, 내용 : ", "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("에러 발생, 내용 : ", error.getMessage());
                    }
                }) {
            //삭제하기 위해 코멘트 아이템의 id를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("commentId", commentId);
                params.put("itemId", itemId);

                Log.e("코멘트 아이디 전송 완료, 값 : ", commentId);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
