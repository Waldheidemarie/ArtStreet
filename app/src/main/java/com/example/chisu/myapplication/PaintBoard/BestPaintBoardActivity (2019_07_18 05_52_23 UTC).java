package com.example.chisu.myapplication.PaintBoard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

public class BestPaintBoardActivity extends AppCompatActivity implements View.OnClickListener {

    //팝업을 종료할 때 이 페인트보드도 한 번에 종료하기 위한 액티비티.
    public static Activity BBctivity;

    static Bitmap bitmap;
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


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 화면을 landscape(가로) 화면으로 고정하고 싶은 경우
        setContentView(R.layout.activity_canvas);

        Log.e("hori onCreate", "");

        BBctivity = BestPaintBoardActivity.this;

        LinearLayout toolsLayout = findViewById(R.id.toolsLayout);
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
//        LinearLayout.LayoutParams addedParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//
//        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                48);
//        addedLayout = new LinearLayout(this);
//        addedLayout.setLayoutParams(addedParams);
//        addedLayout.setOrientation(LinearLayout.VERTICAL);
//        addedLayout.setPadding(8,8,8,8);
//
//        LinearLayout outlineLayout = new LinearLayout(this);
//        outlineLayout.setLayoutParams(buttonParams);
//        outlineLayout.setOrientation(LinearLayout.VERTICAL);
//        outlineLayout.setBackgroundColor(Color.LTGRAY);
//        outlineLayout.setPadding(1,1,1,1);
//
////        colorLegendBtn = findViewById(R.id.selectedColor);
//        colorLegendBtn = new Button(this);
//        colorLegendBtn.setLayoutParams(buttonParams);
//        colorLegendBtn.setText(" ");
//        colorLegendBtn.setBackgroundColor(mColor);
//        colorLegendBtn.setHeight(11);
//        //아웃라인레이아웃에 버튼을 올리고
//        //애디드레이아웃에 아웃라인레이아웃을 올린다. 구분을 위한 것이므로 아웃라인은 하나만.
//        outlineLayout.addView(colorLegendBtn);
//        addedLayout.addView(outlineLayout);
//
//        sizeLegendTxt = new TextView(this);
//        sizeLegendTxt.setLayoutParams(buttonParams);
//        sizeLegendTxt.setText("Size : " + mSize);
//        sizeLegendTxt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        sizeLegendTxt.setTextSize(11);
//        sizeLegendTxt.setTextColor(Color.BLACK);
//        addedLayout.addView(sizeLegendTxt);
//
//        //addedLayout은 toolsLayout에 붙인다.
//        toolsLayout.addView(addedLayout);


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
                Intent intent = new Intent(BestPaintBoardActivity.this, ColorPaletteDialog.class);
//                if(color != 0){
                    //이전 색깔 넣어주기
                    intent.putExtra("oldColor",color);
                    //xml 형태를 맞게 하기 위한 방향 넣어주기
                    intent.putExtra("direction", "horizontal");
                intent.putExtra("drawScore", "1111");

//                }
                startActivityForResult(intent, COLOR_ACTIVITY);


            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

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

//    private void displayPaintProperty() {
//        colorLegendBtn.setBackgroundColor(mColor);
//        sizeLegendTxt.setText("Size : " + mSize);
//        addedLayout.invalidate();
//    }

    @Override
    public void onClick(View v) {

        //이미지를 담기 위해 드로잉캐시 생성
        boardLayout.buildDrawingCache();
        //그려진 그림이 담긴 레이아웃을 스태틱 비트맵에 넣어서 로컬에 저장되지 않고도 바로 db로 보낼 수 있게 되었다.
        bitmap = boardLayout.getDrawingCache();

        Intent intent = new Intent(getApplicationContext(), DrawingFinishDialog1.class);
        intent.putExtra("direction", "horizontal");
        startActivity(intent);

        // 올라가는 이미지의 이름은 그냥 서버에서 만들면 된다.
        // 근데 리팩토링 전에는 그것을 위해 클라에서 임시파일을 만들고, 그 파일의 이름을 만들고,
        // 그 이름을 인텐트를 통해 여러번을 거쳐서 서버에 저장시켰다. 매우 쓸모없는 프로세스다.
        // 잘 몰라서 그런 것도 있지만, 이런 건 매우 문제가 될 수 있으므로 간단하게 만들었다.
        // 비록 그 양이 너무 많아서 시행착오의 대부분을 지우긴 하지만
        // 다음에 이런 짓을 하지 않기 위해 이 곳에 글을 적는다.

        //버전에 따라 코드가 달라질 수 있다는 것을 알려주는 코드. 박제.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
//            photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
//                    BuildConfig.APPLICATION_ID + "com.example.jisu7.provider", tempFile);
//        } else {//sdk 23 이하, 7.0 미만
//            photoUri = Uri.fromFile(tempFile);
//        }

    }

}

//public class BestPaintBoardActivity extends AppCompatActivity implements View.OnClickListener {
//
//    //팝업을 종료할 때 이 페인트보드도 한 번에 종료하기 위한 액티비티.
//    public static Activity BBctivity;
//
//    static Bitmap bitmap;
//    BestPaintBoard board;
//    Button colorBtn;
//    Button penBtn;
//    Button eraserBtn;
//    Button undoBtn;
//    Button finishBtn;
//    LinearLayout addedLayout;
//    LinearLayout boardLayout;
//    Button colorLegendBtn;
//    TextView sizeLegendTxt;
//
//    int mColor = 0xff000000; //펜의 색깔
//    int mSize = 2; //펜의 굵기
//    int oldColor;
//    int oldSize;
//    boolean eraserSelected = false;
//
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        // 화면을 landscape(가로) 화면으로 고정하고 싶은 경우
//        setContentView(R.layout.activity_canvas);
//
//        BBctivity = BestPaintBoardActivity.this;
//
//        LinearLayout toolsLayout = findViewById(R.id.toolsLayout);
//        boardLayout = findViewById(R.id.boardLayout);
//        colorBtn = findViewById(R.id.colorBtn);
//        penBtn = findViewById(R.id.penBtn);
//        eraserBtn = findViewById(R.id.eraserBtn);
//        undoBtn = findViewById(R.id.undoBtn);
//        finishBtn = findViewById(R.id.finishBtn);
//        finishBtn.setOnClickListener(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//
//        board = new BestPaintBoard(this);
//        board.setLayoutParams(params);
//        board.setPadding(2, 2, 2, 2);
//
//        boardLayout.addView(board);
//
//        if (shouldAskPermissions()) {
//            askPermissions();
//        }
//
//        /*
//         *  addedLayout 시작 지점
//         * */
//        // add legend(범례) buttons
//        // 동적으로 legend버튼을 추가했는데 나타나지 않는 이유는 뭐지? 안쪽에서만
//        // 상태를 xml이 아니라 자바로 표현한 이유 : 색깔과 굵기가 변할 때마다 지속적으로 동적으로 변화해야 하기 때문이다. xml은 정적이다.
//        LinearLayout.LayoutParams addedParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//
//        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                48);
//        addedLayout = new LinearLayout(this);
//        addedLayout.setLayoutParams(addedParams);
//        addedLayout.setOrientation(LinearLayout.VERTICAL);
//        addedLayout.setPadding(8,8,8,8);
//
//        LinearLayout outlineLayout = new LinearLayout(this);
//        outlineLayout.setLayoutParams(buttonParams);
//        outlineLayout.setOrientation(LinearLayout.VERTICAL);
//        outlineLayout.setBackgroundColor(Color.LTGRAY);
//        outlineLayout.setPadding(1,1,1,1);
//
////        colorLegendBtn = findViewById(R.id.selectedColor);
//        colorLegendBtn = new Button(this);
//        colorLegendBtn.setLayoutParams(buttonParams);
//        colorLegendBtn.setText(" ");
//        colorLegendBtn.setBackgroundColor(mColor);
//        colorLegendBtn.setHeight(11);
//        //아웃라인레이아웃에 버튼을 올리고
//        //애디드레이아웃에 아웃라인레이아웃을 올린다. 구분을 위한 것이므로 아웃라인은 하나만.
//        outlineLayout.addView(colorLegendBtn);
//        addedLayout.addView(outlineLayout);
//
//        sizeLegendTxt = new TextView(this);
//        sizeLegendTxt.setLayoutParams(buttonParams);
//        sizeLegendTxt.setText("Size : " + mSize);
//        sizeLegendTxt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        sizeLegendTxt.setTextSize(11);
//        sizeLegendTxt.setTextColor(Color.BLACK);
//        addedLayout.addView(sizeLegendTxt);
//
//        //addedLayout은 toolsLayout에 붙인다.
//        toolsLayout.addView(addedLayout);
//
//
//        colorBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
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
//
//            }
//        });
//
//        penBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                //펜팔렛 다이얼로그의 아무 의미없어 보였던 리스너가 여기서 사용된다. 그래서
//                //리스너를 퍼블릭 스태틱으로 선언해 두었던 것이었던 것이었던 것이었다.
//                //리스너 객체를 생성한다. 다이얼로그와 페인트보드는 리스너를 통해서 연결되는 거다.
//
//                PenPaletteDialog.seekedListener = new OnPenSeekedListener() {
//                    @Override
//                    public void onPenSelected(int size) {
//                        mSize = size;
//                        board.updatePaintProperty(mColor, mSize);
//                        displayPaintProperty();
//                    }
//                };
//
//                Intent intent = new Intent(getApplicationContext(), PenPaletteDialog.class);
//                startActivity(intent);
//
//            }
//        });
//
//        eraserBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                eraserSelected = !eraserSelected;
//
//                if (eraserSelected) {
//                    //지우개 버튼을 선택하면 굵기를 제외한 다른 모든 버튼을 비활성화 시킨다.
//                    colorBtn.setEnabled(false);
////                    penBtn.setEnabled(false);
//                    undoBtn.setEnabled(false);
//
//                    colorBtn.invalidate();
//                    penBtn.invalidate();
//                    undoBtn.invalidate();
//
//                    oldColor = mColor;
//                    oldSize = mSize;
//
//                    mColor = Color.WHITE;
//                    mSize = 15;
//
//                    board.updatePaintProperty(mColor, mSize);
//                    displayPaintProperty();
//
//                } else {
//                    colorBtn.setEnabled(true);
//                    penBtn.setEnabled(true);
//                    undoBtn.setEnabled(true);
//
//                    colorBtn.invalidate();
//                    penBtn.invalidate();
//                    undoBtn.invalidate();
//
//                    mColor = oldColor;
//                    mSize = oldSize;
//
//                    board.updatePaintProperty(mColor, mSize);
//                    displayPaintProperty();
//
//                }
//
//            }
//        });
//
//        undoBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                board.undo();
//            }
//        });
//
//    }
//
//    protected boolean shouldAskPermissions() {
//        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
//    }
//
//    @TargetApi(23)
//    protected void askPermissions() {
//        String[] permissions = {
//                "android.permission.READ_EXTERNAL_STORAGE",
//                "android.permission.WRITE_EXTERNAL_STORAGE"
//        };
//        int requestCode = 200;
//        requestPermissions(permissions, requestCode);
//    }
//
//    public int getChosenColor() {
//        return mColor;
//    }
//
//    public int getPenThickness() {
//        return mSize;
//    }
//
//    private void displayPaintProperty() {
//        colorLegendBtn.setBackgroundColor(mColor);
//        sizeLegendTxt.setText("Size : " + mSize);
//        addedLayout.invalidate();
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        //이미지를 담기 위해 드로잉캐시 생성
//        boardLayout.buildDrawingCache();
//        //그려진 그림이 담긴 레이아웃을 스태틱 비트맵에 넣어서 로컬에 저장되지 않고도 바로 db로 보낼 수 있게 되었다.
//        bitmap = boardLayout.getDrawingCache();
//
//        Intent intent = new Intent(getApplicationContext(), DrawingFinishDialog1.class);
//        startActivity(intent);
//
//        // 올라가는 이미지의 이름은 그냥 서버에서 만들면 된다.
//        // 근데 리팩토링 전에는 그것을 위해 클라에서 임시파일을 만들고, 그 파일의 이름을 만들고,
//        // 그 이름을 인텐트를 통해 여러번을 거쳐서 서버에 저장시켰다. 매우 쓸모없는 프로세스다.
//        // 잘 몰라서 그런 것도 있지만, 이런 건 매우 문제가 될 수 있으므로 간단하게 만들었다.
//        // 비록 그 양이 너무 많아서 시행착오의 대부분을 지우긴 하지만
//        // 다음에 이런 짓을 하지 않기 위해 이 곳에 글을 적는다.
//
//        //버전에 따라 코드가 달라질 수 있다는 것을 알려주는 코드. 박제.
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
////            photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
////                    BuildConfig.APPLICATION_ID + "com.example.jisu7.provider", tempFile);
////        } else {//sdk 23 이하, 7.0 미만
////            photoUri = Uri.fromFile(tempFile);
////        }
//
//    }
//
//}