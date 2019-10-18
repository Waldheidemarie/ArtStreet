package com.example.chisu.myapplication.PaintBoard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;


import com.example.chisu.myapplication.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;


public class ColorPaletteDialog extends Activity {

    GridView grid;
    Button closeBtn;
//    ColorDataAdapter adapter;

    public static OnColorSelectedListener listener;

    Intent intent ;
    ColorPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

         intent = getIntent();
        int oldColor = intent.getIntExtra("oldColor", 0);
        String direction = intent.getStringExtra("direction");

        Log.e("direction", direction);

        if (direction.equals("horizontal")){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.pen_color_dialog2);

        } else {
            setContentView(R.layout.pen_color_dialog);
        }
        //가로 세로가 각각 달라야 한다.


        picker = (ColorPicker) findViewById(R.id.picker);
        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);

        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        picker.getColor();

        if(oldColor != 0){
            picker.setOldCenterColor(oldColor);
        }else{
            picker.setOldCenterColor(picker.getColor());
        }


//
//        this.setTitle("색상 선택");
//
//        grid = (GridView) findViewById(R.id.colorGrid);
//        closeBtn = (Button) findViewById(R.id.closeBtn);
//
//        //그리드뷰의 속성 설정
//        grid.setColumnWidth(14);
//        grid.setBackgroundColor(Color.GRAY);
//        grid.setVerticalSpacing(4);
//        grid.setHorizontalSpacing(4);
//
//        //그리드뷰에 어댑터 설정.
//        adapter = new ColorDataAdapter(this);
//        grid.setAdapter(adapter);
//        grid.setNumColumns(adapter.getNumColumns());
//
//        closeBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // 닫기 버튼을 누르면 닫힘.
//                // dispose this activity
//                finish();
//            }
//        });

    }

    public void getColor(View view){

        intent.putExtra("color", picker.getColor());
        setResult(RESULT_OK, intent);
        finish();
    }


}

/**
 * 새로운 어댑터 정의
 *
 */
//class ColorDataAdapter extends BaseAdapter {
//
//    /**
//     * Application Context
//     * 액티비티가 아닌 곳에서 context를 얻으려면 따로 정의하면 된다.
//     */
//    Context mContext;
//
//    /**
//     * Colors defined. 색상값을 배열로 정의
//     * 어댑터 클래스에서 색상의 배열을 정의.
//     */
//
//    public static final int [] colors = new int[] {
//            0xff000000,0xff00007f,0xff0000ff,0xff007f00,0xff007f7f,0xff00ff00,0xff00ff7f,
//            0xff00ffff,0xff7f007f,0xff7f00ff,0xff7f7f00,0xff7f7f7f,0xffff0000,0xffff007f,
//            0xffff00ff,0xffff7f00,0xffff7f7f,0xffff7fff,0xffffff00,0xffffff7f,0xffffffff
//    };
//
//    // 색상 배열의 행 개수와 열 개수
//    int rowCount;
//    int columnCount;
//
//
//
//    //어댑터의 생성자
//    public ColorDataAdapter(Context context) {
//        super();
//
//        mContext = context;
//
//        // create test data
//        rowCount = 3;
//        columnCount = 7;
//
//    }
//
//    //그리드의 컬럼 개수 리턴
//    public int getNumColumns() {
//        return columnCount;
//    }
//
//    //그리드의 총 갯수
//    public int getCount() {
//        return rowCount * columnCount;
//    }
//
//    //포지션 당 컬러 리턴
//    public Object getItem(int position) {
//        return colors[position];
//    }
//
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    public View getView(int position, View view, ViewGroup group) {
//        Log.d("ColorDataAdapter", "getView(" + position + ") called.");
//
//        // 밑의 식으로 색의 위치를 정함. 3행 2열 이런 식.
//        // calculate position
//        int rowIndex = position / rowCount;
//        int columnIndex = position % rowCount;
//        Log.d("ColorDataAdapter", "Index : " + rowIndex + ", " + columnIndex);
//
//        GridView.LayoutParams params = new GridView.LayoutParams(
//                GridView.LayoutParams.MATCH_PARENT,
//                GridView.LayoutParams.MATCH_PARENT);
//
//        // 컬러 버튼 생성
//        // create a Button with the color
//        Button aItem = new Button(mContext);
//        //공간을 위한 빈칸...?
//        aItem.setText(" ");
//        //코드 상에서 레이아웃 파라미터 설정하기(레이아웃 파라미터란 리스트뷰, 그리드뷰, 라디오버튼, 레이아웃 이런 것들을 말한다.)
//        aItem.setLayoutParams(params);
//        aItem.setPadding(4, 4, 4, 4);
//        aItem.setBackgroundColor(colors[position]);
//        aItem.setHeight(120);
//        //태그는 그냥 이름표라고 생각하면 된다. 이걸로 찾을 수도 있다. ex)findviewwithTag 처럼.
//        aItem.setTag(colors[position]);
//
//        // set listener 버튼에 리스너 설정.
//        // 버튼이 클릭되면 버튼에 지정된 색상값(태그)을 리스너에 전달한다.
//        // 리스너에 전달하고 나서 액티비티를 종료한다.
//        aItem.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) { //여기서 v는 버튼.
//                if (ColorPaletteDialog.listener != null) {
//                    ColorPaletteDialog.listener.onColorSelected(((Integer)v.getTag()).intValue());
//                }
//
//                ((ColorPaletteDialog)mContext).finish();
//            }
//        });
//        //그리고 버튼을 리턴한다.
//        return aItem;
//    }
//}

//public class ColorPaletteDialog extends Activity{
//
//
//    GridView grid;
//    Button closeBtn;
//    ColorDataAdapter adapter;
//
//    public static OnColorSelectedListener listener;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.pen_color_dialog);
//
//        this.setTitle("색상 선택");
//
//        grid = (GridView) findViewById(R.id.colorGrid);
//        closeBtn = (Button) findViewById(R.id.closeBtn);
//
//        //그리드뷰의 속성 설정
//        grid.setColumnWidth(14);
//        grid.setBackgroundColor(Color.GRAY);
//        grid.setVerticalSpacing(4);
//        grid.setHorizontalSpacing(4);
//
//        //그리드뷰에 어댑터 설정.
//        adapter = new ColorDataAdapter(this);
//        grid.setAdapter(adapter);
//        grid.setNumColumns(adapter.getNumColumns());
//
//        closeBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // 닫기 버튼을 누르면 닫힘.
//                // dispose this activity
//                finish();
//            }
//        });
//
//    }
//
//}
////
/////**
//// * 새로운 어댑터 정의
//// *
//// */
////class ColorDataAdapter extends BaseAdapter {
////
////    /**
////     * Application Context
////     * 액티비티가 아닌 곳에서 context를 얻으려면 따로 정의하면 된다.
////     */
////    Context mContext;
////
////    /**
////     * Colors defined. 색상값을 배열로 정의
////     * 어댑터 클래스에서 색상의 배열을 정의.
////     */
////
////    public static final int [] colors = new int[] {
////            0xff000000,0xff00007f,0xff0000ff,0xff007f00,0xff007f7f,0xff00ff00,0xff00ff7f,
////            0xff00ffff,0xff7f007f,0xff7f00ff,0xff7f7f00,0xff7f7f7f,0xffff0000,0xffff007f,
////            0xffff00ff,0xffff7f00,0xffff7f7f,0xffff7fff,0xffffff00,0xffffff7f,0xffffffff
////    };
////
////    // 색상 배열의 행 개수와 열 개수
////    int rowCount;
////    int columnCount;
////
////
////
////    //어댑터의 생성자
////    public ColorDataAdapter(Context context) {
////        super();
////
////        mContext = context;
////
////        // create test data
////        rowCount = 3;
////        columnCount = 7;
////
////    }
////
////    //그리드의 컬럼 개수 리턴
////    public int getNumColumns() {
////        return columnCount;
////    }
////
////    //그리드의 총 갯수
////    public int getCount() {
////        return rowCount * columnCount;
////    }
////
////    //포지션 당 컬러 리턴
////    public Object getItem(int position) {
////        return colors[position];
////    }
////
////    public long getItemId(int position) {
////        return 0;
////    }
////
////    public View getView(int position, View view, ViewGroup group) {
////        Log.d("ColorDataAdapter", "getView(" + position + ") called.");
////
////        // 밑의 식으로 색의 위치를 정함. 3행 2열 이런 식.
////        // calculate position
////        int rowIndex = position / rowCount;
////        int columnIndex = position % rowCount;
////        Log.d("ColorDataAdapter", "Index : " + rowIndex + ", " + columnIndex);
////
////        GridView.LayoutParams params = new GridView.LayoutParams(
////                GridView.LayoutParams.MATCH_PARENT,
////                GridView.LayoutParams.MATCH_PARENT);
////
////        // 컬러 버튼 생성
////        // create a Button with the color
////        Button aItem = new Button(mContext);
////        //공간을 위한 빈칸...?
////        aItem.setText(" ");
////        //코드 상에서 레이아웃 파라미터 설정하기(레이아웃 파라미터란 리스트뷰, 그리드뷰, 라디오버튼, 레이아웃 이런 것들을 말한다.)
////        aItem.setLayoutParams(params);
////        aItem.setPadding(4, 4, 4, 4);
////        aItem.setBackgroundColor(colors[position]);
////        aItem.setHeight(120);
////        //태그는 그냥 이름표라고 생각하면 된다. 이걸로 찾을 수도 있다. ex)findviewwithTag 처럼.
////        aItem.setTag(colors[position]);
////
////        // set listener 버튼에 리스너 설정.
////        // 버튼이 클릭되면 버튼에 지정된 색상값(태그)을 리스너에 전달한다.
////        // 리스너에 전달하고 나서 액티비티를 종료한다.
////        aItem.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) { //여기서 v는 버튼.
////                if (ColorPaletteDialog.listener != null) {
////                    ColorPaletteDialog.listener.onColorSelected(((Integer)v.getTag()).intValue());
////                }
////
////                ((ColorPaletteDialog)mContext).finish();
////            }
////        });
////        //그리고 버튼을 리턴한다.
////        return aItem;
////    }
////}

