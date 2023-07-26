package com.viewsonic.service1;

import static com.viewsonic.service1.MainActivity.myapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.clt.helper.OSEvent;
import com.clt.helper.SDKSourceItem;
import com.talents.helper.AtopHelper;


public class VcontrolService extends Service {

    MyBroadcast myBroadcast;
    ExecutorService exec;
    TCPServer tcpServer;
    public static final String CHANNEL_ID_STRING = "service_01";
    private Notification notification;
    static AtopHelper helper;
   // MyAPP myapp;
    public VcontrolService() {
        //if 初始化


    }



    private NotificationManager getNotificationManager() {
        return (NotificationManager) VcontrolService.this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onCreate() {
       // Log.i("Eric","!!!　Create");
        super.onCreate();
        myapp=new MyAPP();

        NotificationManager notificationManager = (NotificationManager)VcontrolService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(1, notification);
        }

        OSEvent e=new OSEvent(getApplicationContext()) {
            @Override
            public void sourceSignalChanged(SDKSourceItem sdkSourceItem, boolean b) {
                Log.i("Eric","2023.04.12:"+sdkSourceItem.label+":"+b);
            }

            @Override
            public void volumeChanged(int i) {
                Log.i("Eric","2023.04.12:"+i);
            }

            @Override
            public void muteChanged(boolean b) {
                if(b)
                Log.i("Eric","2023.04.12: muted");
                 else
                    Log.i("Eric","2023.04.12: not muted");

            }

            @Override
            public void pictureModeChanged(int i) {

            }

            @Override
            public void contrastChanged(int i) {

            }

            @Override
            public void colorTempChanged(int i) {

            }

            @Override
            public void sharpChanged(int i) {

            }

            @Override
            public void fakePowerOffModeChanged(boolean b) {

            }

            @Override
            public void brightnessChanged(int i) {

                Log.i("Eric","2023.04.12: brightnessChange to:"+i);
            }
        };


    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1, notification);
        }

        myBroadcast = new MyBroadcast();
        exec=Executors.newCachedThreadPool();
        helper= AtopHelper.getInstance(this);
        int port = 5000;
        tcpServer = new TCPServer(port, this);
        exec.execute(tcpServer);

        Log.i("VcontrolService","!!!　Start Command  VersionB");
        IntentFilter intentFilter = new IntentFilter(TCPServer.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
    //    Log.i("Eric","!!!onDestroy() 1");
        super.onDestroy();
        tcpServer.closeServer();
        unregisterReceiver(myBroadcast);
        Log.i("VcontrolService","!!!onDestroy() ");
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            assert mAction != null;
            /**接收來自UDP回傳之訊息*/
            switch (mAction) {

                case TCPServer.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(TCPServer.RECEIVE_STRING);
                    Log.i("VcontrolService", "!!!! 2023.03.30 收到訊息3: "+msg);
                    byte[] bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES);
                    String msg2=CheckCommad(msg);
                //                                                                      stringBuffer.append("收到： ").append(msg2).append("\n");
                //    edReceiveMessage.setText(stringBuffer);
                    break;



            }
        }
    }


    private String CheckCommad(String msg)
    {
        StringBuffer b;
        Log.i("VcontrolService", "!!!! 2023.03.30 into checkCommad: "+msg);
        if(msg.equals("38303173223030340D")) //HDMI1
        {             // 38303173223030340D
            //  helper.setAtopListener();
         //     helper.changeSource(9);
            //helper.setKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);

         //  Log.i("Eric,","helper.getBrightness():");
         //   Log.i("Eric,","helper.getVolume():"+helper.getVolume());
         //   Log.i("Eric,","helper.getCurrentSource():"+helper.getCurrentSource());
         //   Log.i("Eric,","helper.helper.getMute():"+helper.getMute());


            b=new StringBuffer("38303173223030340D:HDMI1");
            return b.toString();
        }
        else if(msg.equals("38303173223031340D")) //HDMI2
        {

            // helper.setKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
          // helper.changeSource(10);
            // helper.setMute(true);
            b=new StringBuffer("38303173223030340D:HDMI2");
            return b.toString();
        }
        else if(msg.equals("38303173223030410D")) //HOME
        {
            //  helper.setAtopListener();
            // helper.chbangeSource(9);
          // helper.changeSource(13);
            //helper.setKeyEvent(KeyEvent.KEYCODE_HOME);

            // helper.setBrightness(helper.getBrightness()+1);


            b=new StringBuffer("38303173223030410D:Home");
            return b.toString();
        }
        else if(msg.equals("38303173213030300D")) //poweroff
        {
         //   helper.powerOff();
            b=new StringBuffer("38303173213030300D:Power OFF");
            return b.toString();
        }
        else if(msg.equals("38303173213030310D")) //power on
        {
          //  Log.i("Eric,","IP:"+helper.getEthernetIP());
            //helper.reboot();
            b=new StringBuffer("38303173213030310D:Power ON");
            return b.toString();
        }
        else
            return  msg;
    }


}