package com.example.chisu.myapplication.ar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.VolleySingleton;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    //서피스뷰를 담는 카메라 컨테이너 레이아웃.
    private FrameLayout cameraContainerLayout;

    //카메라에 겹쳐 나타나는 그림 오버레이뷰.
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;

    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    //업데이트를 위한 최소 이동 거리.
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    //업데이트 최소 시간
    private static final long MIN_TIME_BW_UPDATES = 10000;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    String itemId;

    String drawingImage, drawingTitle, drawingLatitude, drawingLongtitude;

    LayoutInflater controlInflater = null;

    public static Bitmap shareBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        Intent intent = getIntent();
        itemId = intent.getStringExtra("itemId");

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.control, null);
        ViewGroup.LayoutParams layoutParamsControl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
        //센서매니저 초기화
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = findViewById(R.id.camera_container_layout);
        surfaceView = findViewById(R.id.surface_view);
        tvCurrentLocation = findViewById(R.id.tv_current_location);

       CircularImageView  buttonTakePicture = findViewById(R.id.takepictureBtn);

        buttonTakePicture.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //previewing이 true라면
                if (arCamera.previewing) {
                    Log.e("previewing", "true");

                    //사진찍는 효과만 내기 위한 용도임. 실제 작업 거의 없음.
                    //사진찍기 버튼 클릭 시 서피스뷰에서 카메라 이미지 정보가 날아온다. 그 위에 오버레이를 얹은 다음에 이미지를 저장해 준다.
                    camera.takePicture(arCamera.myShutterCallback, arCamera.myPictureCallback_RAW, arCamera.myPictureCallback_JPG);

                    //배경으로 쓸 카메라 이미지의 특성을 가져와서 만든다. 이 때는 그냥 모양만 잡음.
                    Bitmap overlay = Bitmap.createBitmap(shareBitmap.getWidth(), shareBitmap.getHeight(), shareBitmap.getConfig());
                    //그 비트맵을 캔버스로 만든다.
                    Canvas canvas = new Canvas(overlay);
                    //캔버스 위에 카메라 이미지를 그린다.
                    canvas.drawBitmap(shareBitmap, 0, 0, null);

                    //오버레이뷰를 비트맵화한다.
                    arOverlayView.buildDrawingCache();
                    Bitmap bm = arOverlayView.getDrawingCache();

                    //오버레이뷰를 비트맵 위에 그린다.
                    canvas.drawBitmap(bm, 0, 0, null);

                    //파일화 과정.
                    FileOutputStream out;

                    String filename = System.currentTimeMillis() + ".jpg";
                    String temp = "/Pictures/" + filename;

                    try {
                        out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + temp);
                        overlay.compress(Bitmap.CompressFormat.JPEG, 30, out);

                        //갤러리 갱신
                        galleryAddPic(Environment.getExternalStorageDirectory().toString() + temp);

                        Toast.makeText(getApplicationContext(), temp + "에 저장되었습니다", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d("screenshot", String.valueOf(e));
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //갤러리 갱신을 위한 메소드.
    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath); //새로고침할 사진경로
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        loadProduct();

    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    //DB에 있는 데이터들을 가져와 자바리스트에 넣어주는 메소드. 발리.
    public void loadProduct() {

        Log.d("로드 프로덕트시작. : ", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.AR_URL + "watch",
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

                                drawingTitle = jsonobject.getString("drawingTitle");
                                drawingImage = jsonobject.getString("drawingImage");
                                drawingLatitude = jsonobject.getString("drawingLatitude");
                                drawingLongtitude = jsonobject.getString("drawingLongtitude");

                                Log.e("그림의 이름", drawingTitle);
                                Log.e("그림의 이미지", drawingImage);
                                Log.e("그림의 위도", drawingLatitude);
                                Log.e("그림의 경도", drawingLongtitude);

                                //여기서 그림을 띄운다.
                                arOverlayView = new AROverlayView(ARActivity.this, drawingTitle, drawingLatitude, drawingLongtitude, drawingImage);
                                registerSensors();
                                initAROverlayView();
                            }
                            Log.v("애드 완료 : ", "");

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

    //카메라 퍼미션을 확인하고 있으면 ARCameraView를 시작한다.
    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            //카메라뷰를 시작한다.
            initARCameraView();
        }
    }

    //로케이션 퍼미션을 확인하고 LocationService를 시작한다.
    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
            Log.e("initLocationService", "시작");

        }
    }

    //AROverlayView 시작.
    public void initAROverlayView() {
        //뭔가가 있다면 정리해준다.
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        //그리고 카메라뷰에 오버레이뷰를 얹어준다.
        cameraContainerLayout.addView(arOverlayView);
    }

    //카메라뷰 초기화 메소드.
    public void initARCameraView() {
        //서피스뷰 초기화해주고
        reloadSurfaceView();

        if (arCamera == null) { //arCamera가 null이라면 새로 객체를 만들어준다.
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) { //뭔가 갖고 있다면 정리해준다.
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        //카메라 컨테이너레이아웃에 올려준다.
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    //카메라 시작 메소드.
    private void initCamera() {
        //카메라의 갯수 구하기.
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    //서피스뷰 초기화
    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) { //뭔가 있으면 제거해 준다.
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        //서피스뷰를 카메라컨테이너뷰에 넣어준다.
        cameraContainerLayout.addView(surfaceView);
    }

    //카메라 종료. onPause에서 발동.
    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    //센서 등록 메소드.
    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    //센서 위치가 변했을 때 발동하는 메소드.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    private void initLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.e("initLocationService", "중간1");

        try {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

//            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.e("initLocationService", "중간3");

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.e("initLocationService", "중간4");

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();

                }
            }

            if (isGPSEnabled) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.e("initLocationService", "중간5");

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();

                }
            }
        } catch (Exception ex) {
            Log.e("initLocationService", ex.getMessage());

        }
    }

    //사진을 다 로드하면 글자를 지워준다.
    private void updateLatestLocation() {
        Log.e("updateLatestLocation", "시작");

//        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);

            tvCurrentLocation.setText("");
//            Toast.makeText(getApplicationContext(), "사진 로드 완료", Toast.LENGTH_SHORT).show();
            Log.e("updateLatestLocation", "완료");

//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
