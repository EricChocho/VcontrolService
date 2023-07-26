package com.viewsonic.service1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public  static MyAPP myapp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myapp=new MyAPP();

        Intent intent = new Intent(MainActivity.this, VcontrolService.class);
        //startService(                                                                                                                                                                                                                                                                                                                                                                             intent);

        startForegroundService(intent);



     //   Intent intent2 = new Intent();
       // intent2.setAction("com.com.viewsonic.service1.VcontrolServic");
      //  intent2.setPackage("com.viewsonic.service1");

      //    startForegroundService(intent2);

        //this.finish();
    }
}