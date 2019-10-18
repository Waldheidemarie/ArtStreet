package com.example.chisu.myapplication;

/**
 * Created by jisu7 on 2018-03-02.
 */
//Drawings 액티비티에 나타날 아이템의 클래스.
public class DrawingsRecyclerviewItem {

    private String drawingsId ;
    private String drawingsImage ;
    private String drawingsUser;

    //아이템의 생성자
    public DrawingsRecyclerviewItem(String drawingsId1, String drawingsImage1, String drawingsUser1){
        this.drawingsId = drawingsId1;
        this.drawingsImage = drawingsImage1;
        this.drawingsUser = drawingsUser1;
    }


    public void setDrawingsId(String drawingsId1) {
        drawingsId = drawingsId1 ;
    }
    public void setDrawingsImage(String drawingsImage1) {
        drawingsImage = drawingsImage1 ;
    }

    public void setDrawingsUser(String drawingsUser1) {
        drawingsUser = drawingsUser1 ;
    }


    public String getDrawingsId() {
        return this.drawingsId ;
    }
    public String getDrawingsImage() {
        return this.drawingsImage ;
    }
    public String getDrawingsUser() {
        return this.drawingsUser ;
    }

}
