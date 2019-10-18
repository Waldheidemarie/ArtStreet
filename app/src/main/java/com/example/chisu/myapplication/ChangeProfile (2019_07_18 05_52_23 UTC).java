package com.example.chisu.myapplication;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeProfile extends AppCompatActivity implements View.OnClickListener{

    TabActivity aActivity = (TabActivity)TabActivity.AActivity;

    Uri photoUri;
    Bitmap photoBitmap;

    //프로필 사진이 변경되었는지 확인하는 전역변수.
    //이 변수의 상태에 따라 실행되는 발리 메소드가 달라진다.
    boolean isPhotoChanged = false;

    Toolbar toolbar;

    EditText introduce;

    ImageButton camera;
    ImageButton gallery;

    ImageView thumbProfile;
    Bitmap thumbImage;

    String gotProfileUrl;
    String gotUserDesc;

    //카메라, 갤러리 관련 코드
    private static final int PICK_FROM_CAMERA = 1; //카메라 촬영으로 사진 가져오기
    private static final int PICK_FROM_ALBUM = 2; //앨범에서 사진 가져오기
    private static final int CROP_FROM_CAMERA = 3;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);

        checkPermissions();

        toolbar = findViewById(R.id.profile_change_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_23dp);
        getSupportActionBar().setTitle("프로필 편집");

        thumbProfile = findViewById(R.id.userProfileImage);

        introduce = findViewById(R.id.introduce);
        camera = findViewById(R.id.cameraBtn);
        gallery = findViewById(R.id.galleryBtn);

        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        earlyLoad();
    }

    //변경 전 초기 상태를 로드하는 메소드.
    private void earlyLoad(){

        //사용자의 이름을 보내기 위해 sp에서 꺼내온다.
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

//                                gotUserName = product.getString("loadedUsername");
                                gotUserDesc = product.getString("loadedUserdesc");
                                gotProfileUrl = product.getString("loadedUserimage");
                                //adding the product to product list

                                Glide.with(getApplicationContext())
                                        .load(gotProfileUrl)
                                        .into(thumbProfile);

//                                userName.setText(gotUserName);
                                introduce.setText(gotUserDesc);
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
                        Toast.makeText(getApplicationContext(),  "에러 발생!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }){
            //db에 구별용 아이디를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //유저네임이 비어 있으면, 즉 로그아웃 상태이면 아무것도 하지 않는다.
                if (username == null){

                } else {
                    params.put("username", username);

                }

                return params;

            }

        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //사진을 찍기 위하여 설정합니다.
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(ChangeProfile.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
                photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
                        BuildConfig.APPLICATION_ID + "com.example.jisu7.dot4.provider", photoFile);
            } else {//sdk 23 이하, 7.0 미만
                photoUri = Uri.fromFile(photoFile);
            }            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); //사진을 찍어 해당 Content uri를 photoUri에 적용시키기 위함
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK); //ACTION_PICK 즉 사진을 고르겠다!
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // Android M에서는 Uri.fromFile 함수를 사용하였으나 7.0부터는 이 함수를 사용할 시 FileUriExposedException이
    // 발생하므로 아래와 같이 함수를 작성합니다. 이전 포스트에 참고한 영문 사이트를 들어가시면 자세한 설명을 볼 수 있습니다.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "IP" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/test/"); //test라는 경로에 이미지를 저장하기 위함
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpeg",
                storageDir
        );
        return image;
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.cameraBtn:
                takePhoto();
                break;
            case R.id.galleryBtn:
                goToAlbum();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(ChangeProfile.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == PICK_FROM_ALBUM) {
            if(data==null){
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            cropImage();
            MediaScannerConnection.scanFile(ChangeProfile.this, //앨범에 사진을 보여주기 위해 Scan을 합니다.
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else if (requestCode == CROP_FROM_CAMERA) {
            try { //저는 bitmap 형태의 이미지로 가져오기 위해 아래와 같이 작업하였으며 Thumbnail을 추출하였습니다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                 thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bs); //이미지가 클 경우 OutOfMemoryException 발생이 예상되어 압축

                //여기서는 ImageView에 setImageBitmap을 활용하여 해당 이미지에 그림을 띄우시면 됩니다.
                thumbProfile.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
        }
    }

    //Android N crop image (이 부분에서 몇일동안 정신 못차렸습니다 ㅜ)
    // 모든 작업에 있어 사전에 FALG_GRANT_WRITE_URI_PERMISSION과 READ 퍼미션을 줘야
    // uri를 활용한 작업에 지장을 받지 않는다는 것이 핵심입니다.
    public void cropImage() {
        isPhotoChanged = true;

        this.grantUriPermission("com.android.camera", photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 3);
            intent.putExtra("aspectY", 3);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/test/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
                photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
                        BuildConfig.APPLICATION_ID + "com.example.jisu7.provider", tempFile);
            } else {//sdk 23 이하, 7.0 미만
                photoUri = Uri.fromFile(tempFile);
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }


    //툴바 관련 메소드.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu2, menu);

        // 툴바의 저장 버튼 만들기
        MenuItem saveItem = menu.findItem(R.id.button_save);

        saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (isPhotoChanged){
                    saveChanges();
                    aActivity.finish();

                }else {
                    saveChangesWithoutPhoto();
                    aActivity.finish();
                }

                finish();
                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                startActivity(intent);
                return false;
            }
        });

//        return true;
        return super.onCreateOptionsMenu(menu);

    }


    public void uriToBitmap(Uri uri){
        try{
            photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);

        }catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();


    }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,30, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    //카메라 혹은 갤러리에서 돌아오지 않았기 때문에 uri에 아무것도 없고, 그렇기에 널포인터 익셉션이 뜨는 것이다.
    //변경사항을 db에 저장하는 메소드.
    public void saveChanges(){

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final String userName = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        //uri를 비트맵으로 만들고
        uriToBitmap(photoUri);

        //비트맵을 스트링으로 만든다.
        final String sendingBitmap = BitMapToString(photoBitmap);

        //그리고 edittext에서 글자를 따온다.
        final String gotIntroduceText = introduce.getText().toString();


        Log.e("이름", photoUri.toString());
//        Log.e("사진", sendingBitmap);
//        Log.e("소개", gotIntroduceText);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.PROFILE_CHANGES + "?apicall=upload",

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
//                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show();

                                //starting the profile activity
                            } else { //에러 시 서버에서 해당하는 메시지를 받아서 표시해준다.
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getApplicationContext(), "에러 발생", Toast.LENGTH_SHORT).show();
                    }
                }) {

            //보낼 때는 최대 프로필사진, 유저이름, 한줄소개 총 3개를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>(); //파라미터스
                params.put("userDesc", gotIntroduceText);
                params.put("sendedPhoto", sendingBitmap);
                params.put("userName", userName);
                Log.e("정보 전송", "완료");
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    //사진은 변경하지 않고 글자만 변경할 때 사용하는 메소드.
    public void saveChangesWithoutPhoto(){

        //sp에서 유저네임을 꺼내 온다.
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final String userName = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);

        final String gotIntroduceText = introduce.getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.PROFILE_CHANGES + "?apicall=uploadwithoutphoto",

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
//                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "프로필이 변경되었습니다.", Toast.LENGTH_SHORT).show();

                                //starting the profile activity
                            } else { //에러 시 서버에서 해당하는 메시지를 받아서 표시해준다.
                                Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getApplicationContext(), "에러 발생!", Toast.LENGTH_SHORT).show();
                    }
                }) {

            //이 경우에는 유저이름, 한줄소개 총 2개를 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>(); //파라미터스
                params.put("userDesc", gotIntroduceText);
                params.put("userName", userName);
                Log.e("정보 전송", "완료");
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

}
