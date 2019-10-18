package com.example.chisu.myapplication.Chat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Socket_Service extends Service {

    private Socket c_socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private int ServerPort = 5555;
    private String Server_Ip = "13.124.241.71";
    private StringBuffer ct_buffer;
    private Bitmap bitmap;

    private static String receive_data, user_id, user_pic_url;
    private static int ct_roomNumber;

    private Client_thread ct;

    private static final String SEPARATOR = "|";
    private static final String DELIMETER = "'";
    private static final String DELIMETER2 = "=";

    private static final int REQ_LOGON = 1001;
    private static final int REQ_CREATEROOM = 1011;
    private static final int REQ_ENTERROOM = 1021;
    private static final int REQ_QUITROOM = 1031;
    private static final int REQ_LOGOUT = 1041;
    private static final int REQ_SENDWORD = 1051;
    private static final int REQ_SENDWORDTO = 1052;
    private static final int REQ_COERCEOUT = 1053;
    private static final int REQ_SENDFILE = 1061;
    private static final int REQ_REFRESH_ROOM = 1071;

    private static boolean flag_Connection;


    public Socket_Service() {

    }

    @Override
    public void onCreate() {
        // startservice를 처음 사용할 경우 액티비티에서 startService를 실행할 경우 처음 호출.
        // bindservice를 처음 사용할 경우, 액티비티에서 bindService를 실행할 경우 처음 호출.
        // 단 서비스는 싱글톤이기 때문에 어플리케이션에서 실행된 경우 onCreate는 한번밖에 호출되지 않는다.
        // onCreate에서 생성자를 통해 클라이언트 소켓 스레드를 무한 시작함과 동시에 로그인까지 시도하겠다.
        super.onCreate();
        flag_Connection = true;
//        user_id = SharedPrefManager.getInstance(getApplicationContext()).getUser().getUser_id();
//        user_pic_url = SharedPrefManager.getInstance(getApplicationContext()).getUser().getUser_pic();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ct = new Client_thread(); // 소켓 스레드 생성
                try{
                    Thread.sleep(300);

                }catch (Exception e){

                }
                ct.start(); // 소켓 스레드 시작
                ct.request_Logon(user_id, user_pic_url); // 서버에 내 아이디를 매개변수, 사진 URL를 매개변수로 하여 로그인 요청을 보냄
                ct_roomNumber = 0; // 클라이언트 전역 변수 방번호를 0으로 바꿈(현재 로비에 있으니까)
            }
        }).start();
        registerReceiver(broadcastReceiver, new IntentFilter("SOCKET_SERVICE")); // 액티비티로부터 메시지를 받을 SOCKET_SERVICE란 이름의 브로드캐스트 리시버 등록

        Log.e("서비스 onCreate" ,"");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startservice 를 두번 째 이후로 사용할 경우 호출. 여러 액티비티에서 서비스를 반복하여 사용하고자 하는 경우 이것이 호출 됨
        // 클라이언트 소켓 생성은 한번만 필요하기 때문에 아직 딱히 쓸 필요는 없다고 생각.
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() { // bindservice, startservice에서 stopService 메소드를 호출하여 모든 서비스가 중지될 때 실행되는 콜백 메소드... 나중에 로그아웃 했을 때 여기다가 소켓 통신을 종료하도록....(완료)
        super.onDestroy();
        try {
            c_socket.close(); // 소켓 닫음
            flag_Connection = false; // 스레드 정지
            dis.close(); // datainputstream 닫음
            dos.close(); // dataoutputstream 닫음
            ct = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                c_socket.close(); // 소켓 마지막으로 확인차 한번 더 닫음
                dis.close(); // datainputstream 확인차 한번 더 닫음
                dos.close(); // dataoutputstream 확인차 한번 더 닫음
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag_Connection = false; // 스레드 마지막으로 정지
            ct = null; // 현재 스레드는 null값으로 지정
        }
        unregisterReceiver(broadcastReceiver); // 브로드캐스트 리시버 등록 해제
    }

    @Override
    public void onLowMemory() { // 메모리 적을 때 쓰이는 메소드... 나중에 생각해보자... 바로 onDestroy를 호출하도록 할 것인지...
        super.onLowMemory();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // bindservice 를 두번 째 이후로 사용할 경우 호출. 여러 액티비티에서 서비스를 사용하고자 하는 경우 이것이 호출 됨
        // 안 씀
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // bindservice 를 사용할 경우, 서비스 실행중이었던 걸 unbindservice를 실행했을 때 ondestroy전에 호출
        // 안 씀
        return super.onUnbind(intent);
    }

    /**
     * 여기부터 여러 액티비티로 부터 메시지를 받는 브로드캐스트를 정의함.
     */

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // 방 생성 정보, 메시지 정보, 파일 정보 등을 액티비티로부터 전달받고, 서버로 전송할 브로드캐스트 리시버
        @Override
        public void onReceive(Context context, final Intent intent) {
            String mode = intent.getStringExtra("mode");
            ct_roomNumber = intent.getIntExtra("ct_room_number", ct_roomNumber);
            Log.d("kb_룸넘버", String.valueOf(ct_roomNumber));
            if (mode != null) {
                switch (mode) { // 여기부터 액티비티로 부터 요청받은 것들(REQUEST CODE)을 서버로 전송할 목록들

                    case "refresh_room": { // 방 목록 갱신 요청
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ct.request_refresh();
                            }
                        }).start();
                        break;
                    }

                    case "create_room": { // 방 생성 요청
                        final String create_room_name = intent.getStringExtra("room_subject");
                        String create_room_maxuser_tmp = intent.getStringExtra("room_maxuser");
                        final int create_room_maxuser = Integer.parseInt(create_room_maxuser_tmp);
                        final int create_room_islock = intent.getIntExtra("room_islock",0);
                        final String create_room_password = intent.getStringExtra("room_password");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                ct.request_Create_Room(create_room_name, create_room_maxuser, create_room_islock, create_room_password);
                                ct.request_Create_Room("aaa", 2, 0, "0");
                            }
                        }).start();
                        break;
                    }
                    case "logout": { // 로그아웃 요청
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ct.request_Logout();
                            }
                        }).start();
                        break;
                    }

                    case "enter_room": { // 방 들어가기 요청
                        final int temp_room_number = Integer.parseInt(intent.getStringExtra("room_number"));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ct.request_Enter_Room(temp_room_number, String.valueOf(0));
                            }
                        }).start();
                        break;
                    }

                    case "quit_room": { // 방 나가기 요청
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ct.request_Quit_Room();
                            }
                        }).start();
                        break;

                    }

                    case "sending_msg": { // 메시지 보내는 요청
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String c_msg = intent.getStringExtra("msg");
                                ct.request_Sending_msg(c_msg);
                            }
                        }).start();

                        break;
                    }

                    case "sending_image": { // 이미지 보내는 요청
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String tmp_image = intent.getStringExtra("image");
                                Uri uri = Uri.parse(tmp_image);
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos); // 2번째 매개변수가 이미지 압축률 조절(0%~100%)
                                byte[] imgBytes = bos.toByteArray();
                                String image = Base64.encodeToString(imgBytes, Base64.DEFAULT);
                                ct.request_Sending_msg(image);
                            }
                        }).start();
                    }


                }
            }

        }
    };

    class Client_thread extends Thread {

        public Client_thread() { // 클라이언트 소켓 connect를 최초로 실행하기 위한 생성자, 74번 째 줄에서 ct = new Client_thread() 를 통하여 생성할 때 생성.
            try {
                c_socket = new Socket(); // 클라이언트 소켓을 생성한다.
                SocketAddress socket_addr = new InetSocketAddress(Server_Ip, ServerPort); // 서버 아이피와 서버 포트를 매개변수로 얻은 소켓 주소를 생성한다.
                c_socket.connect(socket_addr); // 얻은 주소를 매개변수로 하여 소켓을 서버에 연결한다.
                dis = new DataInputStream(c_socket.getInputStream()); // 서버로부터 메시지를 보낼 DataInputSteram을 생성한다.
                dos = new DataOutputStream(c_socket.getOutputStream()); // 서버에 메시지를 받을 DataOutputStream을 생성한다.
                ct_buffer = new StringBuffer(8192); // 구분자를 기준으로 하는 메시지를 전송하기 위해 StringBuffer를 생성한다.
            } catch (IOException e) {
                e.printStackTrace();
                Log.wtf("서버오류", e);
            }
        }

        @Override
        public void run() {
            while (flag_Connection) { // 서버와 연결된 이후, 서버로부터 0.2초마다 메시지를 받는 스레드. 로그아웃 하여 서비스가 destroy 될 때까지 계속 실행된다.
                try {
                    receive_data = dis.readUTF();
                    Log.d("kb_서버로부터 받는 raw데이터", receive_data);
                    Intent intent = new Intent("MY_ACTIVITY"); // 특정 액티비티에 등록된 브로드캐스트 리시버 명으로 서버로부터 수신된 메시지를 전달하기 위함
                    intent.putExtra("service_msg", receive_data);
                    sendBroadcast(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 여기부턴 클라이언트 스레드를 통해 서버에 요청을 보내는 메소드들
         */

        public void request_Logon(String id, String pic_url) {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_LOGON);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(id);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(pic_url);
                send(ct_buffer.toString());
            } catch (IOException e) {
                Log.e("logon_IOException", String.valueOf(e));
            }
        }

        public void request_refresh() {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_REFRESH_ROOM);
                ct_buffer.append(SEPARATOR);
                send(ct_buffer.toString());
            } catch (IOException e) {
                Log.e("refresh_IOException", String.valueOf(e));
            }
        }

        public void request_Logout() {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_LOGOUT);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_id);
                send(ct_buffer.toString());
            } catch (IOException e) {
                Log.e("logout_IOException", String.valueOf(e));
            }
        }

        public void request_Enter_Room(int roomNumber, String password) {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_ENTERROOM);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_id);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(roomNumber);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(password);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_pic_url);
                send(ct_buffer.toString());
            } catch (IOException e) {
                Log.e("enter_room_IOException", String.valueOf(e));
            }
        }

        public void request_Quit_Room() {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_QUITROOM);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_id);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(ct_roomNumber);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_pic_url);
                send(ct_buffer.toString());
            } catch (IOException e) {
                Log.e("quit_room_IOException", String.valueOf(e));
            }
        }

        public void request_Create_Room(String roomName, int roomMaxUser, int isLock, String password) {
            try{
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_CREATEROOM);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_id); // 이건 채팅방의 방장이 누구인가 구분하기 위한 아이디 값.
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(roomName);
                ct_buffer.append(DELIMETER);
                ct_buffer.append(roomMaxUser);
                ct_buffer.append(DELIMETER);
                ct_buffer.append(isLock);
                ct_buffer.append(DELIMETER);
                ct_buffer.append(password);
                send(ct_buffer.toString());
            }catch (IOException e){
                Log.e("create_room_IOException", String.valueOf(e));
            }
        }

        public void request_Sending_msg(String msg) {
            try {
                ct_buffer.setLength(0);
                ct_buffer.append(REQ_SENDWORD);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_id);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(user_pic_url);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(ct_roomNumber);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(msg);
                send(ct_buffer.toString());
            }catch(IOException e){
                Log.e("Sending_msg_Exception", String.valueOf(e));
            }
        }


        private void send(String sendData) throws IOException {
            dos.writeUTF(sendData);
            dos.flush();
        }

    }


}
