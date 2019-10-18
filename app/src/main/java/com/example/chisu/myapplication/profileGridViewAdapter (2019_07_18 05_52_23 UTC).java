package com.example.chisu.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class profileGridViewAdapter  extends BaseAdapter {

    private ArrayList<profileGridViewItem> items = new ArrayList<>();

    //생성자
    public profileGridViewAdapter(){

    }

    @Override
    public int getCount() {
        return items.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.profile_grid_item, parent, false);
            convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 245));

        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView numberTextView = convertView.findViewById(R.id.textViewNumber) ;
        TextView textTextView = convertView.findViewById(R.id.textViewText) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        profileGridViewItem item = items.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        numberTextView.setText(item.getNumberStr());
        textTextView.setText(item.getTextStr());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return items.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String number, String text) {

        profileGridViewItem item = new profileGridViewItem();
        item.setNumberStr(number);
        item.setTextStr(text);

        items.add(item);
    }

    public void clear(){
        items.clear();
    }

}