package com.example.chisu.myapplication.Chat;//package com.example.jisu7.artstreet;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.Switch;
//import android.widget.Toast;
//
//import java.util.StringTokenizer;
//
//public class Chat_room_create_Activity extends AppCompatActivity {
//
//    EditText e_subject, e_password;
//    RadioGroup radioGroup;
//    RadioButton radioButton;
//    Button register_btn, cancel_btn;
//    Switch sw;
//
//    String subject, password, receive_data;
//    public static int entering_room_no;
//    String user_max = "2";
//    int islock = 0; // 0은 안잠긴 것, 1은 잠긴 것
//    int radio_default_id = 2131230930;
//
//    private static final String SEPARATOR = "|";
//
//    private static final int YES_CREATEROOM = 2011;
//    private static final int NO_CREATEROOM = 2012;
//
//    private static final int ERR_ROOMSFULL = 3011;
//    private static final int ERR_ROOMERFULL = 3021;
//    private static final int ERR_REJECTION = 3031;
//    private static final int ERR_NOUSER = 3032;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat_room_create);
//
//        e_subject = (EditText) findViewById(R.id.room_create_subject);
//        e_password = (EditText) findViewById(R.id.room_create_password);
//        radioGroup = (RadioGroup) findViewById(R.id.room_create_usermax);
//        register_btn = (Button) findViewById(R.id.room_create_reg);
//        cancel_btn = (Button) findViewById(R.id.room_create_cancel);
//        sw = (Switch) findViewById(R.id.room_create_lock);
//        e_password.setEnabled(false);
//
//        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b == true) {
//                    e_password.setEnabled(true);
//                    islock = 1;
//                    sw.setText("비공개");
//                    sw.setTextColor(Color.RED);
//                    e_password.setText("");
//                } else {
//                    e_password.setEnabled(false);
//                    islock = 0;
//                    sw.setText("공개");
//                    sw.setTextColor(Color.BLUE);
//                    e_password.setText("");
//                }
//
//            }
//        });
//
//        register_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (islock == 0) { // 안잠겼을 때
//                    subject = e_subject.getText().toString(); // 제목
//                    password = "0"; // 공개방일 때는 비밀번호 0
//                    radio_default_id = radioGroup.getCheckedRadioButtonId();
//                    radioButton = (RadioButton) findViewById(radio_default_id);
//                    user_max = String.valueOf(radioButton.getTag()); // 최대인원 ★☆★☆★☆★☆ 선택 안했을 때 오류있음!!!!!!!!!!!!!!!!!★☆★☆★☆★☆★☆
//                    if (!subject.equals("")) {
//                        Intent intent = new Intent("SOCKET_SERVICE");
//                        intent.putExtra("mode", "create_room");
//                        intent.putExtra("room_subject", subject);
//                        intent.putExtra("room_password", password);
//                        intent.putExtra("room_maxuser", user_max);
//                        intent.putExtra("room_islock", islock);
//                        sendBroadcast(intent);
//                    }else{
//                        Toast.makeText(Chat_room_create_Activity.this, "빠진 사항이 없나 확인하세요.", Toast.LENGTH_SHORT).show();
//                    }
//                } else { // 잠겼을 때
//                    subject = e_subject.getText().toString(); // 제목
//                    password = e_password.getText().toString(); // 비공개방일 때 비밀번호
//                    radio_default_id = radioGroup.getCheckedRadioButtonId();
//                    radioButton = (RadioButton) findViewById(radio_default_id);
//                    user_max = String.valueOf(radioButton.getTag()); // 최대인원 최대인원 ★☆★☆★☆★☆ 선택 안했을 때 오류있음!!!!!!!!!!!!!!!!!★☆★☆★☆★☆★☆
//                    if (!subject.equals("") && !password.equals("")) {
//                        Intent intent = new Intent("SOCKET_SERVICE");
//                        intent.putExtra("mode", "create_room");
//                        intent.putExtra("room_subject", subject);
//                        intent.putExtra("room_password", password);
//                        intent.putExtra("room_maxuser", user_max);
//                        intent.putExtra("room_islock", islock);
//                        sendBroadcast(intent);
//                    }else{
//                        Toast.makeText(Chat_room_create_Activity.this, "빠진 사항이 없나 확인하세요.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            }
//        });
//
//        cancel_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
//
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        registerReceiver(broadcastReceiver, new IntentFilter("MY_ACTIVITY"));
//    }
//
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(broadcastReceiver);
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//
//
//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            receive_data = intent.getStringExtra("service_msg");
//            Log.d("kb_리시빙데이터3",receive_data);
//            StringTokenizer st = new StringTokenizer(receive_data, SEPARATOR);
//            int command = Integer.parseInt(st.nextToken());
//
//            switch(command) {
//                case YES_CREATEROOM: // 방만들고 인텐트로 방만 이동하면 된다. 대신에, Socket_Service에 내가 지금 몇번 방으로 이동했는지 브로드캐스트로 보내야 한다.
//                    entering_room_no = Integer.parseInt(st.nextToken());
//                    Intent intent2 = new Intent("SOCKET_SERVICE");
//                    intent2.putExtra("ct_room_number",entering_room_no);
//                    Log.d("kb룸넘버3", String.valueOf(entering_room_no));
//                    sendBroadcast(intent2);
//                    finish();
//                    startActivity(new Intent(getApplicationContext(),Chat_room_Activity.class));
//                    break;
//            }
//
//        }
//    };
//
//}
