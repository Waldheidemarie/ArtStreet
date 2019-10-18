package com.example.chisu.myapplication.Chat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by jisu7 on 2018-03-06.
 */

public class chatService extends Service {

    private Socket socket;

    BufferedReader socket_in;

    PrintWriter socket_out;

    String data;



    @Override
    public void onCreate() {
        super.onCreate();

        //소켓 통신 스레드.
        Thread worker = new Thread() {

            public void run() {

                try {
                    //소켓을 생성한다.
                    socket = new Socket("ec2-13-124-241-71.ap-northeast-2.compute.amazonaws.com", 5555);

                    socket_out = new PrintWriter(socket.getOutputStream(), true);

                    socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                } catch (IOException e) {

                    e.printStackTrace();

                }

                try {

                    while (true) {
                        //데이터를 얻는다.
                        data = socket_in.readLine();

//                        output.post(new Runnable() {
//
//                            public void run() {
//
//                                output.setText(data);
//
//                            }
//
//                        });

                    }

                } catch (Exception e) {

                }

            }

        };

        worker.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            socket.close();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
