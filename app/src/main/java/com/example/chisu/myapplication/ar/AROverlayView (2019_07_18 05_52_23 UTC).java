package com.example.chisu.myapplication.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import com.example.chisu.myapplication.ar.helper.LocationHelper;
import com.example.chisu.myapplication.ar.model.ARPoint;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ntdat on 1/13/17.
 */
//카메라에 겹치는 Arpoint를 나타내는 뷰.
public class AROverlayView extends View {

    Context context;
    //16개 방향
    private float[] rotatedProjectionMatrix = new float[16];

    //현재 위치
    private Location currentLocation;

    //ARpoint들의 리스트.
    private List<ARPoint> arPoints;

    String drawingTitle;
    String drawingImage;

    //주소값에서 만들어진 비트맵.
    Bitmap finalBitmap;
    Bitmap finalBitmapa2;

    //생성자
    public AROverlayView(Context context, String drawingTitle1, final String drawingLatitude, final String drawingLongtitude, final String drawingImage) {
        super(context);

        this.context = context;
        this.drawingTitle = drawingTitle1;
        this.drawingImage = drawingImage;
        //Demo points. 여기에 그림을 가져다주면 됨.
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint(drawingTitle, Double.parseDouble(drawingLatitude), Double.parseDouble(drawingLongtitude), 0));
//            add(new ARPoint(drawingTitle, 16.0404856, 108.2262447, 0));

            Log.e("drawingTitle", drawingTitle);
            Log.e("drawingImage", drawingImage);

        }};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                finalBitmap = getImageFromURL(drawingImage);
                finalBitmapa2 = imgResize(finalBitmap);

            }
        });
        thread.start();

    }

    public static Bitmap getImageFromURL(String imageURL){
        Bitmap imgBitmap = null;
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;

        try
        {
            URL url = new URL(imageURL);
            conn = (HttpURLConnection)url.openConnection();
            conn.connect();

            int nSize = conn.getContentLength();
            bis = new BufferedInputStream(conn.getInputStream(), nSize);
            imgBitmap = BitmapFactory.decodeStream(bis);
            Log.e("getImageFromURL", "완료");

        }
        catch (Exception e){
            e.printStackTrace();
            Log.e("getImageFromURL", e.getMessage());
        } finally{
            if(bis != null) {
                try {bis.close();} catch (IOException e) {}
            }
            if(conn != null ) {
                conn.disconnect();
            }
        }

        return imgBitmap;
    }

    public Bitmap imgResize(Bitmap bitmap)
    {
        int x=300,y=300; //바꿀 이미지 사이즈
        Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(bitmap, 0, 0, null);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Rect src = new Rect(0, 0, w, h);
        Rect dst = new Rect(0, 0, x, y);//이 크기로 변경됨
        canvas.drawBitmap(bitmap, src, dst, null);
        Log.e("imgResize", "완료");
        return output;
    }

    //카메라 상태변화에 따른 지속적인 투사체 업데이트 메소드.
    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    //현재 위치 업데이트 메소드.
    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    //그리기 메소드. 이거를 그림으로 대체해야 하는데 쉬울 것 같지는 않다.
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //현재위치가 null이면 종료
        if (currentLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        //arPoint의 갯수만큼 반복한다.
        for (int i = 0; i < arPoints.size(); i ++) {
            //현재 위치의 WSG84 좌표를 얻는다.
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            //ECEF : 지구의 중력 중심을 원점으로 하는 좌표계. GPS에서 사용하는 좌표체계로 변환.
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            //ENU : 또 다른 좌표체계로 변환.
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

//                canvas.drawCircle(x, y, radius, paint);

                canvas.drawBitmap(finalBitmapa2, x, y, null);
                canvas.drawText(arPoints.get(i).getName(), x - (10 * arPoints.get(i).getName().length() / 2), y - 20, paint);
            }
        }

        Log.e("onDraw", "진행 중");
    }

}
