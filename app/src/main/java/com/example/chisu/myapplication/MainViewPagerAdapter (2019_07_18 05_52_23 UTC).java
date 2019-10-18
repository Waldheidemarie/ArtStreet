package com.example.chisu.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.chisu.myapplication.pages.page_five;
import com.example.chisu.myapplication.pages.page_four;
import com.example.chisu.myapplication.pages.page_one;
import com.example.chisu.myapplication.pages.page_three;
import com.example.chisu.myapplication.pages.page_two;


//프래그먼트스테이트페이저어댑터는 프래그먼트를 세이브하고 리스토어하는 과정으로 메모리 사용을 절약한다.
public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    //메인 화면의 첫 번째 탭의 뷰페이저에 쓰는 페이저 어댑터.

    private static int PAGE_NUMBER = 5;

    public MainViewPagerAdapter(FragmentManager fm){
        super(fm);
    }

    //이 페이지에 보여줄 아이템을 얻어와라.
    @Override
    public Fragment getItem(int position){

        switch (position){
            case 0:
                return page_one.newInstance();
            case 1:
                return page_two.newInstance();
            case 2:
                return page_three.newInstance();
            case 3:
                return page_four.newInstance();
            case 4:
                return page_five.newInstance();

            default:
                return null;
        }
    }

    @Override
    public int getCount(){
        return PAGE_NUMBER;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "최신 순";
            case 1:
                return "예술 점수 순";
            case 2:
                return "인기 순";
            case 3:
                return "명예의 전당";
            case 4:
                return "무작위 섞기";

            default:
                return null;
        }
    }

}
