package com.example.chisu.myapplication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Volley RequestQueue의 싱글 인스턴스를 사용하는 생각은 항상 좋다.
 * 여기서는 모든 requestQueue를 조종하기 위해 싱글톤 패턴을 사용할 것이다.
 */
public class VolleySingleton {

    //객체들을 생성한다.
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    //생성자를 통해 초기화한다.
    private VolleySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }
    //synchronize는 하나의 객체에 여러 객체가 접근하는 것을 차단한다.
    //그러니까 getinstance는 싱글톤 기법의 일부라고 보면 된다.
    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    //requestqueue가 없을 때 발동하는 메소드
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
