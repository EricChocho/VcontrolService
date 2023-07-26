package com.viewsonic.service1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootComplieted extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
     //   Log.i("Eric","BootFinish2");
      //  Intent intent2 = new Intent();
        //Intent intent2 = new Intent(BootComplieted.this, VcontrolService.class);
/*
        Intent intent1=new Intent(context,MainActivity.class);
        //startService(intent);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.i("Eric","BootFinish3");
        context.startActivity(intent1);
        Log.i("Eric","BootFinish4");
*/Log.i("Eric","BootFinish33");
        Intent serviceLauncher = new Intent(context, VcontrolService.class);
    //    Log.i("Eric","BootFinish44");
        context.startForegroundService(serviceLauncher);
      //  Log.i("Eric","BootFinish55");
       // throw new UnsupportedOperationException("Not yet implemented");
    }
}