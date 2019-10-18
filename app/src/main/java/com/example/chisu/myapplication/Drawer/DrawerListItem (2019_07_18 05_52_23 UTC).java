package com.example.chisu.myapplication.Drawer;

/**
 * Created by jisu7 on 2018-02-12.
 */
//드로어 내비게이션 바의 리스트 아이템 정의.
public class DrawerListItem {

    int drawerImageView;
    String drawerTextView;

    public void setIcon(int icon) {
        drawerImageView = icon ;
    }
    public void setTitle(String text) {
        drawerTextView = text ;
    }

    public int getIcon() {
        return this.drawerImageView ;
    }
    public String getTitle() {
        return this.drawerTextView ;
    }

}
