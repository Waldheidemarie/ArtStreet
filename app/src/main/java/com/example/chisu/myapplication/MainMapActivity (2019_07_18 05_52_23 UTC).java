package com.example.chisu.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chisu.myapplication.ar.ARActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//메인 화면에서 지도 아이콘을 클릭하면 나타나는 지도 클래스.
public class MainMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    Toolbar myToolbar;

    ImageButton collectBtn;
    ImageButton pictureBtn;

    //데이터를 받아올 로케이션리스트.
    private List<art> locationList;

    //The request counter to send ?page=1, ?page=2 requests
    private int requestCount = 1;

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap;
    private Marker currentMarker = null;

    //로그 찍을 때 사용할 태그.
    private static final String TAG = "googlemap_example";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;

    //지도 업데이트 시간
    private static final int UPDATE_INTERVAL_MS = 10000;  // 10초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 5000; // 5초

    //appcompatactivity는 일반 액티비티와 달리 아래 버전들과도 호환된다는 특징이 있다.
    private AppCompatActivity mActivity;

    //한번 더 물어보는지에 대한 변수
    boolean askPermissionOnceAgain = false;
    boolean mRequestingLocationUpdates = false;

    //로케이션 변수 - 현재 위치를 표시함.
    Location mCurrentLocation;

    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;

    //위도와 경도. latitude longtitude
    LatLng currentPosition;
    LatLng previousPosition = null;

    //로케이션 요청 설정 - 인터벌 설정. 이걸 조정하면 스니펫이 사라지는 걸 막을 수 있을지도 모른다.
    //맞았다. 예쓰.
    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

    //지도 내 액자 뷰
    View mCustomMarkerView;
    ImageView mMarkerImageView;

    //발리로 서버에 보내기위한 위도/경도
    String sendedLatitude;
    String sendedLongtitude;

    private LocationManager locationMgr;
    Location location;

    //사용자의 현재 위치와 클릭한 마커 사이의 거리를 나타내는 변수. 변환을 위해 두 개로 만듦.
    double distance1;
    static double distance2;

    //선택한 마커의 아이템 id
    int markerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_map);

        collectBtn = findViewById(R.id.collectBtn);
        pictureBtn = findViewById(R.id.pictureBtn);

        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectDrawing();

            }
        });
        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureDrawing();
            }
        });

        mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        mMarkerImageView = mCustomMarkerView.findViewById(R.id.profile_image);

        //툴바 관련 코드들
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //추가된 소스코드, Toolbar의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);
        getSupportActionBar().setTitle("내 주변");

        locationList = new ArrayList<>();

        //구글맵 관련 코드들
        Log.e(TAG, "onCreate");
        //현재 액티비티를 mActivity에 대입한다.
        mActivity = this;

        //구글api클라이언트 객체 생성.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        //true=현재 이용가능한 공급자 제한 (return String)
        //false (return List<String>)
        String bestProvider = locationMgr.getBestProvider(criteria, true);

        //위치 서비스가 작동되고 있는지 체크. 작동하고 있지 않다면 바로 알림창을 띄워준다.
        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

            //위치 서비스가 작동하고 있다면 위치를 따낸다.
        } else {
            //매니페스트에서 위치 허용을 했는지 여부 체크.
            //coarse location은 대략적 위치, fine location은 자세한 위치.
            //원하는 정확도에 따라 둘 중 하나만 요청해야 한다.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            location = locationMgr.getLastKnownLocation(bestProvider);

            sendedLatitude = String.valueOf(location.getLatitude());
            sendedLongtitude = String.valueOf(location.getLongitude());
        }

        FragmentManager fragmentManager = getFragmentManager();
        //밑의 문장은 액티비티에서만 이용가능. 프래그먼트에서 써야할 경우에는 mapview인가를 써야 함.
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.mainMap);
        mapFragment.getMapAsync(this);

        markerSet = new Hashtable<>();

    }

    //수집 버튼을 눌렀을 때 발동하는 메소드. 현재 사용자의 이름과 클릭한 마커의 아이템의 아이디를 보내주면 된다.
    public void collectDrawing() {

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        final String itemId2 = String.valueOf(markerId);
        final String username2 = username;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.DRAWING_URL + "collectDrawings",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        //과거에 수집한 경력이 없으면 수집됐다고 띄우고 없으면 더이상 수집불가라고 띄운다.
                        if (response.equals("true")) {
                            Toast.makeText(getApplicationContext(), "이미 수집한 작품입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "수집 완료했습니다.", Toast.LENGTH_SHORT).show();
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

                Log.e("collect 메소드 완료했습니다, 값은 ", itemId2 + "," + username2);

                return params;
            }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    //지도에서 선택한 그림을 카메라로 끌고가는 메소드.
    public void pictureDrawing() {
        Intent intent = new Intent(MainMapActivity.this, ARActivity.class);
        //아이템 구분용 마커아이디를 보내준다.
        intent.putExtra("itemId", String.valueOf(markerId));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {

            Log.e(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //구글 api클라이언트가 연결되었는지 확인해서 연결되었으면 로그를 띄운다.
        if (mGoogleApiClient.isConnected()) {
            Log.e(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {
            //sdk 버전이 마시멜로 이상이라면 체크퍼미션을 해본다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;
                checkPermissions();
            }
        }
    }

    @Override
    protected void onStop() {

        if (mRequestingLocationUpdates) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    //지속적인 로케이션 업데이트 시작 메소드.
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            //매니페스트에 위치 허용을 했는지 여부 체크.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

            mGoogleMap.setMyLocationEnabled(true);

        }
    }

    //onStop에서 지속적으로 해 오던 로케이션 업데이트를 중단하는 것.
    private void stopLocationUpdates() {

        Log.e(TAG, "stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        mRequestingLocationUpdates = false;
    }

    Hashtable<String, Boolean> markerSet;

    //맵이 사용할 준비가 됐을 때 호출되는 메소드
    @Override
    public void onMapReady(final GoogleMap map) {

        Log.e(TAG, "onMapReady :");

        //메소드의 인자를 선언해 두었던 구글맵 객체에 할당.
        //즉 지금부터 발생하는 모든 메소드는 우리의 구글맵 객체에 적용된다.
        mGoogleMap = map;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //내 위치 버튼 표시하도록 설정.
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //지도 초기 화면 및 줌 설정
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override //버튼을 클릭할 때마다
            public boolean onMyLocationButtonClick() {

                Log.d(TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });

        //손가락으로 유저가 카메라를 움직일 때 발동하는 메소드.
        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates) {

                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }
                mMoveMapByUser = true;
            }
        });

        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {

            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                try{
                    markerId = locationList.get(Integer.parseInt(marker.getSnippet())).getProductId();
                }catch (NumberFormatException e){
                    //만약 자기 자신의 마커를 클릭해서 에러가 발생하면 아무것도 하지 않는다.
                    return false;
                }

                //공부용으로 남겨놓음.
//                LatLng location = marker.getPosition();

                distance1 = SphericalUtil.computeDistanceBetween(currentPosition, marker.getPosition());
                //소수점 2자리까지 표시하도록 함.
                distance2 = Math.round(distance1 * 100d) / 100d;

                //만약 사용자의 위치와 선택한 마커의 위치 사이가 50미터 이상이라면,
                //수집을 할 수 없도록 invisible로 만들어버린다.
                if (distance2 < 1000) {
                    collectBtn.setVisibility(View.VISIBLE);
                    pictureBtn.setVisibility(View.VISIBLE);
                } else {
                    collectBtn.setVisibility(View.INVISIBLE);
                    pictureBtn.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        getData();

//        setUpClusterer();

        //인포 윈도우 설정을 위한 인포 윈도우 어댑터 적용
        mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter(this, markerSet, locationList));

        //추가검색버튼을 눌러주면 된다.
        //이걸 하면 마커에 넣기 직전까지 모든 과정이 완료됨.
        Log.e("맵레디", "완료");
    }

    //웹에서 데이터 받기
    private void getData() {
        //리퀘스트큐에 메소드 올리기
        VolleySingleton.getInstance(this).addToRequestQueue(getDataFromServer(requestCount));

        //리퀘스트 카운터 +1
        requestCount++;
    }

    //뷰에서 비트맵을 가져오는 메소드
    /**
     * @param view   is custom marker layout which we will convert into bitmap.
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    //데이터 받아오는 발리 리퀘스트
    private StringRequest getDataFromServer(final int requestCount) {

        //추가 로드 시 지역을 옮겨서 갱신 버튼을 누르면 됨. 아니지. 멀리있는 작품을 보고싶을 수도 있잖아.
        //볼수 있는 범위는 캐시 아이템을 사면 더 볼 수 있게 만들자. 왜냐? 서버 비용을 더 내야 하기 때문이다.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.LOCATION_FROM_MAPS + "watchAroundHere&page=" + String.valueOf(requestCount),

                //요러면 되자너
                new Response.Listener<String>() { //json 배열을 String 배열로 바꿔서 해볼까?

                    //일단 서버에서 json 배열이 넘어온다는 것은 부정할 수 없다.
                    //왜냐하면 현재 클라이언트와 서버의 언어가 다르기 때문이다.
                    @Override
                    public void onResponse(String response) {

                        try {
                            //넘어온 스트링을 JsonArray로 만든다.
                            JSONArray gotJsonArray = new JSONArray(response);
                            //그 jsonarray에 파스데이터를 실행한다.
                            Log.e("데이터 파싱", "완료");
                            parseData(gotJsonArray);

                            //작품들의 마커 생성하는 코드. 파스데이터와는 별개.
                            for (int i = 0; i < locationList.size(); i++) {

                                //마커의 번호를 나타내는 변수. 이 숫자는 리스트의 번호와 일치한다.
                                //마커를 클릭했을 때 인포 윈도우의 4개 정보를 나타내기 위한 초석이다.
                                final String markerNumber = String.valueOf(i);

                                final String markerTitle = locationList.get(i).getTitle();
                                String markerLatitude = locationList.get(i).getLatitude();
                                String markerLongtitude = locationList.get(i).getLongtitude();

                                String markerImage = locationList.get(i).getImage();

                                //db에서 받아온 정보를 int화시켜서 새로이 만든 latlng 객체에 저장한다.
                                final LatLng locLatLng = new LatLng(Double.parseDouble(markerLatitude), Double.parseDouble(markerLongtitude));

                                //전역변수가 꼭 정답인 것만은 아니다. 웬만하면 지역변수를 쓰라는 게 이런 이유에서였구나.

                                //마커에 이미지를 입히는 작업.
                                Glide.with(getApplicationContext()).asBitmap()
                                        .load(markerImage)
                                        .into(new SimpleTarget<Bitmap>() {

                                            @Override
                                            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {

                                                MarkerOptions markerOptions = new MarkerOptions()
                                                        .position(locLatLng)
                                                        .title(markerTitle)
                                                        .snippet(markerNumber)
                                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, bitmap)));

                                                Marker marker = mGoogleMap.addMarker(markerOptions);

                                                markerSet.put(marker.getId(), false);

//                                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 13f));
                                            }
                                        });
                            }
                            Log.e("마커 설치", "완료");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //If an error occurs that means end of the list has reached
                        Toast.makeText(MainMapActivity.this, "GoogleMap 에러 발생", Toast.LENGTH_SHORT).show();
                        Log.e("지도 중 에러 발생", "에러 발생");
                    }
                }) {
            //여기서 데이터를 넣는 것 같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>(); //파라미터스
                params.put("nowLatitude", sendedLatitude);
                params.put("nowLongtitude", sendedLongtitude);

                Log.e("위도경도 전송", "완료");

                return params;
            }
        };

        //리퀘스트를 큐에 add해주면 끝.
        //jsonRequest는 작동이 안될 확률이높다. 스트링리퀘스트는 확실히 작동함.
        return stringRequest;
    }

    //받아온 json 데이터를 해석해서 리스트에 넣어주는 과정.
    private void parseData(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {

            //리스트에 저장될 art 객체 생성
            art artExample = new art();
            //json어레이의 원소 하나를 받을 json 객체 생성.
            JSONObject json = null;
            try {
                //Getting json
                json = array.getJSONObject(i);

                artExample.setProductId(json.getInt("productId"));
                artExample.setImage(json.getString("image"));
                artExample.setTitle(json.getString("title"));
                artExample.setUser(json.getString("user"));
                artExample.setLatitude(json.getString("latitude"));
                artExample.setLongtitude(json.getString("longtitude"));
                artExample.setValue(json.getString("value"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //일단 데이터를 받아와서 리스트에 올리는 것은 된다.
            locationList.add(artExample);
        }
        //지속적으로 지도가 갱신되기 때문에 굳이 갱신을 해줄 필요가 없을 것이다.
    }


    //디바이스의 위치가 변화되었을 때 사용하는 메소드. 이 메소드는 계속 실행되는 거 같다.
    //리스트의 데이터를 마커에 옮겨야 한다.
    @Override
    public void onLocationChanged(Location location) {
        //여기에다 반복문으로 마커를 설치한다면 될 거 같다.
        Log.e(TAG, "onLocationChanged : ");

        //이전 위치 설정
        previousPosition = currentPosition;

        //이전 위치를 설정한 후 현재 위치 새로 설정
        currentPosition
                = new LatLng(location.getLatitude(), location.getLongitude());

        if (previousPosition == null) previousPosition = currentPosition;

        //사용자의 현재 위치에 마커를 생성하는 코드

        //String markerTitle = getCurrentAddress(currentPosition);
        //String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());

        //자신의 현재 위치를 클릭했을 때의 에러를 방지하기 위해 구분용으로 마커타이틀을 "현재 위치"로 바꾸었다.
        String markerTitle = "현재 위치";
        //스니펫도 위도 경도 대신 현재 위치를 사용.
        String markerSnippet = getCurrentAddress(currentPosition);

        setCurrentLocation(location, markerTitle, markerSnippet);
        mCurrentLocation = location;

    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mRequestingLocationUpdates == false) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {

                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {

                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {

        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    //현재 위치를 표시하는 메소드
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        //여기서 db에서 데이터를 받아와서 하면 될 거 같다. 공부하기 싫다...크앙

        mMoveMapByUser = false;
        //currentMarker에 뭔가 할당돼 있다면 제거한다.
        if (currentMarker != null) currentMarker.remove();

        //위도경도따는 메소드로 위도와 경도를 따서 위도와 경도를 동시에 저장할 수 있는 latlng 변수에 할당.
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //현재 위치를 지도에 나타낼 마커 생성.
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);

        //마커를 원하는 이미지로 변경하여 현재 위치 표시하도록 수정 fix - 2017. 11.27
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_circle_black_25dp));

        currentMarker = mGoogleMap.addMarker(markerOptions);

        //api에 의해서 지도가 이동했다면?
        if (mMoveMapByAPI) {

            Log.d(TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 20);
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    //모든 것에 앞서 초기위치를 서울로 설정하기
    public void setDefaultLocation() {

        mMoveMapByUser = false;

        //디폴트 위치, Seoul
//        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
//        String markerTitle = "위치정보 가져올 수 없음";
//        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";
//
//        if (currentMarker != null) currentMarker.remove();
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(DEFAULT_LOCATION);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        currentMarker = mGoogleMap.addMarker(markerOptions);

//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
//        mGoogleMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 로케이션 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");

            if (mGoogleApiClient.isConnected() == false) {

                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {

                if (mGoogleApiClient.isConnected() == false) {

                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }

            } else {
                checkPermissions();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainMapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainMapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainMapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("지도를 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정합니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");

                        if (mGoogleApiClient.isConnected() == false) {

                            Log.d(TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void loadPlus(View v) {
        getData();
    }

//    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu, menu);
//
//        // SearchView Hint 변경하기
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setQueryHint("거리의 미술관");
//
//        // SearchView 확장/축소 이벤트 처리
//        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                Toast.makeText(MainMapActivity.this, "SearchView 확장됐다!!", Toast.LENGTH_LONG).show();
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                Toast.makeText(MainMapActivity.this, "SearchView 축소됐다!!", Toast.LENGTH_LONG).show();
//                return true;
//            }
//        };
//
//        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
//
//        // SearchView 검색어 입력/검색 이벤트 처리
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(MainMapActivity.this, "[검색버튼클릭] 검색어 = "+query, Toast.LENGTH_LONG).show();
//                return true;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
////                Toast.makeText(MainMapActivity.this, "입력하고있는 단어 = "+newText, Toast.LENGTH_LONG).show();
//                return true;
//            }
//        });
//
////        return true;
//        return super.onCreateOptionsMenu(menu);
//
//    }
//    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
////        switch (item.getItemId()) {
////            case R.id.action_settings:
////                // User chose the "Settings" item, show the app settings UI...
////                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
////                return true;
////            default:
////                // If we got here, the user's action was not recognized.
////                // Invoke the superclass to handle it.
////                Toast.makeText(getApplicationContext(), "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show();
////                return super.onOptionsItemSelected(item);
////        }
//    }

}
