package com.example.chisu.myapplication.Chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chisu.myapplication.R;
import com.example.chisu.myapplication.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

//채팅방 클래스. 어떤 유저의 프로필에 들어가서 메시지보내기를 클릭하면 나타나는 클래스이다.
public class ChattingRoom extends AppCompatActivity {

    //서비스와 통신을 하기 위한 메신저
    private Messenger mServiceMessenger = null;

    //바인드가 되어 있는지 판별하기 위한 플래그 변수.
    private boolean mIsBound;

    //보내기 버튼
    Button btnSend;
    //보낼 채팅을 입력하는 애딧텍스트
    EditText inputMsg;
    ClientThread thread;

    //작업하는 어댑터
     MessagesListAdapter adapter;
    List<MessageItem> messageItemList;
    ListView list_view_messages;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //리스트뷰 연결 및 어댑터 설치
        list_view_messages = findViewById(R.id.list_view_messages);
        messageItemList = new ArrayList<>();
        adapter = new MessagesListAdapter(ChattingRoom.this, messageItemList);
        list_view_messages.setAdapter(adapter);

        handler = new Handler();

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                try{
//
//                    thread = new ClientThread(ChattingRoom.this, messageItemList);
////                    Thread.sleep(200);
//                    thread.start();
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//
//            }
//        });

        inputMsg = findViewById(R.id.inputMsg);
        btnSend = findViewById(R.id.btnSend);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    String inputedMsg;
    @Override
    protected void onResume() {
        super.onResume();

        try {//시간초를 좀 준다.
            Thread.sleep(200);
        }catch (Exception e){}

        Intent intent = getIntent();
        String isEnter = intent.getStringExtra("room");
        //인텐트 값이 create라면
        if (isEnter.equals("create")){
            //서비스를 시작한다.
            Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
            setStartService();
//            Intent intent2 = new Intent(ChattingRoom.this,MyService.class);
//            //인텐트를 보냄으로써 서비스를 시작하고 종료할 수 있다.
//            startService(intent2);

        }

        //보내기 버튼 클릭리스너.
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //보내기 버튼을 클릭하면 애딧텍스트에 있는 글을 가져온다.
                 inputedMsg = inputMsg.getText().toString();

                //이제 가져온 이 스트링을 서비스로 보내주어야 한다.
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //서비스로 딴 글자를 보내준다.
                        sendMessageToService("from main");

                        thread.requestSendWord(inputedMsg);
                        inputMsg.setText("");

                        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
                        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);
                        try{
                            thread.join(500);
                        }catch (Exception e){}
                        MessageItem messageItem = new MessageItem(username, thread.dataString, true);
                        messageItemList.add(messageItem);
                        adapter.notifyDataSetChanged();

                    }
                });

            }
        });
    }

    /** 서비스 시작 및 Messenger 전달 */
    private void setStartService() {
        startService(new Intent(ChattingRoom.this, MyService.class));
        //서비스 시작 및 바인드.
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /** Service 로 메시지를 보냄 */
    private void sendMessageToService(String str) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /** Service 로 부터 message를 받음 */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("test","act : what "+msg.what);
            switch (msg.what) {
                case MyService.MSG_SEND_TO_ACTIVITY:
                    int value1 = msg.getData().getInt("fromService");
                    String value2 = msg.getData().getString("test");
                    Log.i("test","act : value1 "+value1);
                    Log.i("test","act : value2 "+value2);
                    break;
            }
            return false;
        }
}));
}


//서비스 적용 전의 코드를 주석으로 저장
//package com.example.jisu7.artstreet;
//
//        import android.content.Intent;
//        import android.content.SharedPreferences;
//        import android.os.Bundle;
//        import android.os.Handler;
//        import android.os.StrictMode;
//        import android.support.v7.app.AppCompatActivity;
//        import android.view.View;
//        import android.widget.Button;
//        import android.widget.EditText;
//        import android.widget.ListView;
//
//        import java.util.ArrayList;
//        import java.util.List;
//
////채팅방 클래스. 어떤 유저의 프로필에 들어가서 메시지보내기를 클릭하면 나타나는 클래스이다.
//public class ChattingRoom extends AppCompatActivity {
//
//    //보내기 버튼
//    Button btnSend;
//    //보낼 채팅을 입력하는 애딧텍스트
//    EditText inputMsg;
//    ClientThread thread;
//
//    //작업하는 어댑터
//    MessagesListAdapter adapter;
//    List<MessageItem> messageItemList;
//    ListView list_view_messages;
//
//    Handler handler;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chatting_room);
//
//        if (android.os.Build.VERSION.SDK_INT > 9)
//        {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
//        //리스트뷰 연결 및 어댑터 설치
//        list_view_messages = findViewById(R.id.list_view_messages);
//        messageItemList = new ArrayList<>();
//        adapter = new MessagesListAdapter(ChattingRoom.this, messageItemList);
//        list_view_messages.setAdapter(adapter);
//
//        handler = new Handler();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                try{
//
//                    thread = new ClientThread(ChattingRoom.this, messageItemList);
////                    Thread.sleep(200);
//                    thread.start();
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//
//            }
//        });
//
//
//        inputMsg = findViewById(R.id.inputMsg);
//        btnSend = findViewById(R.id.btnSend);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//    }
//    String inputedMsg;
//    @Override
//    protected void onResume() {
//        super.onResume();
//        try {
//            //시간초를 좀 준다.
//            Thread.sleep(200);
//
//        }catch (Exception e){
//
//        }
//
//        Intent intent = getIntent();
//        String isEnter = intent.getStringExtra("room");
//        if (isEnter.equals("create")){
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    thread.requestCreateRoom("a", 2, 0, "0");
//
//                }
//            });
//
//        } else {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        thread.join(500);
//
//                    }catch (Exception e){
//
//                    }
//                    thread.requestEnterRoom(thread.ct_roomNumber,  "0");
//
//                }
//            });
//
//        }
//
//
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //보내기 버튼을 클릭하면 애딧텍스트에 있는 글을 가져온다.
//                inputedMsg = inputMsg.getText().toString();
//
//                //이제 가져온 이 스트링을 서버로 보내주어야 한다. 그럴라면 클라이언트 스레드를 들고 와야 한다.
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        thread.requestSendWord(inputedMsg);
//                        inputMsg.setText("");
//
//                        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
//                        String username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);
//                        try{
//                            thread.join(500);
//
//                        }catch (Exception e){
//
//                        }
//                        MessageItem messageItem = new MessageItem(username, thread.dataString, true);
//                        messageItemList.add(messageItem);
//                        adapter.notifyDataSetChanged();
//
//                    }
//                });
//
//                //그리고 애딧텍스트는 초기화해준다.
//
//                //이제 서버에 올린 글을 다시 가져오는 걸 해보자.
//                //서버는 클라이언트가 올리자마자 바로 반사한다. 그러므로
//                //서버가 보내준 내용을 가지고 리스트뷰에 올려줘야 한다.
//                //즉 글을 올릴 때마다 리스트뷰에 아이템을 하나 추가해야 한다.
//                //리스트에 추가하고 노티파이 해주면 된다.
//                //서버에서 가져온 데이터들을 가지고 메시지 아이템을 만들어서 메시지 리스트에 넣어줘야 한다.
////                try{
////                    Thread.sleep(100);
////
////                }catch (Exception e){
////
////                }
////                Log.e("채팅 룸 dataString", thread.dataString);
////                thread.dataString
//            }
//        });
//    }
//
//    //애딧텍스트에 글을 입력하고 샌드버튼을 누르면 그 글을 서버에 전송하도록 한다.
//}



