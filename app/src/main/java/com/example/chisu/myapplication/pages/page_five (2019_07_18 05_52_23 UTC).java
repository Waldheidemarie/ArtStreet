package com.example.chisu.myapplication.pages;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.chisu.myapplication.MainRecyclerViewAdapter;
import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.URLs;
import com.example.chisu.myapplication.art;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class page_five extends Fragment implements RecyclerView.OnScrollChangeListener{

    //프래그먼트에서 컨텍스트를 얻기 위한 액티비티
    Activity activity;

    private List<art> artList;

    Context context;

    //발리 요청 큐 선언
    private RequestQueue requestQueue;

    //페이지 정보를 보내기 위한 리퀘스트 카운트
    //The request counter to send ?page=1, ?page=2  requests
    private int requestCount = 1;


    public page_five() {
        // Required empty public constructor
    }

    View v;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;

    public static page_five newInstance(){
        Bundle args = new Bundle();

        page_five fragment = new page_five();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //액티비티 역할을 위한 컨텍스트 얻기
        context = getContext();
        activity = getActivity();

        //선언한 전역 변수들 초기화하기 및 설정
        v = inflater.inflate(R.layout.fragment_page_5, container, false);

        recyclerView = v.findViewById(R.id.recyclerView5);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(layoutManager);

        artList = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(activity);

        //Adding an scroll change listener to recyclerview
        recyclerView.setOnScrollChangeListener(this);

        //initializing our adapter
//        adapter = new MainRecyclerViewAdapter(artList, activity);

        // 레이아웃을 새로 인플레이트하지 않고 과거에 인플레이트했던 v를 그대로 리턴했더니 잘 작동한다.
        // 이 점은 잘 체크하고 잊지 말 것...이지만 아마 전자의 방법도 내가 잘못 실행해서 잘못된 결과가 나왔을 것이다.
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        //게시물 삭제 후 리스트 업데이트를 위해 클리어를 해준다. 또한 페이징을 최초로 돌린다.
        artList.clear();
        requestCount = 1;
        adapter = new MainRecyclerViewAdapter(artList, activity);
        recyclerView.setAdapter(adapter);

        getData();

    }

    //Request to get json from server we are passing an integer here
    //This integer will used to specify the page number for the request ?page = requestcount
    //This method would return a JsonArrayRequest that will be added to the request queue
    private JsonArrayRequest getDataFromServer(int requestCount) {
        //Initializing ProgressBar
        final ProgressBar progressBar = v.findViewById(R.id.progressBar5);

        //Displaying Progressbar
        progressBar.setVisibility(View.VISIBLE);
        activity.setProgressBarIndeterminateVisibility(true);

        //JsonArrayRequest of volley
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLs.FEED + "?apicall=fifth&page=" + String.valueOf(requestCount),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Calling method parseData to parse the json response
                        parseData(response);
                        //Hiding the progressbar
                        progressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() { //에러를 들을 경우에
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        //If an error occurs that means end of the list has reached
//                        Toast.makeText(activity, "더 이상 그림이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        //Returning the request
        return jsonArrayRequest;
    }

    //스크롤을 내릴 때 페이징하는 메소드.
    private void getData() {
        //리퀘스트큐에 리퀘스트 올리기
        requestQueue.add(getDataFromServer(requestCount));
        requestCount++;
    }

    //This method will parse json data
    private void parseData(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            //art 객체 생성하기.
            art artExample = new art();
            JSONObject json;
            try {
                //Getting json
                json = array.getJSONObject(i);

                //art 객체에 관련 정보 탑재하기
                artExample.setImage(json.getString("image"));
                artExample.setTitle(json.getString("title"));
                artExample.setUser(json.getString("user"));
                artExample.setProductId(json.getInt("productId"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("파스데이터 완료. : ", "");

            //리스트에 객체 올리기
            artList.add(artExample);
        }
        adapter.notifyDataSetChanged();
    }

    //리사이클러뷰의 스크롤이 맨 아래에 도달했는지 체크하는 메소드
    //This method would check that the recyclerview scroll has reached the bottom or not
    private boolean isLastItemDisplaying(RecyclerView recyclerView) {
        //리사이클러뷰의 어댑터의 아이템카운트가 0이 아니라면
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }

    //스크롤링을 감지하기 위한 오버라이드메소드.
    //Overriden method to detect scrolling
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        //If scrolled at last then
        if (isLastItemDisplaying(recyclerView)) {
            //Calling the method getdata again
            getData();
        }
    }




}

