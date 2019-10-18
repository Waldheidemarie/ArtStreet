package com.example.chisu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ImageZoomActivity extends AppCompatActivity {

    //넘겨받는 인텐트를 담을 변수.
    String itemId;
    com.github.chrisbanes.photoview.PhotoView zoomView;

    //서버에서 넘어오는 url을 받을 String.
    String gotZoomUrl;

    RequestOptions requestOptions = new RequestOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");

        zoomView = findViewById(R.id.zoomImageView);

        loadImage();
    }

    private void loadImage() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.ZOOM_IMAGE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //json 어레이는 json 객체가 될 수 없다. 진짜 개힘드네
                            JSONArray array = new JSONArray(response);

                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                gotZoomUrl = product.getString("drawingImage");

                                requestOptions.placeholder(R.drawable.ic_account_circle_black_25dp);
                                requestOptions.error(R.drawable.ic_account_circle_black_25dp);

                                Glide.with(getApplicationContext())
                                        .setDefaultRequestOptions(requestOptions)
                                        .load(gotZoomUrl)
                                        .into(zoomView);
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
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            //db에 구별용 아이디를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                    params.put("itemId", itemId);


                return params;
            }

        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
