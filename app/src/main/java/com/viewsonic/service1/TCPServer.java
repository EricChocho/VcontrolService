package com.viewsonic.service1;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_MENU;
import static android.view.KeyEvent.KEYCODE_POWER;
import static com.viewsonic.service1.MainActivity.myapp;
import static com.viewsonic.service1.VcontrolService.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.ada.pmanager.AdaPManager;
import com.clt.helper.OSEvent;
import com.clt.helper.SDKAudioHelper;
import com.clt.helper.SDKPictureHelper;
import com.clt.helper.SDKSourceHelper;
import com.clt.helper.SDKSourceItem;
import com.clt.helper.SDKSystemHelper;
import com.clt.helper.SDKSystemInfoHelper;
import com.talents.helper.AtopHelper;

    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.io.PrintWriter;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.util.ArrayList;

public class TCPServer implements Runnable{


    public static final String TAG = "VcontrolService";
    public static final String RECEIVE_ACTION = "GetTCPReceive";
    public static final String RECEIVE_STRING = "ReceiveString";
    public static final String RECEIVE_BYTES = "ReceiveBytes";
    public static final String Return_OK="3430312B0D";
    public static final String Return_FALSE="3430312D0D";
    int SourceIndex=0;

    private int port;
    private boolean isOpen;
    private Context context;
    public ArrayList<ServerSocketThread> SST = new ArrayList<>();

     private  int logcount;



    /**建立建構子*/
    public  TCPServer(int port,Context context){
        this.port = port;
        isOpen = true;
        this.context = context;
        logcount=0;
    }
    //取得開啟狀態
    public boolean getStatus(){
        return isOpen;
    }
    //關閉伺服器
    public void closeServer(){
        isOpen = false;
        //找出所有正在連線的裝置執行緒，並一一清除、斷線
        for (ServerSocketThread s : SST){
            s.isRun = false;
        }
        SST.clear();
    }

    /**取得Socket許可(握手)*/
    private Socket getSocket(ServerSocket serverSocket){
        try {
                                                                                                                                                                                                                                                                                                                                                    return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            if(logcount>=15)
            Log.e(TAG, "Update Server Status");
            return null;
        }
    }

    @Override
    public void run() {

        try {
            /**在本機的Port上開啟伺服器*/
            ServerSocket serverSocket = new ServerSocket(port);
            //設置Timeout，以便更新裝置連進來的狀況
            serverSocket.setSoTimeout(2000);
            while (isOpen)  {
                if(logcount>=15)
                Log.e(TAG, "Monitor Devices connect...");

                logcount++;
                if (logcount>15)logcount=0;

                if (!isOpen) break;
                Socket socket = getSocket(serverSocket);
                if (socket != null){
                    //如果Socket不為null，表示有裝置連入了
                    new ServerSocketThread(socket,context);
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**監聽裝置連入與收發狀態之執行緒*/
    public class ServerSocketThread extends Thread{
        private Socket socket;
        private PrintWriter pw;
        private InputStream is;
        private boolean isRun = true;
        private Context context;

        ServerSocketThread(Socket socket, Context context){
            this.socket = socket;
            this.context = context;

            String ip = socket.getInetAddress().toString();
            Log.d(TAG, "!!!! Find New Device,Ip: " + ip);



            try {
                socket.setSoTimeout(2000);
                OutputStream os = socket.getOutputStream();
                is = socket.getInputStream();
                pw = new PrintWriter(os,true);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendData(String msg){
            pw.flush();
            pw.print(msg);
            pw.flush();
        }



        @Override
        public void run() {
            byte[] buff = new byte[100];
            SST.add(this);
            while (isRun && !socket.isClosed() && !socket.isInputShutdown()){
                try {
                    //監聽訊息是否有送過來
                    int rcvLen;
                    if ((rcvLen = is.read(buff)) != -1 ){



                        String string = new String(buff, 0, rcvLen);
                        String string2= str2HexStr(string);
                        //Log.d(TAG, "收到訊息1: " + string);
                     //   Log.d(TAG, "收到訊息2: " + string2);

                        String commandresult=CheckCommad(string2);
                        if(commandresult.equals("true"))
                        {
                            String string5=hexToString(Return_OK);
                            sendData(string5);
                        }
                        else if(commandresult.equals("false"))
                        {

                            String string5=hexToString(Return_FALSE);
                            sendData(string5);
                        }
                        else if(commandresult.equals("source"))
                        {
                            Log.i("Eric","2023.04.12 start return source"+CurrentSourceLable);
                            String string5="";
                            switch (CurrentSourceLable)
                            {
                                case "Android":    string5="383031723030300D"; break;
                                case "HDMI 1":     string5="383031723030310D"; break;
                                case "HDMI 2":     string5="383031723030320D"; break;
                                case "HDMI 3":     string5="383031723030330D"; break;
                                case "HDMI 4":     string5="383031723030340D"; break;
                                case "HDMI 5":     string5="383031723030350D"; break;
                                case "HDBaseT":    string5="383031723030360D"; break;
                                case "USB-C":      string5="383031723030370D"; break;
                            }
                          //  Log.i("Eric","2023.04.12 start return source"+string5);


                            String string6=hexToString(string5);
                            sendData(string6);
                        }
                        else if(commandresult.equals("hour"))
                        {
                          //  OPhReturn=new ArrayList();
                            Log.i("Eric","2023.04.11 start return");
                            if(OPhReturn.size()==4) {
                                String s1=hexToString(OPhReturn.get(0));
                                sendData(s1);
                                String s2=hexToString(OPhReturn.get(1));
                                sendData(s2);
                                String s3=hexToString(OPhReturn.get(2));
                                sendData(s3);
                                String s4=hexToString(OPhReturn.get(3));
                                sendData(s4);



                            }


                        }
                        else
                        {
                            Log.d(TAG, "收send 2: " + commandresult);
                            sendData(hexToString(commandresult));
                        }

                        //   String string3=hexToString(string2);

                        //    Log.d(TAG, "收到訊息3: " + string3);


                        /**收到訊息後，以廣播的方式回傳到Activity*/
                        Intent intent = new Intent();
                        intent.setAction(RECEIVE_ACTION);
                        intent.putExtra(RECEIVE_STRING,string2);
                        intent.putExtra(RECEIVE_BYTES, buff);
                        context.sendBroadcast(intent);

                        //   String string4="3430312B0D";
                        //   String string5=hexToString(string4);
                        //sendData("401+");
                        //  sendData(string5);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //跳出While迴圈即為斷開連線
            try {
                socket.close();
                SST.clear();
                Log.e(TAG, "!!! v關閉Server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //2023.04.11
    ArrayList <String>OPhReturn;



    public String CheckCommad(String msg)
    {
        //StringBuffer b;
       Log.i("Eric","2023.03.20 Verion B"+msg);
        SDKAudioHelper Audiohelper = new SDKAudioHelper(context);
        SDKSourceHelper Sourcehelper=new SDKSourceHelper();
        SDKSystemHelper Systemhelper = new SDKSystemHelper(context);
        SDKPictureHelper Picturehelper = new SDKPictureHelper(context);

        if(msg.equals("38303173213030310D")) //power on
        {
            Log.i("Eric","2023.04.10 no api");

              Systemhelper.sendKeyEvent(KEYCODE_POWER);

            AdaPManager adaPManager;

              helper.go2Sleep();
            //Gen1 //helper.reboot();
            Log.i("Eric","isPowerOff:"+helper.isInFakePowerOffMode());
            return "true";
        }
        else if(msg.equals("38303173213030300D")) //poweroff
        {
            Log.i("Eric","2023.04.10 no api");
            Systemhelper.sendKeyEvent(KEYCODE_POWER);

           //gen1 // helper.powerOff();
          //  Log.i("Eric","isPowerOff:"+helper.isInFakePowerOffMode());
            return "true";
        }
        else if(msg.equals("38303173223030340D")) //HDMI1
        {

            Sourcehelper.changeSource(1);

            //helper.changeSource(9);
            return "true";

        }
        else if(msg.equals("38303173223031340D")) //HDMI2
        {
            Sourcehelper.changeSource(2);

            //helper.changeSource(10);
            return "true";
        }
        else if (msg.equals("38303173223032340D")) //HDMI3
        {
            Sourcehelper.changeSource(3);

            //helper.changeSource(10);
            return "true";
        }
        else if(msg.equals("38303173223033340D")) //HDMI4
        {

            Log.i("Eric","2023.04.12 HDMI4 Enter");
            Sourcehelper.changeSource(4);

            Log.i("Eric","2023.04.12 HDMI4 Enter2");
            //helper.changeSource(10);
            return "true";

        }
        else if(msg.equals("38303173223030370D")) // OPS/SDM/HDBT
        {
            Sourcehelper.changeSource(5);

            //helper.changeSource(10);
            return "true";
        }
        else if(msg.equals("38303173223031390D")) //Type-C (1)
        {
            Sourcehelper.changeSource(7);

            //helper.changeSource(10);
            return "true";
        }
          else if(msg.equals("38303173223030410D")) //HOME
        {


            //Sourcehelper.changeSource(0);  //0 Home 1HDMI1 2 3 4  5 HDMI5 7 type-c

            Sourcehelper.changeSource(0);
           // helper.changeSource(13);

            return "true";
        }
        else if(msg.equals("383031736666666666")) //Test 2
        {
            Log.i("Eric","2023.03.30 current index ="+SourceIndex);
             SourceIndex++;
            //SourceIndex=0;

            Log.i("Eric","2023.03.30 current index ="+SourceIndex);


            Sourcehelper.changeSource(SourceIndex);
           //                                                Sourcehelper.changeSource(8);

            Log.i("Eric","2023.03.30 Verion "+msg);
            Log.i("Eric","2023.03.30 current index ="+SourceIndex);

            Log.i("Eric","2023.03.30 Verion "+msg);
           SDKSourceItem sourceItem= Sourcehelper.getCurrentSource();
           Log.i("Eric","2023.03.30 id:"+sourceItem.id);
            Log.i("Eric","2023.03.30 id:"+sourceItem.label);
            Log.i("Eric","2023.03.30 id:"+sourceItem.port);
            Log.i("Eric","2023.03.30 id:"+Sourcehelper.getAvailableSourceModelList().size());

            //Sourcehelper.changeSource(7);


            return "true";
        }
        else if(msg.equals("383031732230305A0D")) //Source Cycle
        {
            Log.i("Eric","2023.03.30 Verion "+msg);
            Log.i("Eric","2023.03.30 current index ="+SourceIndex);
            SourceIndex++;
            if (SourceIndex>7) SourceIndex=0;

            Log.i("Eric","2023.03.30 current index ="+SourceIndex);


            Sourcehelper.changeSource(SourceIndex);

            SDKSourceItem sourceItem= Sourcehelper.getCurrentSource();
            Log.i("Eric","2023.03.30 id:"+sourceItem.id);
            Log.i("Eric","2023.03.30 id:"+sourceItem.label);
            Log.i("Eric","2023.03.30 id:"+sourceItem.port);
            Log.i("Eric","2023.03.30 id:"+Sourcehelper.getAvailableSourceModelList().size());

            //Sourcehelper.changeSource(7);





            return "true";
        }
        else if(msg.equals("383031735555555555")) //Eric Test Command
        {
            Log.i("Eric","2023.03.30 Verion "+msg);
            Log.i("Eric","2023.03.30 current index ="+SourceIndex);
           // SourceIndex++;
             SourceIndex=0;

            Log.i("Eric","2023.03.30 current index ="+SourceIndex);


            Sourcehelper.changeSource(SourceIndex);

            SDKSourceItem sourceItem= Sourcehelper.getCurrentSource();
            Log.i("Eric","2023.03.30 id:"+sourceItem.id);
            Log.i("Eric","2023.03.30 id:"+sourceItem.label);
            Log.i("Eric","2023.03.30 id:"+sourceItem.port);
            Log.i("Eric","2023.03.30 id:"+Sourcehelper.getAvailableSourceModelList().size());

            //Sourcehelper.changeSource(7);

            return "true";
        }

        else if(msg.equals("38303173243930300D")) //Brightness: Down (-1)
        {

            Log.i("Eric","2023.04.10 Brightness  1 :"+Picturehelper.getBrightness());
            Picturehelper.setBrightness(Picturehelper.getBrightness()-1);
            Log.i("Eric","2023.04.10 Brightness  2:"+Picturehelper.getBrightness());
            /* //Gen 1
            Log.i("VcontrolService","Brightness:"+helper.getBrightness());
            helper.setBrightness(helper.getBrightness()-1);
            Log.i("VcontrolService","Brightness:"+helper.getBrightness());

             */
            return "true";
        }
        else if(msg.equals("38303173243930310D")) //Brightness: Up (+1)
        {
            Log.i("Eric","2023.04.10 Brightness  1 :"+Picturehelper.getBrightness());
            int volume=Picturehelper.getBrightness()+1;
            Log.i("Eric","2023.04.10 Brightness  12 :"+volume);
            Boolean f=Picturehelper.setBrightness(volume);
            Log.i("Eric","2023.04.10 Brightness  2:"+Picturehelper.getBrightness()+":"+f);

            /*//Gen 1
            Log.i("VcontrolService","Brightness:"+helper.getBrightness());
            helper.setBrightness(helper.getBrightness()+1);
            Log.i("VcontrolService","Brightna" +helper.getBrightness());

             */
               return "true";
        }
        else if(msg.contains("38303173243")) //brightness xxx=000~100 38303173243x3x3x0D
        {

            //  Log.i("Eric","number:"+msg.substring(11,12)+msg.substring(13,14)+msg.substring(15,16));
            //  String volume=""+msg.substring(11,12)+msg.substring(13,14)+msg.substring(15,16);
            //   int volnum=Integer.parseInt(volume);
            //   helper.setVolume(volnum);
            //  Log.i("VcontrolService","!!!!Set Volume  TO:"+helper.getVolume());

            Log.i("Eric","2023.04.10 Brightness  2:"+Picturehelper.getBrightness());

            String volume=""+msg.substring(11,12)+msg.substring(13,14)+msg.substring(15,16);
            int volnum=Integer.parseInt(volume);
            Log.i("Eric","2023.04.10 Brightness  volnum :"+volnum);
            Boolean f= Picturehelper.setBrightness(volnum);
            Log.i("Eric","2023.04.10 Brightness  2:"+Picturehelper.getBrightness()+":"+f);

            return "true";
        }
        else if(msg.equals("38303173353930310D")) //Volume: Up (+1)
        {
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            Audiohelper.setVolume(Audiohelper.getVolume()+1);
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            /*
            { //G1
                Log.i("VontrolService", "Volume:" + helper.getVolume());
                helper.setVolume(helper.getVolume() + 1);
                Log.i("VontrolService", "Volume:" + helper.getVolume());
            }

             */
            return "true";
        }
        else if(msg.equals("38303173353930300D")) //Volume: Up (-1)
        {
            {   Log.i("Eric", "Volume:" + Audiohelper.getVolume());
                Audiohelper.setVolume(Audiohelper.getVolume()-1);
                Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            }
            /*
            { //G1
                Log.i("VontrolService", "Volume:" + helper.getVolume());
                helper.setVolume(helper.getVolume() - 1);
                Log.i("VontrolService", "Volume:" + helper.getVolume());
            }
            */
             return "true";
        }
        else if(msg.equals("38303173363030300D")) //Mute : OFFxxx
        {
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            Audiohelper.setMute(false);
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
         //G1   helper.setMute(false);
       //     Log.i("Eric","Volume:"+helper.getVolume());
            return "true";
        }
        else if(msg.equals("38303173363030310D")) //Mute : ON(mute)
        {
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            Audiohelper.setMute(true);
            Log.i("Eric", "Volume:" + Audiohelper.getVolume());
            //G1 helper.setMute(true);
           // Log.i("Eric","Volume:"+helper.getVolume());
            return "true";
        }
        else if(msg.contains("383031734030303")) //Number (0~9) xxx=0~9 383031734030303x0D
        {
            Log.i("Eric","2023.04.10 number:"+msg.substring(15,16));
            Systemhelper.sendKeyEvent(Integer.parseInt(msg.substring(15,16))+7);
            // Gen1 //  helper.setKeyEvent(Integer.parseInt(msg.substring(15,16))+7);
            return "true";
        }
        else if(msg.equals("38303173413030300D")) //Key Pad : UP
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_DPAD_UP);
            Log.i("Eric", "Volume:" + msg);

           //G1 // helper.setKeyEvent(KEYCODE_DPAD_UP);
            return "true";
        }
        else if(msg.equals("38303173413030310D")) //Key Pad : DOWN
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_DPAD_DOWN);
            Log.i("Eric", "Volume:" + msg);
            //helper.setKeyEvent(KEYCODE_DPAD_DOWN);
            return "true";
        }
        else if(msg.equals("38303173413030320D")) //Key Pad : LEFT
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_DPAD_LEFT);
            Log.i("Eric", "Volume:" + msg);
            //helper.setKeyEvent(KEYCODE_DPAD_LEFT);
            return "true";
        }
        else if(msg.equals("38303173413030330D")) //Key Pad : RIGHT
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_DPAD_RIGHT);
            Log.i("Eric", "Volume:" + msg);
          //  helper.setKeyEvent(KEYCODE_DPAD_RIGHT);
            return "true";
        }
        else if(msg.equals("38303173413030340D")) //Key Pad : ENTER
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_DPAD_CENTER);
            //helper.setKeyEvent(KEYCODE_DPAD_CENTER);
            return "true";
        }
        else if(msg.equals("38303173413030350D")) //Key Pad : INPUT
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(178);
            //helper.setKeyEvent(2001);
            return "false";
        }
        else if(msg.equals("38303173413030360D")) //Key Pad : MENU
        {
            Log.i("Eric", "Volume:" + msg);
            Systemhelper.sendKeyEvent(KEYCODE_MENU);
            //helper.setKeyEvent(KEYCODE_MENU);
         //     Log.i("Eric","0103:!!!");

            //ArrayList<String> f=helper.getAvailableSourceModelList();

            //Log.i("E  ric","0103:!!!"+f.size());
            //for(int i=0;i<f.size();i++) {
             //   Log.i("Eric","0103:" +i+"  :"+f.get(i));
           // }

           // ArrayList<Integer> I=helper.getPlugInSourceList();
          //  Log.i("Eric","0103:!!!"+I.size());
          //  for(int j=0;j<I.size();j++) {
         //       Log.i("Eric","0103:" +j+"  :"+I.get(j));
          //  }

            return "true";
        }
        else if(msg.equals("38303173413030370D")) //Key Pad : EXIT
        {
            Systemhelper.sendKeyEvent(KEYCODE_BACK);
            //helper.setKeyEvent(KEYCODE_BACK);
            return "true";
        }
        else if(msg.equals("38303173283030310D")) //BLANK On_Off: On
        {
            return "false";
        }
        else if(msg.equals("38303173283030300D")) //BLANK On_Off: Off
        {
            return "false";
        }
        else if(msg.equals("383031414230303x0D")) //Brightness 8 leves adujstment x=1~8 383031414230303x0D
        {
            Log.i("VontrolService","number:"+msg.substring(15,16));
            Log.i("VontrolService","Brightness():"+Picturehelper.getBrightness());
         Picturehelper.setBrightness(Integer.parseInt(msg.substring(15,16)));
       //Gen1 //     helper.setBrightness(Integer.parseInt(msg.substring(15,16)));


            Log.i("VontrolService","Brightness():"+Picturehelper.getBrightness());

            return "true";
        }
        /*  Gen1
        else if(msg.equals("383031732230305A0D")) //Input Select Cycle
        {

           int source[]= helper.getAvailableSourceList();
           int changeTo=0;
           for(int i=0;i<source.length;i++)
           {
               if(source[i]==helper.getCurrentSource()) {
                   if(i!=source.length-1)
                   changeTo = source[i + 1];
                   else
                       changeTo = source[0];
                   break;
               }
           }
           helper.changeSource(changeTo);
            return "true";
        }

         */
        else if(msg.contains("38303173353")) //Volume xxx=000~100 38303173353x3x3x0D
        {

            Log.i("Eric","number:"+msg.substring(11,12)+msg.substring(13,14)+msg.substring(15,16));
            String volume=""+msg.substring(11,12)+msg.substring(13,14)+msg.substring(15,16);
            int volnum=Integer.parseInt(volume);

            Audiohelper.setVolume(volnum);
            //gen1 //helper.setVolume(volnum);
            Log.i("VcontrolService","!!!!Set Volume  TO:"+helper.getVolume());

            return "true";
        }
        ///////////////////////////////////////
        else if(msg.equals("383031676C3030300D")) //Get-Power status
        {
          // Log.i("VcontrolService","P:"+helper.isInFakePowerOffMode());
          //  Log.i("VcontrolService","P:"+helper.isInFakePowerOffMode());
              if(!Systemhelper.isInFakePowerOffMode())
              return "383031726C3030310D";
            else
                return "383031726C3030300D";

        }
        else if(msg.equals("383031676A3030300D")) //Get-Input select
        {
            SDKSourceItem d= Sourcehelper.getCurrentSource();

            Log.i("Eric","2023.04.12 "+d.label);
            CurrentSourceLable=d.label;
            Log.i("Eric","2023.04.12 "+CurrentSourceLable);
            return "source";
        }
        else if(msg.equals("38303167663030300D")) //Get-Volume  //38 30 31 72 66 3x 3x 3x 0D
        {
            int volume=Audiohelper.getVolume();
          //gen1  int volume=helper.getVolume();
            Log.i("VcontrolService","Get volume A:"+volume);
            String ReturnValune;
            if(volume<10)
                ReturnValune="383031726630303"+Integer.toString(volume)+"0D";
            if(volume>10 && volume <100)
                ReturnValune="3830317266303"+Integer.toString(volume).substring(0,1)+"3"+Integer.toString(volume).substring(1,2)+"0D";
            else
                ReturnValune="38303172663130300D";

            Log.i("VontrolService","Get volume B:"+ReturnValune);

            return ReturnValune;
        }
        else if(msg.equals("38303167673030300D")) //Get-Mute
        {

            if(Audiohelper.isMuteEnabled()      )
                return "38303172673030310D";
            else

            return "38303172673030300D";
        }
        else if(msg.equals("38303167313030300D")) //Get-Operational Hours
        {   ///not yet// ReturnValune="38303172313130300D" //00;

            // ReturnValune="38303172313130300D" //00;
            // ReturnValune="38303172313130300D" //00;
            // ReturnValune="38303172313130300D" //00;
            // ReturnValune="38303172313130300D" //00;
            SDKSystemInfoHelper Systeminfohelper=new SDKSystemInfoHelper();
            long OPHmms=Systeminfohelper.getRunningTime();

            Log.i("Eric","2023.04.11 ophs"+OPHmms);
            int hours=(int)(OPHmms/1000/60/60);
            Log.i("Eric","2023.04.11 ophs 2:"+hours);

            String   ReturnValune1 = "383031723130300D";
            String   ReturnValune2 = "383031723230300D";
            String   ReturnValune3 = "383031723330300D";
            String   ReturnValune4 = "383031723430300D";

       // hours=66128765;

            String hourString= String.valueOf(hours);

            if(hourString.length()==1) ReturnValune4 = "3830317234303"+hourString.substring(0,1)+"0D";
            else  if(hourString.length()==2)
            {
                ReturnValune4 = "38303172343"+hourString.substring(0,1)+"3"+hourString.substring(1,2)+"0D";
            }
            else  if(hourString.length()==3)
            {
                ReturnValune3 = "3830317233303"+hourString.substring(0,1)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(1,2)+"3"+hourString.substring(2,3)+"0D";
            }
            else  if(hourString.length()==4)
            {
                ReturnValune3 = "38303172333"+hourString.substring(0,1)+"3"+hourString.substring(1,2)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(2,3)+"3"+hourString.substring(3,4)+"0D";
            }
            else  if(hourString.length()==5)
            {
                ReturnValune2 = "383031723230"+"3"+hourString.substring(0,1)+"0D";
                ReturnValune3 = "38303172333"+hourString.substring(1,2)+"3"+hourString.substring(2,3)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(3,4)+"3"+hourString.substring(4,5)+"0D";
            }
            else  if(hourString.length()==6)
            {
                ReturnValune2 = "38303172323"+hourString.substring(0,1)+"3"+hourString.substring(1,2)+"0D";
                ReturnValune3 = "38303172333"+hourString.substring(2,3)+"3"+hourString.substring(3,4)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(4,5)+"3"+hourString.substring(5,6)+"0D";
            }
            else  if(hourString.length()==7)
            {
                ReturnValune1 = "383031723130"+"3"+hourString.substring(0,1)+"0D";
                ReturnValune2 = "38303172323"+hourString.substring(1,2)+"3"+hourString.substring(2,3)+"0D";
                ReturnValune3 = "38303172333"+hourString.substring(3,4)+"3"+hourString.substring(4,5)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(5,6)+"3"+hourString.substring(6,7)+"0D";
            }
            else  if(hourString.length()==8)
            {
                ReturnValune1 = "38303172313"+hourString.substring(0,1)+"3"+hourString.substring(1,2)+"0D";
                ReturnValune2 = "38303172323"+hourString.substring(2,3)+"3"+hourString.substring(3,4)+"0D";
                ReturnValune3 = "38303172333"+hourString.substring(4,5)+"3"+hourString.substring(5,6)+"0D";
                ReturnValune4 = "38303172343"+hourString.substring(6,7)+"3"+hourString.substring(7,8)+"0D";
            }


                Log.i("Eric","S:"+hourString);
                Log.i("Eric","S:"+hourString.length());
                Log.i("Eric","S:"+hourString.substring(0,1));
                /*
                ReturnValune3 = "38303172343"+Integer.toString(hours/10).substring(0,1)+"3"+
                        Integer.toString(hours%10).substring(0,1)+"0D";

                ReturnValune4 = "38303172343"+Integer.toString(hours/10).substring(0,1)+"3"+
                        Integer.toString(hours%10).substring(0,1)+"0D";
*/


            Log.i("eric","2023.04.11 1:"+ReturnValune1);
            Log.i("eric","2023.04.11 2:"+ReturnValune2);
            Log.i("eric","2023.04.11 3:"+ReturnValune3);
            Log.i("eric","2023.04.11 4:"+ReturnValune4);

            OPhReturn=new ArrayList();
            OPhReturn.add(ReturnValune1); OPhReturn.add(ReturnValune3);
            OPhReturn.add(ReturnValune3);OPhReturn.add(ReturnValune4);

            return "hour";
          //  return "true";
        }
        else if(msg.equals("38303167623030300D")) //HoursGet-Brightness  //38 30 31 72 62 3x 3x 3x 0D
        {   int volume=Picturehelper.getBrightness();
            Log.i("VcontrolService","Get volume A:"+volume);
            String ReturnValune;
            if(volume<10)
                ReturnValune="383031726230303"+Integer.toString(volume)+"0D";
            if(volume>10 && volume <100)
                ReturnValune="3830317262303"+Integer.toString(volume).substring(0,1)+"3"+Integer.toString(volume).substring(1,2)+"0D";
            else
                ReturnValune="38303172623130300D";

            Log.i("VontrolService","Get volume B:"+ReturnValune);

            return ReturnValune;
        }


        else  return "false";
    }

  public String CurrentSourceLable="";


    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
//sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String hexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int count = 0; count < hex.length() - 1; count += 2) {
            String output = hex.substring(count, (count + 2));    //grab the hex in pairs
            int decimal = Integer.parseInt(output, 16);    //convert hex to decimal
            sb.append((char) decimal);    //convert the decimal to character
        }
        return sb.toString();
    }
}
