package com.example.chisu.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword;
    ProgressBar progressBar;

    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        //로그인되어있는 상태라면
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, TabActivity.class));
        }

        progressBar = findViewById(R.id.progressBar);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textView = findViewById(R.id.loginTitle);
        textView.setTypeface(Typeface.createFromAsset(getAssets(), "sangsangflowerroad.ttf"));

        //if user presses on login
        //calling the method login
        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

        //if user presses on not registered
        findViewById(R.id.textViewRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open register screen
                finish();
                startActivity(new Intent(getApplicationContext(), RegisterUserActivity.class));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //로그인이 성공해서 나갈 때와 실패해서 나갈 때를 구분해서 isLogin 값을 다르게 보내주어야 한다.
        if (SharedPrefManager.getInstance(this).isLoggedIn()){
//            TabActivity.isLogin = true;

        } else {
//            TabActivity.isLogin = false;
        }
    }

    private void userLogin() {
        //first getting the values
        final String username = editTextUsername.getText().toString();
        final String password = editTextPassword.getText().toString();

        //validating inputs
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("아이디를 입력해주세요");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("비밀번호를 입력해주세요");
            editTextPassword.requestFocus();
            return;
        }

        //if everything is fine 여기서 정보를 넣고 받는 일을 동시에 진행하고 있다.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_LOGIN,
                //Listener는 AysncTask의 postExecute(..)와 유사합니다.
                //여기서 결과를 파싱하고 View에 보여줄 아이템 목록을 추가하고, 데이터셋이 변경되었다는 것을 통지합니다
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressBar.setVisibility(View.GONE);

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //에러가 없고 정상적일 때, 즉 로그인에 성공했을 때
                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                                //반응을 얻는다. 가능한 반응은 2개. 오류 혹은 가입성공
                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("user");

                                //유저 객체를 만들고, 그 안에 서버에서 받은 값을 넣는다.
                                //creating a new user object
                                User user = new User(
                                        userJson.getInt("id"),
                                        userJson.getString("username"),
                                        userJson.getString("email"),
                                        userJson.getString("gender")
                                );

                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                                //starting the profile activity
//                                TabActivity.isLogin = true;
                                finish();
                                startActivity(new Intent(getApplicationContext(), TabActivity.class));
                            } else { //서버에서 메시지를 받아서 표시해준다.
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            //여기서 데이터를 넣는 것 같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        //리퀘스트를 큐에 add해주면 끝.
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
