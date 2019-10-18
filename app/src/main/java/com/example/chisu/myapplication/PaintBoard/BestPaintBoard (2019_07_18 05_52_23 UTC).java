package com.example.chisu.myapplication.PaintBoard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.OutputStream;
import java.util.Stack;

public class BestPaintBoard extends View {

    //사용자가 그림을 그린 시간 - 전역변수
    int drawTime = 0;

    /**
     * Undo data
     * 자료구조 스택. 선입후출. 뒤로가기를 할 때 사용.
     */
    Stack undos = new Stack();

    /**
     * Maximum Undos
     * 뒤로가기 한계 수 10;
     */
    public static int maxUndos = 15;

    /**
     * Changed flag
     * 체인지 플래그
     */
    public boolean changed = false;

    /** 캔버스 객체 정의
     * Canvas instance
     */
    Canvas mCanvas;

    /** 더블 버퍼링을 위한 비트맵 객체.
     * Bitmap for double buffering
     * 더블 버퍼링 : api로 화면을 만들다보면 화면이 깜박거리는데, 이 깜박거림을 없애주는 게 더블 버퍼링이다.
     * 원리 : 메모리에 먼저 그림을 그린 후 그 그림이 완성되면 화면에 출력해주는 것.
     */
    Bitmap mBitmap;

    /** 페인트 객체
     * Paint instance
     */
    final Paint mPaint;

    //터치 마지막 지점
    float lastX;
    float lastY;

    // path : 도형 궤적 정보를 가지는 그래픽 객체. 패스 정의만으로는 화면에 표시되지 않고 drawpath를 호출해 줘야 path정의를 따라 캔버스에 그린다.
    private final Path mPath = new Path();

    //커브
    private float mCurveEndX;
    private float mCurveEndY;

    private int mInvalidateExtraBorder = 10;

    //터치 허용 오차
    static final float TOUCH_TOLERANCE = 8;

    //anti alias : 선이나 도형을 그렸을 때 부드럽게 보여주는 것.
    private static final boolean RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    //페인트보드 배경색을 흰색으로 설정하기.
    private int mCertainColor = 0xFF000000;
    //기본적인 펜 선의 굵기
    private float mStrokeWidth = 2.0f;

    /** coordinates : 좌표
     * Initialize paint object and coordinates
     * @paramc
     */

    //생성자
    public BestPaintBoard(Context context) {
        super(context);

        // create a new paint object
        mPaint = new Paint();
        mPaint.setAntiAlias(RENDERING_ANTIALIAS);
        mPaint.setColor(mCertainColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setDither(DITHER_FLAG);

        //터치 이전에 기본적으로 -1로 설정해 아무것도 안 그려진 상태로 시작.
        lastX = -1;
        lastY = -1;

    }

    /**
     * Clear undo
     */
    public void clearUndo()
    {
        while(true) {
            //팝 : 스택의 자료를 꺼내기. 즉 방금 생성한 비트맵을 제거하기.
            Bitmap prev = (Bitmap)undos.pop();
            //그려진 비트맵이 더 이상 없으면 종료 - 아무것도 하지 않는다.
            if (prev == null) return;
            //메모리 오버를 방지하기 위해 리사이클을 통해 메모리 할당을 해제한다.
            prev.recycle();
        }
    }

    /**
     * Save undo
     * 뒤로 가기를 준비하기 위해서 이미지를 스택에 넣는 메소드.
     */
    public void saveUndo()
    {   //그려진 비트맵이 없다면 그대로 종료 - 아무것도 안 한다.
        if (mBitmap == null) return;

        //현재 스택의 사이즈가 최대 스택(10)보다 크거나 같은 동안 "반복"한다. 즉 10이 넘지 않게 스택의 쌓임을 조절하는 것이다.
        while (undos.size() >= maxUndos){
            //비트맵 객체 i를 생성하고 거기에 스택 사이즈 -1을 한다.
            Bitmap i = (Bitmap)undos.get(undos.size()-1);
            //그리고 그걸 제거한다.
            i.recycle();
            undos.remove(i);
        }

        //비트맵 객체와 캔버스 객체를 생성하고 캔버스에 비트맵을 그린다. RGB_8888은 메모리를 엄청나게 먹는다.
        Bitmap img = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        //이미지 자료를 스택에 넣는다(푸쉬한다).
        undos.push(img);

        Log.i("BestPaintBoard", "saveUndo() called. image data pushed in stack.");
    }

    /**
     * Undo
     */
    public void undo()
    {    //비트맵 객체 생성 후 초기화.
        Bitmap prev = null;
        // 스택에서 데이터를 하나 뺀 후 prev에 할당. 즉 그렸던 그림을 하나 뺀 후 prev에 할당.
        try {
            prev = (Bitmap)undos.pop();
        } catch(Exception ex) {
            Log.e("bestPaintBoard", "Exception : " + ex.getMessage());
        }
        //만약 prev가 null이 아니라면
        if (prev != null){

            drawBackground(mCanvas);
            mCanvas.drawBitmap(prev, 0, 0, mPaint);
            //invalidate : 화면 지속 갱신. 갱신을 안하면 사용자에게 보이는 화면은 그대로이기 때문입니다.
            invalidate();

            //메모리를 위해 비트맵 객체는 리사이클.
            prev.recycle();
        }

        Log.i("bestPaintBoard", "undo() called.");
    }

    /**
     * Paint background
     *
     * @paramg
     * @paramw
     * @paramh
     */
    public void drawBackground(Canvas canvas)
    {   //캔버스가 널이 아니라면 흰색으로 칠한다.
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
        }
    }

    /**
     * Update paint properties
     * 페인트 특성 업데이트해주는 메소드(색상 / 사이즈)
     * 팝업 두개가 닫힐 때마다 사용된다.
     * @paramcanvas
     */
    public void updatePaintProperty(int color, int size)
    {
        mPaint.setColor(color);
        mPaint.setStrokeWidth(size);
    }

    /**
     * Create a new image
     */
    public void newImage(int width, int height)
    {
        Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);

        mBitmap = img;
        mCanvas = canvas;

        drawBackground(mCanvas);

        changed = false;
        invalidate();
    }

    /**
     * Set image
     * 안 쓰는데 왜 있을까
     * @param newImage
     */
    public void setImage(Bitmap newImage)
    {
        changed = false;
        setImageSize(newImage.getWidth(),newImage.getHeight(),newImage);
        invalidate();
    }

    /**
     * Set image size
     *
     * @param width
     * @param height
     * @param newImage
     */
    public void setImageSize(int width, int height, Bitmap newImage)
    {
        if (mBitmap != null){
            if (width < mBitmap.getWidth()) width = mBitmap.getWidth();
            if (height < mBitmap.getHeight()) height = mBitmap.getHeight();
        }

        if (width < 1 || height < 1) return;

        Bitmap img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        drawBackground(canvas);

        if (newImage != null) {
            canvas.setBitmap(newImage);
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mCanvas.restore();
        }

        mBitmap = img;
        mCanvas = canvas;

        clearUndo();
    }



    /**
     * onSizeChanged
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0) {
            newImage(w, h);
        }
    }

    /**
     * Draw the bitmap
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

    }

    /**
     * Handles touch event, UP, DOWN and MOVE
     * 위의 메소드들은 모두 이 터치이벤트 메소드 내에서 일어난다.
     * 터치이벤트를 각각 터치업, 터치다운, 터치무브로 분류해 따로 처리한다.
     */
    public boolean onTouchEvent(MotionEvent event) {
        //터치할 때의 액션이 인트 변수 액션에 저장된다. 1, 2, 3, 이런 식일 것이다.
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_UP:
                //손가락을 뗄 때마다 changed가 true가 된다.
                changed = true;

                Rect rect = touchUp(event, false);
                if (rect != null) {
                    invalidate(rect);
                }

                //패스 객체 초기화
                mPath.rewind();

                return true;

            case MotionEvent.ACTION_DOWN:
                saveUndo();

                rect = touchDown(event);
                if (rect != null) {
                    invalidate(rect);
                }

                return true;

            case MotionEvent.ACTION_MOVE:
                rect = touchMove(event);
                if (rect != null) {
                    invalidate(rect);
                }

                return true;
        }

        return false;
    }

    /**
     * Process event for touch down
     *
     * @param event
     * @return
     */
    private Rect touchDown(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        lastX = x;
        lastY = y;

        Rect mInvalidRect = new Rect();
        //패스 객체에 현재 좌표값 추가(즉 터치다운 했을 때 그림이 그려진다는 것이다.)
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder;
        mInvalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);

        //아 이걸 뭐라고 설명해야 될 지 모르겠다.. mCurveEndX를 뭐라고 설명해야 할까?
        mCurveEndX = x;
        mCurveEndY = y;

        mCanvas.drawPath(mPath, mPaint);

        return mInvalidRect;
    }

    /**
     * Process event for touch move
     *
     * @param event
     * @return
     */
    private Rect touchMove(MotionEvent event) {
        Rect rect = processMove(event);

        return rect;
    }

    private Rect touchUp(MotionEvent event, boolean cancel) { //boolean cancel == boolean false
        Rect rect = processMove(event);

        return rect;
    }

    /**
     * Process Move Coordinates
     *
     * @paramx
     * @paramy
     * @paramdx
     * @paramdy
     * @return
     */
    private Rect processMove(MotionEvent event) { //손가락을 누른 채 이동할 때 발동되는 메소드.

        drawTime++;
        Log.e("drawTime", String.valueOf(drawTime));

        //새 터치 지점
        final float x = event.getX();
        final float y = event.getY();

        //이동 수치(새 터치 지점 - 이동 터치 지점)의 절대값.
        final float dx = Math.abs(x - lastX);
        final float dy = Math.abs(y - lastY);

        Rect mInvalidRect = new Rect();
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) { //둘 중 하나라도 터치 허용 오차보다 크다면
            final int border = mInvalidateExtraBorder;
            //Rect 객체 설정(높이, 너비)
            //다시 그려질 영역으로 현재 이동한 좌표 추가
            mInvalidRect.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                    (int) mCurveEndX + border, (int) mCurveEndY + border);

            float cX = mCurveEndX = (x + lastX) / 2;
            float cY = mCurveEndY = (y + lastY) / 2;

            //quadTo : 기준점에서 x1,y1까지, 그리고 x2,y2까지 곡선 형태를 그린다. (나이키 모양 생각)
            //패스 객체에 현재 좌표값을 곡선으로 추가. 즉 손가락을 누른 채로 이동했을 때 그림이 그려진다는 뜻이다.
            mPath.quadTo(lastX, lastY, cX, cY);

            // union with the control point of the new curve
            mInvalidRect.union((int) lastX - border, (int) lastY - border,
                    (int) lastX + border, (int) lastY + border);

            // union with the end point of the new curve
            mInvalidRect.union((int) cX - border, (int) cY - border,
                    (int) cX + border, (int) cY + border);

            lastX = x;
            lastY = y;

            mCanvas.drawPath(mPath, mPaint);
        }

        return mInvalidRect;
    }


    public boolean Save(OutputStream outstream) {
        try {
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            invalidate();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
