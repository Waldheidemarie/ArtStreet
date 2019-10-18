package com.example.chisu.myapplication.ar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.KITKAT)
//ARActivity의 서피스뷰에 나타나는 카메라 클래스. 서피스뷰를 상속하지 않고 서피스뷰 객체를 만들어서 했다.
public class ARCamera extends ViewGroup implements SurfaceHolder.Callback {

    private final String TAG = "ARCamera";

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    //카메라 프리뷰 사이즈.
    Camera.Size previewSize;
    //사이즈들의 리스트. 이 중에 int값을 줘서 선택할 수 있다.
    List<Camera.Size> supportedPreviewSizes;
    //기본 카메라 클래스.
    Camera camera;
    //카메라의 파라미터.
    Camera.Parameters parameters;
    //context를 얻기 위한 액티비티 객체 생성.
    Activity activity;

    //프리뷰 중인지 알기 위한 플래그 변수. 프리뷰가 시작하면 true로 변한다. 이 변수 체크는 사진 찍을 때 체크.
    boolean previewing = false;

    //16가지 방향
    float[] projectionMatrix = new float[16];

    //카메라의 가로세로
    int cameraWidth;
    int cameraHeight;

    private final static float Z_NEAR = 0.5f;
    private final static float Z_FAR = 2000;

    //ARCamera 생성자.
    public ARCamera(Context context, SurfaceView surfaceView) {
        super(context);

        this.surfaceView = surfaceView;
        this.activity = (Activity) context;
        surfaceHolder = this.surfaceView.getHolder();
        //클래스에 implement했기 때문에 this 만 해줘도 충분하다.
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //서피스뷰에서 캡쳐를 하기 위한 핵심 콜백 메소드.
    //onPreviewFrame메소드의 bytes가 이미지 정보이다. 이 이미지를 YuvImage로 바꾸고,
    //이걸 다시금 바이트로 바꾸는 작업이다. 그리고 그 이미지를 ARAcivity에 제공해준다.
    //참고로 이 이미지에는 아직 오버레이는 포함되어있지 않다. 생 사진임.
    Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            Camera.Parameters params = camera.getParameters();
            int w = params.getPreviewSize().width;
            int h = params.getPreviewSize().height;
            int format = params.getPreviewFormat();
            YuvImage image = new YuvImage(bytes, format, w, h, null);

            Log.e("capture", "중간1");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rect area = new Rect(0, 0, w, h);
            image.compressToJpeg(area, 30, out);

            Bitmap bm = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

            //opengl 매트릭스가 아닌 그래픽 매트릭스를 이용해야 한다.
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            //찍을 때 세로로 찍었기 때문에 90도로 돌려준다. 가로로 사진 찍을 때는 추가로 생각해 볼 것. if로 어떻게 하면 되겠지.
            matrix.postRotate(90);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
            ARActivity.shareBitmap = rotatedBitmap;

        }
    };

    public void setCamera(Camera camera) {
        this.camera = camera;
        if (this.camera != null) {
            supportedPreviewSizes = this.camera.getParameters().getSupportedPreviewSizes();
            requestLayout();
            Camera.Parameters params = this.camera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                this.camera.setParameters(params);
            }
        }
        Log.e("setCamera", " ");
    }

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            Log.e(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] arg0, Camera arg1) {
            Log.e("onPictureTaken_JPG", "시작");
            //사진을 찍고 난 후 다시 카메라가 작동하도록 스타트프리뷰를 해준다.
            //사진을 찍기 직전에 카메라가 스톱되기 때문이다.
            camera.startPreview();
        }
    };

    //셔터 콜백. 이게 찰칵 소리게 나게 해준다.
    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.e(TAG, "onShutter'd");
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (supportedPreviewSizes != null) {
            previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
        }
        Log.e("onMeasure", " ");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = right - left;
            final int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }

        Log.e("onLayout", " ");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                //위에 작성한 프리뷰콜백을 create에서 적용한다.
                camera.setPreviewCallback(callback);
                parameters = camera.getParameters();

                int orientation = getCameraOrientation();

                camera.setDisplayOrientation(orientation);
                camera.getParameters().setRotation(orientation);
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Log.e("surfaceCreated", " ");

    }

    //카메라 방향 선정 메소드
    private int getCameraOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int orientation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            orientation = (info.orientation + degrees) % 360;
            orientation = (360 - orientation) % 360;
        } else {
            orientation = (info.orientation - degrees + 360) % 360;
        }
        Log.e("getCameraOrientation", " ");

        return orientation;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        Log.e("surfaceDestroyed", " ");
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) width / height;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        if (optimalSize == null) {
            optimalSize = sizes.get(0);
        }
        Log.e("getOptiomalPriviewSize", " ");

        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //카메라 각도를 90도로(세로).
        camera.setDisplayOrientation(90);
        if (camera != null) {
            this.cameraWidth = width;
            this.cameraHeight = height;

            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> priviewSizes = params.getSupportedPreviewSizes();

            Camera.Size priviewsize = priviewSizes.get(1);

            params.setPreviewSize(priviewsize.width, priviewsize.height);
            requestLayout();
            Log.e("width", String.valueOf(priviewsize.width));
            Log.e("height", String.valueOf(priviewsize.height));

            camera.setParameters(params);
            //프리뷰를 시작하면 플래그를 바꾸어준다.
            previewing = true;
            generateProjectionMatrix();
        }
        camera.startPreview();

        Log.e("surfaceChanged", " ");

    }

    private void generateProjectionMatrix() {
        float ratio = (float) this.cameraWidth / this.cameraHeight;
        final int OFFSET = 0;
        final float LEFT = -ratio;
        final float RIGHT = ratio;
        final float BOTTOM = -1;
        final float TOP = 1;
        Matrix.frustumM(projectionMatrix, OFFSET, LEFT, RIGHT, BOTTOM, TOP, Z_NEAR, Z_FAR);
        Log.e("generateProjectionMatri", " ");

    }

    public float[] getProjectionMatrix() {

        return projectionMatrix;

    }


}
