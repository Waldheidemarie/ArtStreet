package com.example.chisu.myapplication.PaintBoard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chisu.myapplication.R;
import com.larswerkman.holocolorpicker.ColorPicker;

public class BestPaintBoardActivityVertical extends AppCompatActivity implements View.OnClickListener {

    //팝업을 종료할 때 이 페인트보드도 한 번에 종료하기 위한 액티비티.
    public static Activity BBctivity;

    public static Bitmap bitmap;
    public static Bitmap bitmap1;

    BestPaintBoard board;
    Button colorBtn;
    Button penBtn;
    Button eraserBtn;
    Button undoBtn;
    Button finishBtn;
    LinearLayout addedLayout;
    LinearLayout boardLayout;
    Button colorLegendBtn;
    TextView sizeLegendTxt;

    int mColor = 0xff000000; //펜의 색깔
    int mSize = 2; //펜의 굵기
    int oldColor;
    int oldSize;
    boolean eraserSelected = false;

    private final static int COLOR_ACTIVITY = 1;

    int color;
    ColorPicker picker;

    LinearLayout.LayoutParams buttonParams;
    LinearLayout toolsLayout;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 화면을 landscape(가로) 화면으로 고정하고 싶은 경우
        setContentView(R.layout.activity_canvas_vertical);

        Log.e("vert onCreate", "");


        BBctivity = BestPaintBoardActivityVertical.this;

        toolsLayout = findViewById(R.id.toolsLayout);
        boardLayout = findViewById(R.id.boardLayout);
        colorBtn = findViewById(R.id.colorBtn);
        penBtn = findViewById(R.id.penBtn);
        eraserBtn = findViewById(R.id.eraserBtn);
        undoBtn = findViewById(R.id.undoBtn);
        finishBtn = findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        board = new BestPaintBoard(this);
        board.setLayoutParams(params);
        board.setPadding(2, 2, 2, 2);

        boardLayout.addView(board);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        /*
         *  addedLayout 시작 지점
         * */
        // add legend(범례) buttons
        // 동적으로 legend버튼을 추가했는데 나타나지 않는 이유는 뭐지? 안쪽에서만
        // 상태를 xml이 아니라 자바로 표현한 이유 : 색깔과 굵기가 변할 때마다 지속적으로 동적으로 변화해야 하기 때문이다. xml은 정적이다.

        penBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //펜팔렛 다이얼로그의 아무 의미없어 보였던 리스너가 여기서 사용된다. 그래서
                //리스너를 퍼블릭 스태틱으로 선언해 두었던 것이었던 것이었던 것이었다.
                //리스너 객체를 생성한다. 다이얼로그와 페인트보드는 리스너를 통해서 연결되는 거다.

                PenPaletteDialog.seekedListener = new OnPenSeekedListener() {
                    @Override
                    public void onPenSelected(int size) {
                        mSize = size;
                        board.updatePaintProperty(mColor, mSize);
//                        displayPaintProperty();
                    }
                };

                Intent intent = new Intent(getApplicationContext(), PenPaletteDialog.class);
                startActivity(intent);

            }
        });

        eraserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                eraserSelected = !eraserSelected;

                if (eraserSelected) {
                    //지우개 버튼을 선택하면 굵기를 제외한 다른 모든 버튼을 비활성화 시킨다.
                    colorBtn.setEnabled(false);
//                    penBtn.setEnabled(false);
                    undoBtn.setEnabled(false);

                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    oldColor = mColor;
                    oldSize = mSize;

                    mColor = Color.WHITE;
                    mSize = 15;

                    board.updatePaintProperty(mColor, mSize);
//                    displayPaintProperty();

                } else {
                    colorBtn.setEnabled(true);
                    penBtn.setEnabled(true);
                    undoBtn.setEnabled(true);

                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    mColor = oldColor;
                    mSize = oldSize;

                    board.updatePaintProperty(mColor, mSize);
//                    displayPaintProperty();

                }

            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                board.undo();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        colorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

//                ColorPaletteDialog.listener = new OnColorSelectedListener() {
//                    public void onColorSelected(int color) {
//                        mColor = color;
//                        board.updatePaintProperty(mColor, mSize);
//                        displayPaintProperty();
//                    }
//                };
//
//                Intent intent = new Intent(getApplicationContext(), ColorPaletteDialog.class);
//                startActivity(intent);
                Intent intent = new Intent(BestPaintBoardActivityVertical.this, ColorPaletteDialog.class);
//                if (color != 0) {
                    intent.putExtra("oldColor", color);
                    intent.putExtra("direction", "vertical");
//                }
                startActivityForResult(intent, COLOR_ACTIVITY);

            }
        });

    }

    //컬러피커에서 색깔을 받아오는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == COLOR_ACTIVITY) {
                color = data.getIntExtra("color", 0);
                mColor = color;
                board.updatePaintProperty(mColor, mSize);
//                displayPaintProperty();

            }
        }
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    public int getChosenColor() {
        return mColor;
    }

    public int getPenThickness() {
        return mSize;
    }

    private void displayPaintProperty() {
        colorLegendBtn.setBackgroundColor(mColor);
        sizeLegendTxt.setText("Size : " + mSize);
        addedLayout.invalidate();
    }

    @Override
    public void onClick(View v) {

        View drawingView = boardLayout;

        //이미지를 담기 위해 드로잉캐시 생성
        drawingView.buildDrawingCache(true);

        //그려진 그림이 담긴 레이아웃을 스태틱 비트맵에 넣어서 로컬에 저장되지 않고도 바로 db로 보낼 수 있게 되었다.
        //RGB_565로 하면 바탕화면이 왜그런진 몰라도 초록색이 된다.
        bitmap = drawingView.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);

        //mainactivity에 보내기 위한 용도의 bitmap1 생성. 리사이클드 비트맵 문제를 해결하기 위해 이렇게 생성했다.
        bitmap1 = drawingView.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);

        Intent intent = new Intent(getApplicationContext(), DrawingFinishDialog1.class);
        //DrawingfinishDialog2에서 bBActivity 종료를 할 때 구분하기 위해 인텐트를 보내줌.
        intent.putExtra("direction", "vertical");
        intent.putExtra("drawScore", String.valueOf(board.drawTime));
        startActivity(intent);

    }

}
