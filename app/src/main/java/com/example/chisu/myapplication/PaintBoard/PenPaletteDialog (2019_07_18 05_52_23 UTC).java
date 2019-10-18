package com.example.chisu.myapplication.PaintBoard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chisu.myapplication.R;


public class PenPaletteDialog extends Activity {

    //    GridView grid;
    Button closeBtn;
    //    PenDataAdapter adapter;

    //seekbar의 수준을 넣을 변수
    int getPro;

    TextView tv;
    SeekBar sb;

    //리스너를 미리 선언해둔다.
    public static OnPenSeekedListener seekedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.pen_width_dialog);

        this.setTitle("ddd");

         tv = findViewById(R.id.tv);
         sb = findViewById(R.id.seekBar);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv.setText("너비 : " + progress);
                getPro = progress;

                PenPaletteDialog.seekedListener.onPenSelected(getPro);

        }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                tv.setText("난이도를 설정하지 마세요.");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv.setText("너비 : " + getPro);
            }
        });



        //        grid =  findViewById(R.id.colorGrid);

//
//        grid.setColumnWidth(14);
//        grid.setBackgroundColor(Color.GRAY);
//        grid.setVerticalSpacing(4);
//        grid.setHorizontalSpacing(4);
//
//        adapter = new PenDataAdapter(this);
//        grid.setAdapter(adapter);
//        grid.setNumColumns(adapter.getNumColumns());
//

        //닫기 버튼을 누르면 닫힘
        closeBtn =  findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

//        int areaWidth = 10;
//        int areaHeight = 20;
//
//        Canvas penCanvas = new Canvas();
//        penCanvas.setBitmap(penBitmap);
//
//        Paint mPaint = new Paint();
//        mPaint.setColor(Color.WHITE);
//        penCanvas.drawRect(0, 0, areaWidth, areaHeight, mPaint);
//
//        mPaint.setColor(Color.BLACK);
//        //이 함수에 따라 펜의 두께가 달라진다. 이거를 만지면 될 거 같은데?
//        mPaint.setStrokeWidth(getPro);
//        penCanvas.drawLine(0, areaHeight/2, areaWidth-1, areaHeight/2, mPaint);
////        BitmapDrawable penDrawable = new BitmapDrawable(mcontext.getResources(), penBitmap);
//

    }

}

//싴바의 활용으로 더 이상 쓰이지 않지만 기억을 위해 남겨둔다. 펜 두께 그리드뷰로 선택할 때.
//class PenDataAdapter extends BaseAdapter {
//
//    /**
//     * Application Context
//     */
//    Context mContext;
//
//    /**
//     * Pens defined
//     */
//    public static final int [] pens = new int[] {
//            1,3,5,7,9,
//            11,13,15,17,20,
//            23,26,28,30,33
//    };
//
//    int rowCount;
//    int columnCount;
//
//
//
//    public PenDataAdapter(Context context) {
//        super();
//
//        mContext = context;
//        rowCount = 3;
//        columnCount = 5;
//
//    }
//
//    public int getNumColumns() {
//        return columnCount;
//    }
//
//    public int getCount() {
//        return rowCount * columnCount;
//    }
//
//    public Object getItem(int position) {
//        return pens[position];
//    }
//
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    public View getView(int position, View view, ViewGroup group) {
//
//        Log.d("PenDataAdapter", "getView(" + position + ") called.");
//
//        // calculate position
//        int rowIndex = position / rowCount;
//        int columnIndex = position % rowCount;
//        Log.d("PenDataAdapter", "Index : " + rowIndex + ", " + columnIndex);
//
//        GridView.LayoutParams params = new GridView.LayoutParams(
//                GridView.LayoutParams.MATCH_PARENT,
//                GridView.LayoutParams.MATCH_PARENT);
//
//        // create a Pen Image
//        int areaWidth = 10; //펜 굵기 이미지의 가로길이
//        int areaHeight = 20; // 세로길이
//
//        Canvas penCanvas = new Canvas();
//        penCanvas.setBitmap(penBitmap);
//
//        Paint mPaint = new Paint();
//        //mPaint가 penCanvas에 흰색의 Rect 객체를 그린다.
//        mPaint.setColor(Color.WHITE);
//        penCanvas.drawRect(0, 0, areaWidth, areaHeight, mPaint);
//
//        //그리고 나서 mPaint는 검은색으로 전환된다.
//        //그래서 penCanvas 안에 흰 rect객체 안에 검은 줄을 그리기 시작한다.
//        mPaint.setColor(Color.BLACK);
//        //그릴 검은 줄의 두께를 산정한다.
//        mPaint.setStrokeWidth((float)pens[position]/3);
//        //검은색 라인을 mPaint로 그린다. start, stop은 각각 처음과 끝을 의미한다. 그러니까 만지려면 양쪽 다 만져줘야 하는 것이다.
//        //stopX에 -1을 함으로서 흰 rect 객체를 가로로 가득 채우지 않게 한다.
//        //startY와 stopY가 일치하면 어떻게 되는 거지?
//        penCanvas.drawLine(0, areaHeight/2, areaWidth-1, areaHeight/2, mPaint);
//
//        //bitmapdrawable : 비트맵은 비트맵인데 좀 더 메모리 누출 가능성이 적은 것.
//        BitmapDrawable penDrawable = new BitmapDrawable(mContext.getResources(), penBitmap);
//
//        // 버튼 만들기. 현재 이 버튼에는 아무것도 담겨 있지 않다.
//        // create a Button with the color
//        Button aItem = new Button(mContext);
//        aItem.setText(" ");
//        //그리드뷰에 미리 설정해둔 레이아웃파라미터들을 적용시킨다.
//        aItem.setLayoutParams(params);
//        //내부 패딩 설정.
//        aItem.setPadding(4, 4, 4, 4);
//        //버튼의 배경을 미리 만들어 놓은 penDrawable로 채운다.
//        aItem.setBackgroundDrawable(penDrawable);
//        //버튼의 높이 및 태그 설정. 태그는 펜의 두께가 담겨 있다.
//        aItem.setHeight(120);
//        aItem.setTag(pens[position]);
//
//        // 아이템 버튼에 리스너를 단다. 리스너를 클릭하면 그 아이템의 태그 값을 리턴하고 다이얼로그를 닫는다.
//        // 즉 닫는 기능은 펜 굵기를 클릭하나 닫기를 클릭하나 똑같이 다이얼로그를 닫는 효과가 있다.
//        // set listener
//        aItem.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                if (PenPaletteDialog.listener != null) {
////                    //펜팔렛다이얼로그의 리스너의 onpenselected 메소드를 실행. 인자는 버튼의 태그값을 넣어준다.
////                    //근데 이 메소드에는 아무것도 없는데?
////                    PenPaletteDialog.listener.onPenSelected(((Integer)v.getTag()).intValue());
////                }
//                //컨텍스트는 곧 그 액티비티를 의미하는가? 강제형변환을 통해 더욱 강조하는 느낌.
//                ((PenPaletteDialog)mContext).finish();
//            }
//        });
//
//        return aItem;
//    }

