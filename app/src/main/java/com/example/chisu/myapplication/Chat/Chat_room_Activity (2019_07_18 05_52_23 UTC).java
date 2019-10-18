package com.example.chisu.myapplication.Chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.chisu.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//import com.mymusic.orvai.high_pitched_tone.Shared_Preference.SharedPrefManager;
//import com.mymusic.orvai.high_pitched_tone.adapters.Chat_msg_view_adapter;
//import com.mymusic.orvai.high_pitched_tone.models.Chat_msg;
//import com.mymusic.orvai.high_pitched_tone.models.User;

public class Chat_room_Activity extends AppCompatActivity {

    String user_id, receive_data, chat_user_id, chat_msg, user_pic_url, chat_server_time, room_number;
//    ImageButton img_btn;
    ListView listView;

    MessagesListAdapter adapter;
    List<MessageItem> msg_list;
    Intent intent;
    Handler handler;

    EditText chat_e_text;
    Button send_btn;
    int mdy_code;

    private static final String SEPARATOR = "|";

    private static final int YES_QUITROOM = 2031;
    private static final int YES_SENDWORD = 2051;

    private static final int MDY_ROOMUSER = 2023;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);

        //유저 아이디. 이름.
//        user_id = SharedPrefManager.getInstance(getApplicationContext()).getUser().getUser_id();
        user_id = "heaven";

        listView = findViewById(R.id.list_view_messages);
        chat_e_text = findViewById(R.id.inputMsg);
        send_btn = findViewById(R.id.btnSend);
//        img_btn = findViewById(R.id.chat_album);
        msg_list = new ArrayList<>();

        //센드버튼을 클릭하면
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //글자를 얻어서 서비스에 인텐트로 글자들을  보내준다.
                String msg = chat_e_text.getText().toString();
                if (!msg.equals("") && !msg.startsWith("|") && !msg.endsWith("|")) {
                    intent = new Intent("SOCKET_SERVICE");
                    intent.putExtra("mode", "sending_msg");
                    intent.putExtra("msg", msg);
                    sendBroadcast(intent);
                    chat_e_text.setText("");
                }
            }
        });


//        img_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, 100);
//            }
//        });
        Log.e("onCreate 완료","");

        Intent intent1 = new Intent(getApplicationContext(), Socket_Service.class);
        startService(intent1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter("MY_ACTIVITY"));

        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        intent = new Intent("SOCKET_SERVICE");
        intent.putExtra("mode", "quit_room");
        sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            String image_temp = String.valueOf(imageUri);
            intent = new Intent("SOCKET_SERVICE");
            intent.putExtra("mode", "sending_image");
            intent.putExtra("image", image_temp);
            sendBroadcast(intent);

        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receive_data = intent.getStringExtra("service_msg");
            Log.d("kb_리시빙데이터2", receive_data);
            StringTokenizer st = new StringTokenizer(receive_data, SEPARATOR);
            int command = Integer.parseInt(st.nextToken());

            switch (command) {

                case MDY_ROOMUSER: { // 2023|유저아이디|코드(0은 퇴장, 1은 입장, 2는 강퇴)|방에 남아있는 유저 아이디들
                    chat_user_id = st.nextToken();
                    mdy_code = Integer.parseInt(st.nextToken());
                    msg_list.add(new MessageItem("ddd", "메시지 1", true));

//                    msg_list.add(new MessageItem(chat_user_id, null, null, mdy_code, null, false));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    break;
                }

                case YES_SENDWORD: { // 2051|유저아이디|유저사진url|방번호|메시지내용
                    chat_user_id = st.nextToken();
                    user_pic_url = st.nextToken();
                    room_number = st.nextToken(); // 씨잘데기 없음
                    chat_server_time = st.nextToken();
                    chat_msg = st.nextToken();
                    if (chat_msg.length() >= 1000) { // 메시지 크기가 비정상적으로 큰 경우... 이미지로 판단
//                        msg_list.add(new MessageItem(chat_user_id, chat_msg, user_pic_url, 3, chat_server_time, true));
                        msg_list.add(new MessageItem("ddd", "메시지 1", true));

                    } else {
//                        msg_list.add(new MessageItem(chat_user_id, chat_msg, user_pic_url, 3, chat_server_time, false));
                        msg_list.add(new MessageItem("ddd", "메시지 1", true));

                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    break;
                }
                case YES_QUITROOM: {

                    break;
                }
            }
        }
    };


}