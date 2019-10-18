package com.example.chisu.myapplication.Chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

import com.example.chisu.myapplication.TabActivity;

import java.util.List;

public class MyService extends Service {
    //알림 매니저.
    NotificationManager Notifi_M;
    //서비스스레드 객체 생성
    ServiceThread thread;
    //알림 객체.
    Notification Notifi ;

    List<MessageItem> messageItemList;

    public static final int MSG_REGISTER_CLIENT = 1;
    //public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;

    private Messenger mClient = null;   // Activity 에서 가져온 Messenger


    //서비스가 최초 '생성'되었을 때 한 번만 실행된다.
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //백그라운드에서 실행되는 동작들이 들어가는 곳.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //알림 매니저를 초기화한다.
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //아래 클래스의 객체 생성.
        myServiceHandler handler = new myServiceHandler();

        //스레드 객체 생성 및 스타트. 핸들러 연결.
        thread = new ServiceThread(getApplicationContext(), messageItemList, handler);
        thread.start();

        //start_sticky : 서비스가 강제종료되었을 경우 다시 서비스를 재시작시켜 주지만
        //intent값을 null로 초기화시켜 재시작하는 것.
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    //핸들러를 상속한 클래스. 이 클래스는 스레드에서 사용된다.
    class myServiceHandler extends Handler {

        //아래 함수는 sendmessage 함수에 의해 전달되는 msg를 받아서 처리하는 함수이다.
        //즉 sendMessage가 올 때마다 일어난다고 볼 수 있다.
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(MyService.this, TabActivity.class);
            //pendingIntent : 인텐트를 생성한 후 해당 인텐트를 바로 사용하는 대신
            //다른 컴포넌트가 이 인텐트를 사용하도록 할 때 쓰는 클래스.
            //여기서는 현재상태를 지속적으로 업데이트하도록 설정했다.
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            //알림 객체 여기서 초기화.
//            Notifi = new Notification.Builder(getApplicationContext())
//                    .setContentTitle("Content Title")
//                    .setContentText("Content Text")
//                    .setSmallIcon(R.drawable.ok_btn)
//                    .setTicker("알림!!!")
//                    .setContentIntent(pendingIntent)
//                    .build();
//
//            //소리추가
//            Notifi.defaults = Notification.DEFAULT_SOUND;
//
//            //알림 소리를 한번만 내도록
//            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//
//            //확인하면 자동으로 알림이 제거 되도록
//            Notifi.flags = Notification.FLAG_AUTO_CANCEL;
//
//            Notifi_M.notify( 777 , Notifi);

            //토스트 띄우기
//            Toast.makeText(MyService.this, "뜸?", Toast.LENGTH_LONG).show();
        }
    }
}
//서비스 적용 전 코드를 주석을 통한 저장
//package com.example.jisu7.artstreet;
//
//        import android.app.Notification;
//        import android.app.NotificationManager;
//        import android.app.PendingIntent;
//        import android.app.Service;
//        import android.content.Context;
//        import android.content.Intent;
//        import android.os.Handler;
//        import android.os.IBinder;
//
//public class MyService extends Service {
//    //알림 매니저.
//    NotificationManager Notifi_M;
//    //서비스스레드 객체 생성
//    ServiceThread thread;
//    //알림 객체.
//    Notification Notifi ;
//
//    //서비스가 최초 '생성'되었을 때 한 번만 실행된다.
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    //백그라운드에서 실행되는 동작들이 들어가는 곳.
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //알림 매니저를 초기화한다.
//        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        //아래 클래스의 객체 생성.
//        myServiceHandler handler = new myServiceHandler();
//
//        //스레드 객체 생성 및 스타트. 핸들러 연결.
//        thread = new ServiceThread(handler);
//        thread.start();
//
//        //start_sticky : 서비스가 강제종료되었을 경우 다시 서비스를 재시작시켜 주지만
//        //intent값을 null로 초기화시켜 재시작하는 것.
//        return START_STICKY;
//    }
//
//    //서비스가 종료될 때 할 작업
//    public void onDestroy() {
//        thread.stopForever();
//        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
//    }
//
//    //핸들러를 상속한 클래스.
//    class myServiceHandler extends Handler {
//
//        //아래 함수는 sendmessage 함수에 의해 전달되는 msg를 받아서 처리하는 함수이다.
//        @Override
//        public void handleMessage(android.os.Message msg) {
//            Intent intent = new Intent(MyService.this, TabActivity.class);
//            //pendingIntent : 인텐트를 생성한 후 해당 인텐트를 바로 사용하는 대신
//            //다른 컴포넌트가 이 인텐트를 사용하도록 할 때 쓰는 클래스.
//            //여기서는 현재상태를 지속적으로 업데이트하도록 설정했다.
//            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//            //알림 객체 여기서 초기화.
//            Notifi = new Notification.Builder(getApplicationContext())
//                    .setContentTitle("Content Title")
//                    .setContentText("Content Text")
//                    .setSmallIcon(R.drawable.ok_btn)
//                    .setTicker("알림!!!")
//                    .setContentIntent(pendingIntent)
//                    .build();
//
//            //소리추가
//            Notifi.defaults = Notification.DEFAULT_SOUND;
//
//            //알림 소리를 한번만 내도록
//            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//
//            //확인하면 자동으로 알림이 제거 되도록
//            Notifi.flags = Notification.FLAG_AUTO_CANCEL;
//
//            Notifi_M.notify( 777 , Notifi);
//
//            //토스트 띄우기
////            Toast.makeText(MyService.this, "뜸?", Toast.LENGTH_LONG).show();
//        }
//    }
//}



