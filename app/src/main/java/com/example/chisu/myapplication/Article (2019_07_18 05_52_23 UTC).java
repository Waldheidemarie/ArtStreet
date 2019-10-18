package com.example.chisu.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.chisu.myapplication.Comment.CommentActivity;
import com.example.chisu.myapplication.Like.LikeActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//메인 리사이클러뷰에서 그림을 클릭하면 나타나는 액티비티.
public class Article extends AppCompatActivity {

    static List<art> artList;

    //페이징용 카운트
    int requestCount = 1;

    //어댑터에서 초기화용으로 사용하기 때문에 스태틱으로 선언
    public static TextView textViewTitle, textViewRecom, textViewUser;
    public static TextView textViewCreated, textViewComment, textViewValue;

    public static com.github.siyamed.shapeimageview.CircularImageView imageViewUser;

    //전체 레이아웃 - 겹쳐진 레이아웃 중 하나인 정보 레이아웃이 나타났다 사라졌다 하기 때문에 프레임 레이아웃 사용.
    FrameLayout frameLayout;

    //그림의 정보를 담당하는 레이아웃
    static ConstraintLayout artInfo;

    //그림을 담당하는 레이아웃
    ConstraintLayout pagerLayout;

    //스마일 버튼
    static ImageButton smileBtn;

    ImageButton deleteBtn;

    //인텐트로 넘어오는 값 받기 용.
    String itemId;

    //그림의 화가 이름
    static String itemUser;

    //뷰페이지 구분자
    String viewPageNumber;

    //정보를 보는지 여부에 대한 판단 변수
    static boolean isInfo = false;

    //좋아요를 눌렀는지 판단하기 위한 불리언 변수. 처음에는 false로 선언해둠.
    static boolean isSmile;

    //서버에 좋아요 정보가 있는지 확인하는 변수.
    String isLike;

    //뷰페이저 선언
    ViewPager viewPager;
    //뷰페이저 어댑터
    ArticleViewpagerAdapter adapter;

    //게시물의 추천, 가치, 댓글 개수를 나타내는 변수.
    //다른 클래스에서도 조절해야하므로 스태틱으로 선언.
    static String recom1;
    static String value1;
    static String comm1;
    int nowRecom1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Log.e("Article onCreate", " ");


        textViewTitle = findViewById(R.id.textViewTitle);
//        textViewDesc = findViewById(R.id.textViewDesc);
        textViewRecom = findViewById(R.id.textViewRecom);
        textViewUser = findViewById(R.id.textViewUser);
        textViewCreated = findViewById(R.id.textViewTime);
        textViewComment = findViewById(R.id.textViewComment);
        textViewValue = findViewById(R.id.textViewValue);

        imageViewUser = findViewById(R.id.userProfileImage);

        artList = new ArrayList<>();

        artInfo = findViewById(R.id.artInfo);
        frameLayout = findViewById(R.id.entire);
        pagerLayout = findViewById(R.id.pager);
        smileBtn = findViewById(R.id.smileBtn);

        deleteBtn = findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Article.this);
                builder.setTitle("그림 삭제");
                builder.setMessage("이 그림을 삭제합니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                deleteArticle();

                                Toast.makeText(getApplicationContext(),"삭제했습니다.",Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

        smileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSmile();
            }
        });

        //db에서 정보를 받아오기 위한 id를 받는 인텐트. 이 인텐트는 카드어댑터에서 날아온다.
        //받은 정보를 통해 발리로 통신하면 된다. 이 id를 서버에 보내서 해당하는 정보를 가져오도록 한다.
        Intent intent = getIntent();
        itemId = String.valueOf(intent.getIntExtra("itemId", 1));
        itemUser = intent.getStringExtra("itemUser");
        viewPageNumber = intent.getStringExtra("viewPageNumber");

        //속도가 너무 빨라서 발생하는 nullpointer 방지용
        recom1 = "1";
        value1 = "1";
        comm1 = "1";

        //그리고 뷰페이저를 xml과 연결하고
        viewPager = findViewById(R.id.view_pager);

        Log.e("onCreate 종료 ", " ");

        nowRecom1 = Integer.parseInt(value1);
    }

    public void checkUserForDelete(){
        //deleteBtn은 사용자가 게시물의 작성자와의 일치 여부에 따라 나타나거나 사라지게 해야 한다.

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        Log.e("화가명", itemUser);

        //만약 현재 이용자와 작성자가 일치한다면
        if (username.equals(itemUser)){
            //아무것도 하지마.가 아니라 보이게 해야지!
            deleteBtn.setVisibility(View.VISIBLE);

        } else {
            //그러나 일치하지 않는다면 삭제 버튼을 비활성화 한다.
            deleteBtn.setVisibility(View.INVISIBLE);
        }
    }

    //onStart에는 너무 많은 작업을 하면 다 못하는 듯 하다..
    @Override
    protected void onStart() {
        super.onStart();

        //DB의 좋아요 데이터 여부에 따라 초기 스마일의 상태를 설정해 준다.
        earlySmileLoad();

        //onResume에 어댑터를 초기화하는 이유 :
        //어댑터의 콜백 메소드인 instantiate에서 뷰페이저의 첫 번째 아이템을 초기화하기 때문이다.
        //즉 어댑터의 초기화는 뷰페이저의 첫 번째 페이지의 정보를 설정하는 메소드역할을 한다고도 볼 수 있다.

        Log.e("onStart 종료", " ");
    }

    //왜 로그를 안찍지?
    @Override
    protected void onResume() {
        super.onResume();

        adapter = new ArticleViewpagerAdapter(artList, Article.this);

        checkUserForDelete();

        //초기화한 뷰페이저 어댑터의 리스트에 아트 객체들을 넣는다.
        loadProducts();

        //뷰페이저에 어댑터를 단다.
        viewPager.setAdapter(adapter);

        //페이지체인지리스너도 단다.
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
           /**
             * 아래 메소드는 추가 작업 필요 부분. 페이지를 넘어갈 때 데이터를 추가 로드할 수 있게 해야 한다.
             * 페이지 맨 처음에 좌단이 아닌 중간부터 시작하는 것도 필요함
             */
            @Override
            public void onPageSelected(int position) {

                //페이지가 넘어갈 때마다 itemId, itemId가 해당 게시물의 id로 변하도록 해야 한다.
                //게시글의 유저 사진을 클릭했을 때 해당 유저의 프로필로 넘어가기 위해서 여기에 유저이름도 설정해 둔다.
                itemId = String.valueOf(artList.get(position).getProductId());
                itemUser = artList.get(position).getUser();

                //페이지를 넘길 때마다 변수를 초기화한다.
                recom1 = artList.get(position).getRecom();
                value1 = artList.get(position).getValue();
                comm1 = artList.get(position).getCommentNum();

                Log.e("isSMile ", String.valueOf(isSmile));

                //글자를 문장 형태로 넘기기 위해 새로운 스트링에 저장.
                String value2 = "작품의 가치 : " + value1;
                String comm2 = "댓글 " + comm1 + "개";

                //페이지를 넘길 때마다 초기 스마일 상태와 댓글 개수, 좋아요 개수를 설정해 준다.
                earlySmileLoad();

                textViewTitle.setText(artList.get(position).getTitle());
//                textViewDesc.setText(artList.get(position).getDesc());
                textViewUser.setText(artList.get(position).getUser());
                textViewCreated.setText(artList.get(position).getCreated());
                textViewComment.setText(comm2);
                textViewValue.setText(value2);

                Glide.with(getApplicationContext())
                        .load(artList.get(position).getUserImage())
                        .into(imageViewUser);

                //페이지가 넘어갈 때마다 해당 페이지의 좋아요가 눌려있는지 검사해야 한다. 근데 왜 한 발씩 늦지?
                //아래의 메소드가 그 아래의 if문보다 느리게 시행되는 것이 문제다.
                //그러니까 표정을 바꾸고 그 뒤에 설정을 하기 때문인 것이다. 그렇다면 if문이 기다리게 해야 하는데...
                //표정을 반대로 설정해뒀었다. 해결 완료.

                checkUserForDelete();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Log.e("onResume 종료", " ");
    }

    @Override
    protected void onStop() {
//        finish();
        super.onStop();
    }

    //클릭하면 해당 게시물의 좋아요 액티비티로 넘어가는 메소드.
    public void goToLikeActivity(View v) {

        //뷰페이저의 페이지가 넘어감에 따라 그 글의 id도 바뀌어야 한다.
        //댓글 버튼을 클릭하면 그 게시물의 pk를 코멘트 리스트 액티비티에 보내야 한다.
        Intent intent = new Intent(getApplicationContext(), LikeActivity.class);
        intent.putExtra("itemId", itemId);
        startActivity(intent);

    }

    //댓글 텍스트뷰 클릭 시
    public void goToCommentActivity(View v) {

        Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
        intent.putExtra("itemId", itemId);
        startActivity(intent);
    }

    //댓글 쓰기 아이콘 클릭 시
    public void goToCommentActivityToWrite(View v) {

        Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
        intent.putExtra("itemId", itemId);
        intent.putExtra("write", "write");
        startActivity(intent);
    }

    public void goToImageZoomActivity(View v) {

        Intent intent = new Intent(getApplicationContext(), ImageZoomActivity.class);
        intent.putExtra("itemId", itemId);
        startActivity(intent);
    }

    //프로파일 액티비티로 가는 메소드.
    public void goToUserProfileActivity(View v) {

        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.putExtra("itemUser", itemUser);
        startActivity(intent);
    }

    //일반 다이얼로그를 써보자.
    //해당 게시물을 삭제하는 메소드. 필요한 건 유저네임, 그림의 게시자, 그림의 아이디.
    public void deleteArticle(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.ROOT_URL + "deletePics",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("에러 발생, 내용 : ", error.getMessage());
                        Toast.makeText(getApplicationContext(), "에러 발생 2", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("drawingId", itemId);
                params.put("userName", itemUser);

                Log.i("딜릿아티클 메소드 보내기 완료, 값 ", itemId+itemUser);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //처음 아티클 액티비티를 들어왔을 때,
    //이 게시물에 대해 좋아요를 이전에 클릭했는지 여부를 알아내서 표시해주는 메소드.
    //게시물의 아이디와 유저의 이름을 따고, 그 정보를 보내 서버에서 작업한다.
    //서버에서 작업 결과 이미 좋아요를 표시한 기록이 있으면 좋아요 취소 버튼으로 바꿔둔다.
    //또한 좋아요 개수도 표정에 따라 다르게 표시해준다.
    public void earlySmileLoad() {

        //현재 사용 중인 유저네임을 알아내기 위한 sp에서 꺼내기. 두 개의 메소드에서 사용하므로 전역변수로 만들었다....지만
        //전역변수로 쓰면 왜인지 nullpointerException이 나서 지역변수로 사용한다.
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        VolleySendClass.send(itemId, username, getApplicationContext(), URLs.LIKE + "check", 1);
    }

    //스마일 버튼을 클릭할 때마다 발리 통신을 한다.
    public void setSmile() {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        final String itemId2 = itemId;
        final String username2 = username;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.LIKE + "like",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.e("트라이 진입. : ", "");

                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonobject = array.getJSONObject(i);

                                //정보를 넘겨 받는다.
                                nowRecom1 = jsonobject.getInt("drawingCommentNumber");
                                isLike = jsonobject.getString("isliked");
                                Log.i("정보 받기 완료. : ", "");

                                //정보가 없는 경우
                                //조건을 두 개를 주어야 할까?
                                if (isLike.equals("false")) { //조회해 보니 이미 row가 있어서 삭제해서 좋아요가 하나 사라진 경우.

                                    //정보가 없는 상태에서 웃고 있지 않다면(클릭 0회 함) 웃게 만들어 준다. 즉 +1해준다.
                                    if (!isSmile) {
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_sentiment_very_satisfied_white_24dp)
                                                .into(smileBtn);

                                        Toast.makeText(getApplicationContext(), "이 작품을 좋아합니다.", Toast.LENGTH_SHORT).show();

                                        //좋아요를 클릭하면 +1 되게 한다.

                                        nowRecom1 = nowRecom1 + 1;

                                        String recom = "좋아요 " + String.valueOf(nowRecom1) + "명";
                                        textViewRecom.setText(recom);

                                        //좋아요를 클릭하면 작품의 가치가 +1 상승한다.
                                        String value = "작품의 가치 : " + String.valueOf(Integer.parseInt(value1) + 2);
                                        textViewValue.setText(value);

                                        isSmile = true;
                                        Log.e("이 작품을 좋아합니다..", String.valueOf(Article.isSmile));

                                    }
                                    //정보가 없는 상태에서 웃고 있다면(클릭 1회 함) 원래대로 되돌린다. 표정을 일반으로 하고 값은 원래 값으로 돌아온다.
                                    else {
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_mood_white_24dp)
                                                .into(smileBtn);

                                        Toast.makeText(getApplicationContext(), "좋아요 취소.", Toast.LENGTH_SHORT).show();

                                        nowRecom1 = nowRecom1 ;

                                        String recom = "좋아요 " + String.valueOf(nowRecom1) + "명";
                                        textViewRecom.setText(recom);
                                        String value = "작품의 가치 : " + String.valueOf(Integer.parseInt(value1));
                                        textViewValue.setText(value);

                                        isSmile = false;
                                        Log.e("좋아요 취소..", String.valueOf(Article.isSmile));

                                    }
                                }
                                //정보가 없었어서 새로 좋아요 테이블에 row를 추가한 경우.
                                else {
                                    //정보가 있고 웃고 있는 경우. 이 때 클릭하면 -1 해준다.
                                    if (isSmile) {
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_mood_white_24dp)
                                                .into(smileBtn);

                                        Toast.makeText(getApplicationContext(), "좋아요 취소.", Toast.LENGTH_SHORT).show();

                                        //과거의 좋아요를 취소하는 것이므로 -1해준다.
                                        nowRecom1 = nowRecom1 - 1;

                                        String recom = "좋아요 " + String.valueOf(nowRecom1) + "명";
                                        Log.e("좋아요 취소!!!!!!!!.", String.valueOf(nowRecom1));

                                        textViewRecom.setText(recom);
                                        String value = "작품의 가치 : " + String.valueOf(Integer.parseInt(value1));
                                        textViewValue.setText(value);

                                        isSmile = false;
                                        Log.e("좋아요 취소.", String.valueOf(Article.isSmile));

                                    } else {
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.ic_sentiment_very_satisfied_white_24dp)
                                                .into(smileBtn);

                                        Toast.makeText(getApplicationContext(), "이 작품을 좋아합니다.", Toast.LENGTH_SHORT).show();

                                        //취소하면 다시 원래대로 돌아오므로 값을 변화시킬 필요 없다.
                                        nowRecom1++;
                                        String recom = "좋아요 " + String.valueOf(nowRecom1) + "명";
                                        textViewRecom.setText(recom);

                                        //좋아요를 클릭하면 작품의 가치가 +1 상승한다.
                                        String value = "작품의 가치 : " + String.valueOf(Integer.parseInt(value1) + 2);
                                        textViewValue.setText(value);

                                        isSmile = true;

                                        Log.e("이 작품을 좋아합니다???????", String.valueOf(Article.isSmile));

                                    }
                                }

                            }
                            Log.i("리스트에 객체들 올리기 완료. : ", "");

                            adapter.notifyDataSetChanged();

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
                params.put("itemId", itemId2);
                params.put("userName", username2);

                Log.e("send 메소드 완료했습니다, 값은 ", itemId2 + "," + username2);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    //DB에 있는 데이터들을 가져와 자바리스트에 넣어주는 메소드. 발리.
    public void loadProducts() {

        artList.clear();
        adapter.notifyDataSetChanged();

        Log.d("로드 프로덕트시작. : ", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.VIEW_PAGER_LOAD + "?apicall=" + viewPageNumber,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.w("트라이 바깥. : ", "");

                        //트라이 내부가 전혀 안되네.
                        try {
                            Log.e("트라이 진입. : ", "");

                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonobject = array.getJSONObject(i);

                                //adding the product to product list
                                artList.add(new art(
                                        jsonobject.getInt("drawingId"),
                                        jsonobject.getString("drawingTitle"),
                                        jsonobject.getString("drawingUser"),
                                        jsonobject.getString("drawingUserImage"),
                                        jsonobject.getString("drawingCreated"),
                                        jsonobject.getString("drawingImage"),
                                        jsonobject.getString("drawingRecommended"),
                                        jsonobject.getString("drawingValue"),
                                        jsonobject.getString("drawingCommentNumber")
                                ));
                                Log.v("애드 완료 : ", "");

                            }
                            Log.i("리스트에 객체들 올리기 완료. : ", "");

                            adapter.notifyDataSetChanged();

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
                        Log.e("에러 발생, 내용 : ", error.getMessage());
                        Toast.makeText(getApplicationContext(), "에러 발생 2", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("itemId", itemId);
                Log.i("로드 프로덕트 메소드 보내기 완료, 값 ", itemId);

                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

}
