package com.example.chisu.myapplication.Chat;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

//소켓, httpconnection 등 인터넷 연결에 대한 것은 쓰레드로 구현해야 한다. 안 그러면 앱 터진다.
public class ServiceThread extends Thread {

    //핸들러
    Handler handler;

    //계속 돌고 있는지 확인하는 변수.
    //이 변수를 통해 while문을 핸들링할 수 있다.
    boolean isRun = true;

    Context context;

    //클라이언트 소켓
    private Socket ct_sock;

    //서버에서 날아오는 데이터를 읽을 인풋스트림
    private DataInputStream ct_in;

    //서버에 데이터를 날릴 아웃풋스트림
    private DataOutputStream ct_out;

    //데이터를 임시저장할 스트링 버퍼
    private StringBuffer ct_buffer;

    //스레드
    private Thread thisThread;

    //현재 아이디
    private String ct_logonID;

    //현재 방 번호
//    private int ct_roomNumber;
    //발표를 위해 임시적으로 스태틱으로 설정
    public int ct_roomNumber;

    //사진 url
    private String pic_url = null;

    //받는 데이터의 내용
    public String dataString;

    public List<MessageItem> messageItemList = new ArrayList<>();

    //구분자들
    private static final String SEPARATOR = "|";
    private static final String DELIMETER = "'";
    private static final String DELIMETER2 = "=";

    //서버에서 날아오는 메시지 코드들
    private static final int REQ_LOGON = 1001;
    private static final int REQ_CREATEROOM = 1011;
    private static final int REQ_ENTERROOM = 1021;
    private static final int REQ_QUITROOM = 1031;
    private static final int REQ_LOGOUT = 1041;
    private static final int REQ_SENDWORD = 1051;
    private static final int REQ_SENDWORDTO = 1052;
    private static final int REQ_COERCEOUT = 1053;
    private static final int REQ_SENDFILE = 1061;

    private static final int YES_LOGON = 2001;
    private static final int NO_LOGON = 2002;
    private static final int YES_CREATEROOM = 2011;
    private static final int NO_CREATEROOM = 2012;
    private static final int YES_ENTERROOM = 2021;
    private static final int NO_ENTERROOM = 2022;
    private static final int YES_QUITROOM = 2031;
    private static final int YES_LOGOUT = 2041;
    private static final int YES_SENDWORD = 2051;
    private static final int YES_SENDWORDTO = 2052;
    private static final int NO_SENDWORDTO = 2053;
    private static final int YES_COERCEOUT = 2054;
    private static final int YES_SENDFILE = 2061;
    private static final int NO_SENDFILE = 2062;
    private static final int MDY_WAITUSER = 2003;
    private static final int MDY_WAITINFO = 2013;
    private static final int MDY_ROOMUSER = 2023;
    private static final int ERR_ALREADYUSER = 3001;
    private static final int ERR_SERVERFULL = 3002;
    private static final int ERR_ROOMSFULL = 3011;
    private static final int ERR_ROOMERFULL = 3021;
    private static final int ERR_PASSWORD = 3022;
    private static final int ERR_REJECTION = 3031;
    private static final int ERR_NOUSER = 3032;

    //스레드의 생성자
    public ServiceThread(Context context1, List<MessageItem> list, Handler handler1) {
        Log.e("클라이언트 스레드 생성자", "ok");
        context = context1;
        messageItemList = list;
        handler = handler1;


//        new Thread(){
//            @Override
//            public void run() {

//                try {
//                    //클라이언트 소켓 생성 및 초기화
//                    ct_sock = new Socket("13.124.241.71", 5555);
//                    ct_in = new DataInputStream(ct_sock.getInputStream());
//                    ct_out = new DataOutputStream(ct_sock.getOutputStream());
//                    ct_buffer = new StringBuffer(4096); //4kb
//                    //스레드 연결.. 채팅룸에서 하는건데.
//                    thisThread = this;
////                    handler = new Handler();
//
//                }catch (Exception e){
//                    Log.e("ClientThread 생성자 에러~", e.getMessage());
//                }
//            }
//        }.start();

    }

    //생성자. 여기서 양 쪽의 핸들러를 연결한다.
    public ServiceThread(Handler handler){
        this.handler = handler;
    }

    //스레드를 멈추는 메소드. 플래그를 바꿔준다.
    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    //방을 만드는 요청. 보내기까지.
    public void requestCreateRoom(String roomName, int roomMaxUser,
                                  int isRock, String password) {
        //isRock은 잠김 여부에 대한 인자. 0이 false - 즉 안 잠긴 것, 1은 true- 잠긴 것.
        try {
            ct_buffer.setLength(0);
            ct_buffer.append(REQ_CREATEROOM);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(ct_logonID);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(roomName);
            ct_buffer.append(DELIMETER);
            ct_buffer.append(roomMaxUser);
            ct_buffer.append(DELIMETER);
            ct_buffer.append(isRock);
            ct_buffer.append(DELIMETER);
            ct_buffer.append(password);
            send(ct_buffer.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void requestSendWord(String data) {
        try {
            ct_buffer.setLength(0);
            ct_buffer.append(REQ_SENDWORD);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(ct_logonID);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(pic_url);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(ct_roomNumber);
            ct_buffer.append(SEPARATOR);
            ct_buffer.append(data);
            send(ct_buffer.toString());

            Log.e("requestSendWord", "완료");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void send(String sendData) throws IOException { // 서버에 메시지 보내는 메소드
        ct_out.writeUTF(sendData);
        ct_out.flush();
    }

    public void release() { // 서버와의 연결이 끊길 때 클라이언트의 모든 스레드와 스트림들을 종료하는 메소드
        if (thisThread != null) {
            thisThread = null;
        }
        try {
            if (ct_out != null) {
                ct_out.close();
            }
        } catch (IOException e) {
        } finally {
            ct_out = null;
        }
        try {
            if (ct_in != null) {
                ct_in.close();
            }
        } catch (IOException e) {
        } finally {
            ct_in = null;
        }
        try {
            if (ct_sock != null) {
                ct_sock.close();
            }
        } catch (IOException e) {
        } finally {
            ct_sock = null;
        }
        System.exit(0);
        Log.e("소켓 연결을 종료합니다.", "");
    }

    //안드로이드는 메인스레드와 서브스레드 간 핸들러를 통해 메시지를 전달해 메시지 큐에 저장하는 방식의 통신을 사용한다.

    public void run(){
        //반복적으로 수행할 작업을 한다. 즉 반복적으로 핸들러에 메시지를 보낸다. 그러면 핸들러는 메시지를 받을 때마다 알림을 내보내는 것이다.
        while(isRun){
            handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄. 즉 알림 실행. 여기서 보낸 메시지는 myService의 handleMessage에서 처리함.

            try {
                //클라이언트 소켓 생성 및 초기화
                ct_sock = new Socket("13.124.241.71", 5555);
                ct_in = new DataInputStream(ct_sock.getInputStream());
                ct_out = new DataOutputStream(ct_sock.getOutputStream());
                ct_buffer = new StringBuffer(4096); //4kb
                //스레드 연결.. 채팅룸에서 하는건데.
                thisThread = this;
                requestCreateRoom("a", 2, 0, "0");

            } catch (Exception e){
                Log.e("ClientThread 생성자 에러~", e.getMessage());
            }

            Log.e("run 메소드 실행", "ok");

            try {
                Thread currThread = thisThread;
                Log.e("try문 진입", "ok");

                while (currThread == thisThread) {
                    Log.e("while문 진입." , "ok");

                    //계속해서 데이터를 받아서 처리해주는 과정을 반복한다.
                    String recvData = ct_in.readUTF();
                    Log.e("넘겨받은 데이터 묶음", recvData);
                    StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
                    int command = Integer.parseInt(st.nextToken());

                    Log.e("command", String.valueOf(command));

                    //처리해주는 부분. 서버에서 넘어온 커맨드에 따라 반응이 달라진다.
                    switch (command) {
//                    case YES_LOGON: {
//
//                        ct_roomNumber = 0;
//                        try {
//                            // ' 를 기준으로 스트링을 '다시' 나눈다.
//                            StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
//
//                            //룸 정보를 담는 벡터 객체를 만든다.
//                            Vector roomInfo = new Vector();
//
//                            while (st1.hasMoreTokens()) {
//                                //토큰의 아이디를 임시 변수에 담아준다.
//                                String temp = st1.nextToken();
//
//                                //temp가 empty라는 단어가 아니라면 벡터에 temp를 넣어준다.
//                                if (!temp.equals("empty")) {
//                                    roomInfo.addElement(temp);
//                                }
//                            }
//                            ct_waitRoom.roomInfo.setListData(roomInfo);
//                            ct_waitRoom.message.requestFocusInWindow();
//                        } catch (NoSuchElementException e) {
//                            ct_waitRoom.message.requestFocusInWindow();
//                        }
//                        break;
//                    }
//                    case NO_LOGON: {
//                        String id;
//                        int errCode = Integer.parseInt(st.nextToken());
//                        if (errCode == ERR_ALREADYUSER) {
//
//                            JOptionPane.showMessageDialog(ct_waitRoom, "이미 다른 사용자가 있습니다.",
//                                    "로그온", JOptionPane.ERROR_MESSAGE);
//                            id = ChatClient.getLogonID();
//                            requestLogon(id);
//                        } else if (errCode == ERR_SERVERFULL) {
//                            logonbox.dispose();
//                            JOptionPane.showMessageDialog(ct_waitRoom, "대화방이 만원입니다.",
//                                    "로그온", JOptionPane.ERROR_MESSAGE);
//                            id = ChatClient.getLogonID();
//                            requestLogon(id);
//                        }
//                        break;
//                    }
//                    case MDY_WAITUSER: {
//                        StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
//                        Vector user = new Vector();
//                        while (st1.hasMoreTokens()) {
//                            user.addElement(st1.nextToken());
//                        }
//                        ct_waitRoom.waiterInfo.setListData(user);
//                        ct_waitRoom.message.requestFocusInWindow();
//                        break;
//                    }

                        //서버에서 YES_CREATEROOM이 날아온 경우
                        case YES_CREATEROOM: {
                            //일단 이걸 받아야 함 ㅇㅇ.
                            ct_roomNumber = Integer.parseInt(st.nextToken());
                            Log.e("채팅방 생성 완료", " ");
//                        ct_waitRoom.hide();
//                        if (ct_chatRoom == null) {
//                            ct_chatRoom = new ChatRoomDisplay(this);
//                            ct_chatRoom.isAdmin = true;
//                        } else {
//                            ct_chatRoom.show();
//                            ct_chatRoom.isAdmin = true;
//                            ct_chatRoom.resetComponents();
//                        }
                            break;
                        }
//                    case NO_CREATEROOM: {
//                        int errCode = Integer.parseInt(st.nextToken());
//                        if (errCode == ERR_ROOMSFULL) {
//                            msgBox = new MessageBox(ct_waitRoom, "대화방개설",
//                                    "더 이상 대화방을 개설 할 수 없습니다.");
//                            msgBox.show();
//                        }
//                        break;
//                    }
//                    case MDY_WAITINFO: {
//                        StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
//                        StringTokenizer st2 = new StringTokenizer(st.nextToken(), DELIMETER);
//
//                        Vector rooms = new Vector();
//                        Vector users = new Vector();
//                        while (st1.hasMoreTokens()) {
//                            String temp = st1.nextToken();
//                            if (!temp.equals("empty")) {
//                                rooms.addElement(temp);
//                            }
//                        }
//                        ct_waitRoom.roomInfo.setListData(rooms);
//
//                        while (st2.hasMoreTokens()) {
//                            users.addElement(st2.nextToken());
//                        }
//
//                        ct_waitRoom.waiterInfo.setListData(users);
//                        ct_waitRoom.message.requestFocusInWindow();
//
//                        break;
//                    }
                        case YES_ENTERROOM: {
                            ct_roomNumber = Integer.parseInt(st.nextToken());
                            String id = st.nextToken();
//                        ct_waitRoom.hide();
//                        if (ct_chatRoom == null) {
//                            ct_chatRoom = new ChatRoomDisplay(this);
//                        } else {
//                            ct_chatRoom.show();
//                            ct_chatRoom.resetComponents();
//                        }
                            break;
                        }
//                    case NO_ENTERROOM: {
//                        int errCode = Integer.parseInt(st.nextToken());
//                        if (errCode == ERR_ROOMERFULL) {
//                            msgBox = new MessageBox(ct_waitRoom, "대화방입장",
//                                    "대화방이 만원입니다.");
//                            msgBox.show();
//                        } else if (errCode == ERR_PASSWORD) {
//                            msgBox = new MessageBox(ct_waitRoom, "대화방입장",
//                                    "비밀번호가 틀립니다.");
//                            msgBox.show();
//                        }
//                        break;
//                    }

                        //채팅방에 유저가 왔다갔다 할 때마다 발생
                        case MDY_ROOMUSER: {
                            String id = st.nextToken();
                            int code = Integer.parseInt(st.nextToken());

                            StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
                            Vector user = new Vector();
                            while (st1.hasMoreTokens()) {
                                user.addElement(st1.nextToken());
                            }
//                        ct_chatRoom.roomerInfo.setListData(user);
//                        if (code == 1) {
//                            ct_chatRoom.messages.append("###" + id + "님이 입장하셨습니다. ###\n");
//                        } else if (code == 2) {
//                            ct_chatRoom.messages.append("###" + id + "님이 강제퇴장 되었습니다. ###\n");
//                        } else { // code 0
//                            ct_chatRoom.messages.append("###" + id + "님이 퇴장하셨습니다. ###\n");
//                        }
//                        ct_chatRoom.message.requestFocusInWindow();
                            break;
                        }
                        case YES_QUITROOM: {
//                        String id = st.nextToken();
//                        if (ct_chatRoom.isAdmin) ct_chatRoom.isAdmin = false;
//                        ct_chatRoom.hide();
//                        ct_waitRoom.show();
//                        ct_waitRoom.resetComponents();
//                        ct_roomNumber = 0;
                            break;
                        }
                        case YES_LOGOUT: {
//                        ct_waitRoom.dispose();
//                        if (ct_chatRoom != null) {
//                            ct_chatRoom.dispose();
//                        }
//                        release();
                            break;
                        }
                        case YES_SENDWORD: { // 2051 | 메시지 보낸 유저 아이디 | 유저사진url(안드로이드 한정, 여기서는 null값으로 대체) | 방번호 | 현재시각(스윙에선 필요없음) | 내용
                            Log.e("Yes_SENDWORD ", "받음");
                            //데이터가 왔을 때 반응을 할 수 있어야 해ㅣ
                            //유저아이디 받기
                            String id = st.nextToken();
                            //유저 사진 받기
                            String url_pic = st.nextToken();
                            //룸번호 받기
                            int roomNumber = Integer.parseInt(st.nextToken());
                            //시간 받기
                            String time = st.nextToken();
                            try {
                                //데이터 스트링은 예스 센드워드 이후.
                                //데이터 받기. 이 정보를 리스트뷰에 띄워야 한다....
//                            String data = st.nextToken();
                                dataString = st.nextToken();

//                            MessageItem messageItem = new MessageItem("다른 사용자", dataString, true);
//                            messageItemList.add(messageItem);
//                            ChattingRoom.adapter.notifyDataSetChanged();

                                Log.e("dataString ", dataString);
                                // 방번호(0은 대기실....사실 안드로이드에선 의미가 없는 메시지임)
//                           if (roomNumber == 0) {
//                                if (id.equals(ct_logonID)) {
//
//                                }
//                            } else {
//                                if (id.equals(ct_logonID)) {
//
//                                }
//                            }

                            } catch (NoSuchElementException e) {
                                Log.e("sendword 중 에러 발생 ", e.getMessage());

                            } catch (Exception e){
                                Log.e("sendword 중 에러 발생 ", e.getMessage());

                            }
                            break;
                        }
//                    case YES_SENDWORD: { // 2051 | 메시지 보낸 유저 아이디 | 유저사진url(안드로이드 한정, 여기서는 null값으로 대체) | 방번호 | 현재시각(스윙에선 필요없음) | 내용
//                        //유저아이디 받기
//                        String id = st.nextToken();
//                        //유저 사진 받기
//                        String url_pic = st.nextToken();
//                        //룸번호 받기
//                        int roomNumber = Integer.parseInt(st.nextToken());
//                        //시간 받기
//                        String time = st.nextToken();
//                        try {
//                            //데이터 받기. 이 정보를 리스트뷰에 띄워야 한다....
//                            String data = st.nextToken();
//                            // 방번호(0은 대기실....사실 안드로이드에선 의미가 없는 메시지임)
//                            if (roomNumber == 0) {
////                                ct_waitRoom.messages.append(id + " : " + data + "\n");
//                                if (id.equals(ct_logonID)) {
////                                    ct_waitRoom.message.setText("");
////                                    ct_waitRoom.message.requestFocusInWindow();
//                                }
////                                ct_waitRoom.message.requestFocusInWindow();
//                            } else {
////                                ct_chatRoom.messages.append(id + " : " + data + "\n");
//                                if (id.equals(ct_logonID)) {
//
////                                    ct_chatRoom.message.setText("");
//                                }
////                                ct_chatRoom.message.requestFocusInWindow();
//                            }
//
//                        } catch (NoSuchElementException e) {
////                            if (roomNumber == 0) ct_waitRoom.message.requestFocusInWindow();
////                            else ct_chatRoom.message.requestFocusInWindow();
//                        }
//                        break;
//                    }
//                    case YES_SENDWORDTO: {
//                        String id = st.nextToken();
//                        String idTo = st.nextToken();
//                        int roomNumber = Integer.parseInt(st.nextToken());
//                        try {
//                            String data = st.nextToken();
//                            if (roomNumber == 0) {
//                                if (id.equals(ct_logonID)) {
//                                    ct_waitRoom.message.setText("");
//                                    ct_waitRoom.messages.append("귓속말<to:" + idTo + "> : " + data + "\n");
//                                } else {
//                                    ct_waitRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
//                                }
//                                ct_waitRoom.message.requestFocusInWindow();
//                            } else {
//
//                                if (id.equals(ct_logonID)) {
//                                    ct_chatRoom.message.setText("");
//                                    ct_chatRoom.messages.append("귓속말<to:" + idTo + "> : " + data + "\n");
//                                } else {
//                                    ct_chatRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
//                                }
//                                ct_chatRoom.message.requestFocusInWindow();
//                            }
//                        } catch (NoSuchElementException e) {
//                            if (roomNumber == 0) ct_waitRoom.message.requestFocusInWindow();
//                            else ct_chatRoom.message.requestFocusInWindow();
//                        }
//                        break;
//                    }
//                    case NO_SENDWORDTO: {
//                        String id = st.nextToken();
//                        int roomNumber = Integer.parseInt(st.nextToken());
//                        String message = "";
//                        if (roomNumber == 0) {
//                            message = "대기실에 " + id + "님이 존재하지 않습니다.";
//                            JOptionPane.showMessageDialog(ct_waitRoom, message,
//                                    "귓속말 에러", JOptionPane.ERROR_MESSAGE);
//                        } else {
//                            message = "이 대화방에 " + id + "님이 존재하지 않습니다.";
//                            JOptionPane.showMessageDialog(ct_chatRoom, message,
//                                    "귓속말 에러", JOptionPane.ERROR_MESSAGE);
//                        }
//                        break;
//                    }
//                    case REQ_SENDFILE: {
//                        String id = st.nextToken();
//                        int roomNumber = Integer.parseInt(st.nextToken());
//                        String message = id + "로 부터 파일전송을 수락하시겠습니까?";
//                        int value = JOptionPane.showConfirmDialog(ct_chatRoom, message,
//                                "파일수신", JOptionPane.YES_NO_OPTION);
//                        if (value == 1) {
//                            try {
//                                ct_buffer.setLength(0);
//                                ct_buffer.append(NO_SENDFILE);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(ct_logonID);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(roomNumber);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(id);
//                                send(ct_buffer.toString());
//                            } catch (IOException e) {
//                                System.out.println(e);
//                            }
//                        } else {
//                            StringTokenizer addr = new StringTokenizer(InetAddress.getLocalHost().toString(), "/");
//                            String hostname = "";
//                            String hostaddr = "";
//
//                            hostname = addr.nextToken();
//                            try {
//                                hostaddr = addr.nextToken();
//                            } catch (NoSuchElementException err) {
//                                hostaddr = hostname;
//                            }
//
//                            try {
//                                ct_buffer.setLength(0);
//                                ct_buffer.append(YES_SENDFILE);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(ct_logonID);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(roomNumber);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(id);
//                                ct_buffer.append(SEPARATOR);
//                                ct_buffer.append(hostaddr);
//                                send(ct_buffer.toString());
//                            } catch (IOException e) {
//                                System.out.println(e);
//                            }
//                            // 파일 수신 서버실행.
//                            new RecieveFile();
//                        }
//                        break;
//                    }
//                    case NO_SENDFILE: {
//                        int code = Integer.parseInt(st.nextToken());
//                        String id = st.nextToken();
//                        fileTransBox.dispose();
//
//                        if (code == ERR_REJECTION) {
//                            String message = id + "님이 파일수신을 거부하였습니다.";
//                            JOptionPane.showMessageDialog(ct_chatRoom, message,
//                                    "파일전송", JOptionPane.ERROR_MESSAGE);
//                            break;
//                        } else if (code == ERR_NOUSER) {
//                            String message = id + "님은 이 방에 존재하지 않습니다.";
//                            JOptionPane.showMessageDialog(ct_chatRoom, message,
//                                    "파일전송", JOptionPane.ERROR_MESSAGE);
//                            break;
//                        }
//                    }
//                    case YES_SENDFILE: {
//                        String id = st.nextToken();
//                        String addr = st.nextToken();
//
//                        fileTransBox.dispose();
//                        // 파일 송신 클라이언트 실행.
//                        new SendFile(addr);
//                        break;
//                    }
//                    case YES_COERCEOUT: {
//                        ct_chatRoom.hide();
//                        ct_waitRoom.show();
//                        ct_waitRoom.resetComponents();
//                        ct_roomNumber = 0;
//                        ct_waitRoom.messages.append("### 방장에 의해 강제퇴장 되었습니다. ###\n");
//                        break;
//                    }
                    }
                    Log.e("스위치문 벗어남", "ok");
                    //0.2초마다 갱신
                    Thread.sleep(200);
                }
                Log.e("while 벗어남", " ");
            } catch (Exception e){
                String err = (e.getMessage()==null)?"SD Card failed":e.getMessage();
                Log.e("트라이 문 에러 발생:",err);
//            Log.e("에러 발생 : ", e.getMessage());
                release();
            }

            try{
//                Thread.sleep(10000); //10초씩 쉰다.
            }catch (Exception e) {}
        }
    }
}
//서비스 적용 전 주석을 통한 코드 저장.
//package com.example.jisu7.artstreet;
//
//        import android.os.Handler;
//
//public class ServiceThread extends Thread{
//    //소켓, httpconnection 등 인터넷 연결에 대한 것은 쓰레드로 구현해야 한다. 안 그러면 앱 터진다.
//
//    //핸들러
//    Handler handler;
//
//    //계속 돌고 있는지 확인하는 변수.
//    //이 변수를 통해 while문을 핸들링할 수 있다.
//    boolean isRun = true;
//
//    //생성자. 여기서 양 쪽의 핸들러를 연결한다.
//    public ServiceThread(Handler handler){
//        this.handler = handler;
//    }
//
//    //스레드를 멈추는 메소드. 플래그를 바꿔준다.
//    public void stopForever(){
//        synchronized (this) {
//            this.isRun = false;
//        }
//    }
//
//    //안드로이드는 메인스레드와 서브스레드 간 핸들러를 통해 메시지를 전달해 메시지 큐에 저장하는 방식의 통신을 사용한다.
//
//    public void run(){
//        //반복적으로 수행할 작업을 한다. 즉 반복적으로 핸들러에 메시지를 보낸다. 그러면 핸들러는 메시지를 받을 때마다 알림을 내보내는 것이다.
//        while(isRun){
//            handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄. 여기서 보낸 메시지는 myService의 handleMessage에서 처리함.
//            try{
//                Thread.sleep(10000); //10초씩 쉰다.
//            }catch (Exception e) {}
//        }
//    }
//}







