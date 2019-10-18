package com.example.chisu.myapplication;

/**
 * Created by jisu7 on 2018-02-04.
 */

public class profileGridViewItem {

    private String numberStr ;
    private String textStr ;

//    public profileGridViewItem(String numberStr, String textStr){
//        this.numberStr = numberStr;
//        this.textStr = textStr;
//    }

    public void setNumberStr(String number) {
        numberStr = number ;
    }
    public void setTextStr(String text) {
        textStr = text ;
    }

    public String getNumberStr() {
        return this.numberStr ;
    }
    public String getTextStr() {
        return this.textStr ;
    }


}
