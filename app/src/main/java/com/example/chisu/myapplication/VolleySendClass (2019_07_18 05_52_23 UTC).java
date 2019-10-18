package com.example.chisu.myapplication;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jisu7 on 2018-02-19.
 */

 class VolleySendClass {
    //발리를 사용할 때 받지 않고 하나의 아이템만 보낼 때 사용 가능한 메소드.
    //좀 복잡한 발리 같은 경우는 따로 구현하는 것이 낫다.

    //좋아요 관련 메소드.
    //Article의 oncreate에서, 그리고 페이지를 넘어갈 때마다, 클릭 시마다 실행된다.
     static void send(String itemId, String username, final Context context, String url, int fordivision) {

        final String itemId2 = itemId;
        final String username2 = username;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
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
                            Article.isSmile = false;
                            Glide.with(context)
                                    .load(R.drawable.ic_mood_white_24dp)
                                    .into(Article.smileBtn);
                            //그리고 좋아요 개수는 리스트에 저장된 그대로 보여주면 됨.
                            Article.textViewRecom.setText("좋아요 " + Article.recom1 + "명");

                            Log.e("이 작품에 과거에 좋아요를 안 했습니다..", String.valueOf(Article.isSmile));

                        } else {
                            //이미 좋아요 했던 거라면 어쩔.. 아 이거 진짜 머리아프네
                            //옛날에 한 좋아요와 없었는데 새로 누른 좋아요를 구분해야 한다.
                            //있으면 웃는 모습으로 세팅.
                            Article.isSmile = true;
                            Glide.with(context)
                                    .load(R.drawable.ic_sentiment_very_satisfied_white_24dp)
                                    .into(Article.smileBtn);

                            //좋아요 개수는 리스트에 저장된 것의 +1해서 보여주면 된다.
                            Article.textViewRecom.setText("좋아요 " + String.valueOf(Integer.parseInt(Article.recom1) ) + "명");
//                            Article.textViewValue.setText("작품의 가치 : " + String.valueOf(Integer.parseInt(Article.value1) + 1));

                            Log.e("이 작품에 과거에 좋아요를 했습니다.", String.valueOf(Article.isSmile));
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
                params.put("itemId", itemId2);
                params.put("userName", username2);

                Log.e("send 메소드 완료했습니다, 값은 ", itemId2 + "," + username2);

                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

}
