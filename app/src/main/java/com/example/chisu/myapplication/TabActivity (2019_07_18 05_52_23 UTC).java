package com.example.chisu.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chisu.myapplication.Drawer.DrawerItemListAdapter;
import com.example.chisu.myapplication.Follow.FollowerActivity;
import com.example.chisu.myapplication.PaintBoard.BestPaintBoardActivity;
import com.example.chisu.myapplication.PaintBoard.BestPaintBoardActivityVertical;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//메인 탭 액티비티.
public class TabActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    //2초 안에 백버튼을 한번 더 누르면 종료시키겠다는 변수.
    private final long FINISH_INTERVAL_TIME = 2000;
    //2초를 측정하기 위해 사용하는 변수.
    private long backPressedTime = 0;

//    static {
//        System.loadLibrary("native-lib");
//   }

    Toolbar toolbar;

    TabHost tabHost1;
    GridView gridView;
    profileGridViewAdapter gridViewAdapter;

    DrawerLayout drawer;
    //드로어레이아웃에 쓰일 리스트뷰.
    ListView listview;
    //드로어 토글
    ActionBarDrawerToggle dtToggle;

    DrawerItemListAdapter drawerItemListAdapter;

    //프로필 관련 변수 선언
    TextView userName;
    TextView userDesc;
    TextView userTitle;
    TextView userScore;
    TextView mainLogo;

    ImageView userProfileImage;
    ImageView userGradeImage;
    ImageView userBackImage;

    String gotUserName;
    String gotUserDesc;
    String gotProfileUrl;
    String gotUserTitle;
    String gotUserScore;
    String gotFollowers;
    String gotFollowings;
    String gotMadeDrawings;
    String gotCollectedDrawings;

    //체인지프로파일 클래스를 완료할 때 동시 종료를 위한 액티비티.
    public static Activity AActivity;

    RequestOptions requestOptions = new RequestOptions();

    //탭액티비티의 뷰페이지 구분을 위한 구분자
    public static int viewPageNumber;

    ViewPager viewPager;

    @Override
    public void onBackPressed() {
        //뒤로가기 버튼을 2초 이내에 2번 연속으로 눌러야 종료되도록 했다.
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        AActivity = TabActivity.this;

        //툴바 관련 코드
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        viewPageNumber = 0;

        //추가된 소스코드, Toolbar의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);
        getSupportActionBar().setTitle("Pencil");

        tabHost1 = findViewById(R.id.tabHost1);
        tabHost1.setOnTabChangedListener(this);
        tabHost1.setup();

        mainLogo = findViewById(R.id.mainLogo);
        mainLogo.setTypeface(Typeface.createFromAsset(getAssets(), "sangsangflowerroad.ttf"));


        //그러면 시작할 때 말고 나갈 때 탭들을 다 쓸어버리는 건 어떨까?
//          로그인 여부에 따라 탭 개수를 제어해 보자.
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {

            // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
            TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
            ts1.setContent(R.id.content1);
            ts1.setIndicator("둘러보기");
            tabHost1.addTab(ts1);

            //로그인이 안 되어 있으면 작업실 탭과 프로필 탭은 안 보이게 한다.
            ConstraintLayout ts2 = findViewById(R.id.content2);
            ts2.setVisibility(View.INVISIBLE);

            LinearLayout ts3 = findViewById(R.id.content3);
            ts3.setVisibility(View.INVISIBLE);

        } else {
            //로그인이 되어 있다면

            // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
            TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
            ts1.setContent(R.id.content1);
            ts1.setIndicator("둘러보기");

            tabHost1.addTab(ts1);

            // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
            // 여기서 로그인 여부에 따라 다른 페이지를 나타내게 해보자.
            // 그냥 작업실 탭도 없애고 팔레트는 드로어로 옮겼다.
//            TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
//            ts2.setContent(R.id.content2);
//            ts2.setIndicator("내 작실업");
//            tabHost1.addTab(ts2);

            ConstraintLayout ts2 = findViewById(R.id.content2);
            ts2.setVisibility(View.INVISIBLE);

            // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
            TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
            ts3.setContent(R.id.content3);
            ts3.setIndicator("내 작업실");
            tabHost1.addTab(ts3);
        }

        //탭레이아웃, 뷰페이저 관련 코드

        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(mainViewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        //프로필 화면 관련 코드. 화면을 시작할 때 db에서 받아서 옮겨주면 되려나?
        userName = findViewById(R.id.userName);
        userDesc = findViewById(R.id.userDescription);
        userTitle = findViewById(R.id.rankText);
        userScore = findViewById(R.id.rankNumber);

        userProfileImage = findViewById(R.id.userProfileImage);
        userBackImage = findViewById(R.id.userBackImage);
        userBackImage.setColorFilter(Color.parseColor("#9b9b9b"), PorterDuff.Mode.MULTIPLY);
        userGradeImage = findViewById(R.id.rankImage);

        //그리드뷰 관련 코드
        gridViewAdapter = new profileGridViewAdapter();
        gridView = findViewById(R.id.profileGridView);

        gridView.setAdapter(gridViewAdapter);

        //드로어 레이아웃 관련 코드.
        drawer = findViewById(R.id.drawer);
        drawerItemListAdapter = new DrawerItemListAdapter();

        listview = findViewById(R.id.drawer_menulist);
        listview.setAdapter(drawerItemListAdapter);

        //토글 연결
        dtToggle = new ActionBarDrawerToggle(this, drawer, R.string.app_name, R.string.app_name);
        drawer.setDrawerListener(dtToggle);

    }

    //sp에서 유저 네임을 보내고, DB에서 해당하는 유저 정보를 받아와서 그 정보들을 프로필 창에 표시해주면 된다. 보내는 건 유저의 이름.
    //유저의 정보를 db에서 받아와서 그 정보를 클라이언트에 표시해주는 메소드.
    private void loadProfile() {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

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

                                gotFollowings = product.getString("loadedFollowingUser");
                                gotFollowers = product.getString("loadedFollowedUser");
                                gotMadeDrawings = product.getString("madeDrawingCount");
                                gotCollectedDrawings = product.getString("collectedDrawingCount");

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

                                gridViewAdapter.clear();

                                gridViewAdapter.addItem(gotFollowings, "팔로잉");
                                gridViewAdapter.addItem(gotFollowers, "팔로워");
//                                gridViewAdapter.addItem("0", "협업");
                                gridViewAdapter.addItem(gotMadeDrawings, "그린 그림");
                                gridViewAdapter.addItem(gotCollectedDrawings, "수집한 그림");
//                                gridViewAdapter.addItem("To.", "메시지 보내기");

                                gridViewAdapter.notifyDataSetChanged();

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
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }) {
            //db에 구별용 아이디를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //유저네임이 비어 있으면, 즉 로그아웃 상태이면 아무것도 하지 않는다.
                if (username == null) {

                } else {
                    params.put("username", username);

                }
                return params;
            }

        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void goToChangeProfile(View v) {
        Intent intent = new Intent(getApplicationContext(), ChangeProfile.class);
        startActivity(intent);
    }

    //따로 메소드를 만들고 클릭할 때마다 다른 메소드를 실행하게 해볼까??
    //3개의 탭을 유지하되 , 내용물의 상호작용 방식을 로그인 여부에 따라 다르게 반응해볼까??
    public void login() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        //탭클리어 메소드를 할 때 시간 차를 약간 두지 않으면 제대로 작동하지 않아서 handler로 약간 시간을 끌었다.
        final Runnable r = new Runnable() {
            public void run() {
                tabHost1.clearAllTabs();
            }
        };

        Handler mHandler = new Handler();
        mHandler.postDelayed(r, 100);

        startActivity(intent);
    }

    //로그인된 상태에서 현재 버튼('로그인'상태)을 누르면 로그아웃으로 글자를 바꾸고 sp에 저장된 데이터를 삭제한다.
    public void logout() {
        drawerItemListAdapter.setVoidList();

        drawerItemListAdapter.addItem(R.drawable.ic_mood_bad_black_24dp, "비로그인 상태");
        drawerItemListAdapter.addItem(R.drawable.ic_palette_black_24dp, "팔레트");
//        drawerItemListAdapter.addItem(R.drawable.ic_settings_black_24dp, "설정");
//        drawerItemListAdapter.addItem(R.drawable.ic_notifications_black_24dp, "알림 피드");
        drawerItemListAdapter.addItem(R.drawable.ic_map_black_24dp, "내 주변 작품들");
        drawerItemListAdapter.addItem(R.drawable.ic_arrow_back_black_24dp, "로그인");

        drawerItemListAdapter.notifyDataSetChanged();

        //이건 그냥 이 클래스의 로그아웃 메소드를 실행하기 위한 것이다.
        SharedPrefManager.getInstance(getApplicationContext()).logout();

        //로그아웃 이후의 탭을 변경하기 위한 화면 갱신 코드. 시간차를 주지 않으면 작동하지 않는다. 왜일깡?
        Intent intent = getIntent();
        finish();
        final Runnable r = new Runnable() {
            public void run() {
                tabHost1.clearAllTabs();
            }
        };

        //시간끌기용. 스레드 슬립 말고 이걸 써봐라.
        Handler mHandler = new Handler();
        mHandler.postDelayed(r, 100);

        startActivity(intent);


    }

    @Override
    protected void onStart() {
        super.onStart();

        drawerItemListAdapter.setVoidList();

        //유저의 이름을 드로어 리스트에 띄우기 위해 sp에서 불러온다.
        //여기서는 sp에 저장된 데이터가 필요하므로 getinstance가 아니라 sp를 불러와야 한다.
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            drawerItemListAdapter.addItem(R.drawable.ic_mood_bad_black_24dp, "비로그인 상태");
        } else {
            //로그인을 하고 들어왔으면 이게 로그아웃이 떠야 하는데 로그아웃이 안뜬다...왜지?
            //어댑터에 노티파이를 안해줘서 그렇다.
            drawerItemListAdapter.addItem(R.drawable.ic_mood_black_24dp, username + " 님");
        }

        drawerItemListAdapter.addItem(R.drawable.ic_palette_black_24dp, "팔레트");
//        drawerItemListAdapter.addItem(R.drawable.ic_settings_black_24dp, "설정");
//        drawerItemListAdapter.addItem(R.drawable.ic_notifications_black_24dp, "알림 피드");
        drawerItemListAdapter.addItem(R.drawable.ic_map_black_24dp, "내 주변 작품들");

        //로그인 여부에 따라 리스트 아이템이 달라지게 한다.
        //로그인이 안 되어 있다면
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            drawerItemListAdapter.addItem(R.drawable.ic_arrow_back_black_24dp, "로그인");
        } else {

            drawerItemListAdapter.addItem(R.drawable.ic_arrow_left_black_24dp, "로그아웃");
        }

        drawerItemListAdapter.notifyDataSetChanged();
    }

    //여기서 프래그먼트를 가져와 보자.
    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();

        //드로어 안에 리스트뷰 클릭 시
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                //리스트 아이템의 개수가 늘어나면 위치가 바뀜에 따라 작동을 하던 것들이 안 할 수도 있다. 참고.
                switch (position) {
                    case 0:

                        break;
                    case 1: // 팔레트

                        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
                            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                        } else { DialogSelectOption(); }
                        break;
//                    case 2: // 설정
////                        Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
//                        Intent intent2 = new Intent(TabActivity.this,MainActivity.class);
////                        인텐트를 보냄으로써 서비스를 시작하고 종료할 수 있다.
//                        startActivity(intent2);
//
//                        break;
//                    case 3: // 알림 피드
//                        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
//                            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
//                        } else {
////                            Toast.makeText(getApplicationContext(),"Service 종료",Toast.LENGTH_SHORT).show();
////                            Intent intent3 = new Intent(TabActivity.this,MyService.class);
////                            stopService(intent3);
//                        }
//                        break;
                    case 2: // 내 주변 지도

                        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
                            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainMapActivity.class);
                            startActivity(intent);

                        }
                        break;
                    case 3: // 로그아웃/로그인
                        //로그아웃 상태라면
                        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
                            login();
                        } else {
                            logout();
                        }
                        break;
                }
//                drawer.closeDrawer(Gravity.LEFT);

            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPageNumber = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onTabChanged(String tabId) {
        // Tab 색 변경 관련 메소드
        for (int i = 0; i < tabHost1.getTabWidget().getChildCount(); i++) {
            tabHost1.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#fbfcf9"));
        }
        tabHost1.getTabWidget().getChildAt(tabHost1.getCurrentTab()).setBackgroundColor(Color.parseColor("#e4f2c9"));
        //->모든 탭들을 일괄적으로 같은 색으로 변경, 그리고 현재 제일 맨 위에 보여지는 탭을 선택된 색깔로 변경
        if (tabHost1.getCurrentTab() == 2) {
            //마지막탭을 클릭했을 때 툴바 색깔 변경
            toolbar.setBackgroundColor(getResources().getColor(R.color.검정투명));
            toolbar.setTitle("프로필");

        } else if (tabHost1.getCurrentTab() == 0) {
            //다른 탭을 클릭했을 때 툴바 색깔 원위치
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            toolbar.setTitle("둘러보기");
        } else {
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            toolbar.setTitle("내 작업실");
        }
    }

    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu3, menu);

        // SearchView Hint 변경하기
//        MenuItem searchItem = menu.findItem(R.id.button_search);

//        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
////                Intent intent = new Intent(TabActivity.this, SearchActivity.class);
////                startActivity(intent);
//                return false;
//            }
//        });

        return super.onCreateOptionsMenu(menu);
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (dtToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
//            case R.id.action_settings:
//                // User chose the "Settings" item, show the app settings UI...
//                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
//                return true;
//            default:
//                // If we got here, the user's action was not recognized.
//                // Invoke the superclass to handle it.
//                Toast.makeText(getApplicationContext(), "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show();
//                return super.onOptionsItemSelected(item);
//        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        dtToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dtToggle.onConfigurationChanged(newConfig);
    }

    public void goToCanvases(View v) {

        if (!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        } else {
            DialogSelectOption();
        }

    }

    //그리기 클릭했을 때 캔버스의 가로/세로 결정하는 다이얼로그.
    private void DialogSelectOption() {
        final String items[] = {"가로", "세로"};
        AlertDialog.Builder ab = new AlertDialog.Builder(TabActivity.this);
        ab.setTitle("캔버스 형태");
        ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정

            public void onClick(DialogInterface dialog, int index) {

                if (index == 0) {//가로
                    Intent intent = new Intent(getApplicationContext(), BestPaintBoardActivity.class);
                    startActivity(intent);

                    Log.e("가로 선택", "");

                } else if (index == 1) {//세로
                    Intent intent = new Intent(getApplicationContext(), BestPaintBoardActivityVertical.class);
                    startActivity(intent);

                    Log.e("세로 선택", "");
                }
            }
        });
        ab.show();
    }
}

