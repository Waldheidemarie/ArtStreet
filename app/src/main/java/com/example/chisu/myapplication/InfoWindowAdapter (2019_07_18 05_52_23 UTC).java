package com.example.chisu.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by jisu7 on 2018-03-01.
 */
//구글맵의 인포윈도우 어댑터
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final Hashtable<String, Boolean> markerSet;
    private Context context;
    private View myContentsView;
    private List<art> locationList;

    public InfoWindowAdapter(Context context, Hashtable<String, Boolean> markerSet, List<art> locationList) {
        this.context = context;
        this.markerSet = markerSet;
        this.locationList = locationList;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //클릭한 마커가 현재 위치의 마커라면 아무것도 하지 않는다. 즉 기본 형태의 마커를 내놓는다.
        if (marker.getTitle().equals("현재 위치")){
            return null;
        } else {
            //아니라면 그림을 나타내는 마커를 내놓는다.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            myContentsView = inflater.inflate(R.layout.custom_infowindow, null);

            ImageView infoPic = myContentsView.findViewById(R.id.infoPic);

            TextView nameTxt = myContentsView.findViewById(R.id.nameTxt);
            TextView valueTxt = myContentsView.findViewById(R.id.valueTxt);
            TextView addressTxt = myContentsView.findViewById(R.id.addressTxt);
            boolean isImageLoaded;
            try{
                isImageLoaded = markerSet.get(marker.getId());

                if (isImageLoaded) {
                    Picasso.with(context)
                            .load(locationList.get(Integer.parseInt(marker.getSnippet())).getImage())
                            .placeholder(android.R.color.white)
                            .into(infoPic);

                    nameTxt.setText(locationList.get(Integer.parseInt(marker.getSnippet())).getTitle());
                    valueTxt.setText(locationList.get(Integer.parseInt(marker.getSnippet())).getValue());
                    addressTxt.setText(String.valueOf(MainMapActivity.distance2)+"m");

                    Log.e("작품의 가치 ",locationList.get(Integer.parseInt(marker.getSnippet())).getValue());

                } else {
                    isImageLoaded = true;
                    markerSet.put(marker.getId(), isImageLoaded);
                    Picasso.with(context)
                            .load(locationList.get(Integer.parseInt(marker.getSnippet())).getImage())
                            .placeholder(android.R.color.white)
                            .into(infoPic, new InfoWindowRefresher(marker));

                    nameTxt.setText(locationList.get(Integer.parseInt(marker.getSnippet())).getTitle());
                    valueTxt.setText(locationList.get(Integer.parseInt(marker.getSnippet())).getValue());

                }
            }catch (Exception e){
                //인포 윈도우를 나타내는 과정에서 에러가 발생할 경우
                e.printStackTrace();
            }
        }


        return myContentsView;
    }

     class InfoWindowRefresher implements Callback {
        private Marker markerToRefresh = null;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();

//            if (markerToRefresh != null && markerToRefresh.isInfoWindowShown()) {
//                markerToRefresh.hideInfoWindow();
//                markerToRefresh.showInfoWindow();
//            }
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");

        }
    }
}