package com.example.chisu.myapplication.Chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chisu.myapplication.R;

//채팅 내용 액티비티
public class ChatList extends Activity {

    ClientThread client;

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list);

    }


    @Override

    protected void onStop() {

        super.onStop();


    }

    public void goToChatRoom(View v){
        Intent intent = new Intent(getApplicationContext(), ChattingRoom.class);
        startActivity(intent);
    }

    public void createChatRoom(View v){
        Intent intent = new Intent(getApplicationContext(), ChattingRoom.class);
        startActivity(intent);
//        requestCreate
    }
}
//
//public class ChatList extends Activity {
//
//    private Socket socket;
//
//    BufferedReader socket_in;
//
//    PrintWriter socket_out;
//
//    EditText input;
//
//    Button button;
//
//    TextView output;
//
//    String data;
//
//
//    @Override
//
//    public void onCreate(Bundle savedInstanceState) {
//
//        // TODO Auto-generated method stub
//
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.chat_list);
//
//        input = findViewById(R.id.input);
//
//        button = findViewById(R.id.button);
//
//        output = findViewById(R.id.output);
//
//        button.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//
//                String data = input.getText().toString();
//
//                Log.e("NETWORK", " " + data);
//
//                if (data != null) {
//
//                    socket_out.println(data);
//
//                }
//
//            }
//
//        });
//
//
//        Thread worker = new Thread() {
//
//            public void run() {
//
//                try {
//
//                    socket = new Socket("ec2-13-124-241-71.ap-northeast-2.compute.amazonaws.com", 5555);
//
//                    socket_out = new PrintWriter(socket.getOutputStream(), true);
//
//                    socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//
//                }
//
//                try {
//
//                    while (true) {
//
//                        data = socket_in.readLine();
//
//                        output.post(new Runnable() {
//
//                            public void run() {
//
//                                output.setText(data);
//
//                            }
//
//                        });
//
//                    }
//
//                } catch (Exception e) {
//
//                }
//
//            }
//
//        };
//
//        worker.start();
//
//    }
//
//
//    @Override
//
//    protected void onStop() {
//
//        // TODO Auto-generated method stub
//
//        super.onStop();
//
//        try {
//
//            socket.close();
//
//        } catch (IOException e) {
//
//            e.printStackTrace();
//
//        }
//
//    }
//
//
//}