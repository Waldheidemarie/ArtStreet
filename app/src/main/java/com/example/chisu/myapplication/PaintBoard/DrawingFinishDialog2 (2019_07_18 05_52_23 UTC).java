package com.example.chisu.myapplication.PaintBoard;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chisu.myapplication.MainActivity;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.SharedPrefManager;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.VolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DrawingFinishDialog2 extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;

    private GoogleApiClient mGoogleApiClient = null;

    boolean askPermissionOnceAgain = false;

    //필요하니 지우면 안됨
    private AppCompatActivity mActivity;

    //이 액티비티를 종료할 때 베스트페인트액티비티도 종료하기 위해 만든 것.
    BestPaintBoardActivity bBcitivity = (BestPaintBoardActivity) BestPaintBoardActivity.BBctivity;

    BestPaintBoardActivityVertical aAcitivity = (BestPaintBoardActivityVertical) BestPaintBoardActivityVertical.BBctivity;

    private LocationManager locationMgr;

    TextView titleView;
    Button rightBtn;
    Button cancelBtn;

    String gotArtName;
    String stringedBitmap;
    String drawScore;

    Bitmap bitmap;

    String latitude;
    String longtitude;

    Location location;

    String direction;

    //구글맵 관련 부분도 리팩토링 좀 해야 한다...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.drawing_finish_dialog2);

        titleView = findViewById(R.id.titleView);
        rightBtn = findViewById(R.id.rightButton);
        cancelBtn = findViewById(R.id.closeButton);

        Intent intent = getIntent();
        direction = intent.getStringExtra("direction");
        gotArtName = intent.getStringExtra("inputedArtName");
        drawScore = intent.getStringExtra("drawScore");
        Log.e("artname", gotArtName);

        try {
            //베스트 페인트 보드의 스태틱 비트맵을 가져와서 이곳의 새로운 비트맵 객체에 담는다.
            if (direction.equals("vertical")) {
                bitmap = BestPaintBoardActivityVertical.bitmap;
            } else {
                bitmap = BestPaintBoardActivity.bitmap;

            }
            //담은 비트맵 객체를 스트링화 시켜서 스트링 객체에 담는다.
            stringedBitmap = BitMapToString(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

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

            latitude = String.valueOf(location.getLatitude());
            longtitude = String.valueOf(location.getLongitude());

        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        titleView.setText("\"" + gotArtName + "\" (이)가 맞습니까?");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //작품명 최종 확인이 끝나면 그림 파일을 데이터베이스에 추가한다.
        rightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                artWrite();

                finish();
                //여기가 문제였다. (작품 추가 후 보드액티비티로 넘어갔을 때 갱신이 되지 않는 현상)
                //순서의 중요성. 논리에서 순서란 매우 중요한 것이다.
//                startActivity(new Intent(getApplicationContext(), TabActivity.class));


                SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
                String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);

                intent1.putExtra("username", username);
                startActivity(intent1);

                if (direction.equals("vertical")) {
                    //세로라면 aActivity 종료
                    aAcitivity.finish();
                } else {
                    bBcitivity.finish();
                }

            }
        });

        //checking the permission
        //if the permission is not given we will open setting to add permission
        //else app will not open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            finish();
            startActivity(intent1);

        }
    }

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */

    // 버튼을 누르면 두 메소드를 차례차례...가 아니라 한 번에 보내야 할 거 같은데??
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    //갤러리 갱신을 위한 메소드.
    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath); //새로고침할 사진경로
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void artWrite() { //보내야 할 것들 : 작품 제목, 작품 이미지, 유저 이름, 작품 설명, 경도, 위도

        SharedPreferences sharedPreferences =
                getApplicationContext().getSharedPreferences("simplifiedcodingsharedpref", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("keyusername", " ");

        //작품의 제목
        final String artName = gotArtName;
        //화가의 이름
        final String userName = username;
        //올라가는 그림
        final String artImage = stringedBitmap; //그러면 스트링드 비트맵을 옮기지 말고, uri로 계속 옮기다가 여기서 스트링으로 만들어서 보내는 전략을 써 보자.

        final String latitude1 = latitude;
        final String longtitude1 = longtitude;

        //파일화 과정.
        FileOutputStream out;

        String filename = System.currentTimeMillis() + ".jpg";
        String temp = "/Pictures/" + filename;

        try {
            out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + temp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);

            //갤러리 갱신
            galleryAddPic(Environment.getExternalStorageDirectory().toString() + temp);

//            Toast.makeText(getApplicationContext(), temp + "에 저장되었습니다", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("screenshot", String.valueOf(e));
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.UPLOAD_URL,

                //3번째 인자 Listener는 AysncTask의 postExecute(..)와 유사합니다.
                //여기서 결과를 파싱하고 View에 보여줄 아이템 목록을 추가하고, 데이터셋이 변경되었다는 것을 통지합니다.
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //에러가 없고 정상적일 때
                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
//                                Toast.makeText(getApplicationContext(), "전송 성공", Toast.LENGTH_SHORT).show();

                                //starting the profile activity
                            } else { //에러 시 서버에서 해당하는 메시지를 받아서 표시해준다.
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "전송 실패!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                //StringRequset 메소드의 4번째 인자
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "에러 발생", Toast.LENGTH_SHORT).show();
                    }
                }) {
            //인자 이후의 시행부분.
            //여기서 데이터를 넣는 것 같다... 처음에 정의한 걸 넣어 주었다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>(); //파라미터스
                params.put("username", userName);
                params.put("artname", artName);
                if (artImage == null){
                    Log.e("artimage is null", "");
                }
                params.put("artimage", artImage);
                params.put("latitude", latitude1);
                params.put("longtitude", longtitude1);
                params.put("drawScore", drawScore);
                Log.e("정보 전송", "완료");
                return params;
            }
        };

        //리퀘스트를 큐에 add해주면 끝. 위에 긴 걸 다 올려준다.....라...
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingFinishDialog2.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingFinishDialog2.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingFinishDialog2.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("작품 등록을 위해서는 위치 서비스가 필요합니다.\n"
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


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
