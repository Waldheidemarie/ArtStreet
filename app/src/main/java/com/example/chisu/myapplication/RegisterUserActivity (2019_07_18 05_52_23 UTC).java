package com.example.chisu.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class RegisterUserActivity extends AppCompatActivity {

    //만약에 사용자가 로그인하고 있지 않다면 내용을 그대로 나타내고,
    //사용자가 로그인을 한번 한 후에 다시 들어올 경우에는 바로 프로필 페이지로 넘어간다.

    //xml파일에 뷰들을 연결하기 위한 선언
    EditText editTextUsername, editTextEmail, editTextPassword;
    RadioGroup radioGroupGender;
    ProgressBar progressBar;
    TextView textView;
    //액티비티에서는 되는데 프래그먼트에서는 안 되는 게 많다...ㅡㅡㅡ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        progressBar = findViewById(R.id.progressBar);

        //유저가 이미 로그인한 경우 바로 프로파일 액티비티로 보내버린다.
        //if the user is already logged in we will directly start the profile activity
        //로그인해 있으면 true를 리턴하고, 아니면 false를 리턴한다. ...싱글톤에 대한 공부가 더 필요할 듯 하다.
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            //로그인했으므로 isLogin을 true로 변경.
//            TabActivity.isLogin = true;
            finish();
            startActivity(new Intent(this, TabActivity.class));
            return;
        }

        //선언한 뷰들을 할당을 통해 연결.
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupGender = findViewById(R.id.radioGender);

        textView = findViewById(R.id.registerTitle);
        textView.setTypeface(Typeface.createFromAsset(getAssets(), "sangsangflowerroad.ttf"));


        //따로 java에서 변수를 선언하지 않아도 바로 findviewbyid를 통해 사용할 수도 있다.
        //다만 이 경우는 변수명이 깔끔하지 않기 때문에 반복 사용은 지양된다.
        //여기서는 findviewbyid에 바로 onclicklistener를 달았다.
        findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View view) {
                //if user pressed on button register
                //here we will register the user to server
                registerUser();
            }
        });

        findViewById(R.id.textViewLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if user pressed on login
                //we will open the login screen
                finish();
                startActivity(new Intent(RegisterUserActivity.this, LoginActivity.class));
            }
        });
    }

    //회원가입 버튼을 클릭했을 때
    private void registerUser() {
        //inner 클래스라서 final?
        final String username = editTextUsername.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        //라디오그룹 객체의 메소드를 사용한 것이다.
        final String gender = ((RadioButton) findViewById(radioGroupGender.getCheckedRadioButtonId())).getText().toString();

        //빈칸 여부 검사.
        //first we will do the validations
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("아이디를 입력해주세요");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("이메일을 입력해주세요");
            editTextEmail.requestFocus();
            return;
        }
        //이메일 유효성 검사(안드로이드 기능 중에 하나)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("이메일을 형식에 맞게 입력해주세요");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("비밀번호를 입력해주세요");
            editTextPassword.requestFocus();
            return;
        }

        //volley에 포함된 클래스.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);

                        try {
                            //json 객체에 서버에서 온 응답 json 객체를 담는다.
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //만약 response에 문제가 없다면
                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                                //response에서 user관련 데이터를 얻어낸다.
                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("user");

                                //user 객체를 생성자를 통해 생성한다. 그리고 그 유저 객체에 유저 데이터를 저장한다.
                                //creating a new user object
                                User user = new User(
                                        userJson.getInt("id"),
                                        userJson.getString("username"),
                                        userJson.getString("email"),
                                        userJson.getString("gender")
                                );

                                //생성한 유저 객체를 sp에 저장한다.
                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                                //회원가입이 완료되면 탭액티비티로 넘어간다.
                                finish();
                                startActivity(new Intent(getApplicationContext(), TabActivity.class));
                            } else { //만약 response에 문제가 있다면
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
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                params.put("gender", gender);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
