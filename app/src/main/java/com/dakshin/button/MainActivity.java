package com.dakshin.button;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    int DRAW_REQUEST_CODE=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!Settings.canDrawOverlays(this))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_REQUEST_CODE);
        }
        else if(Settings.canDrawOverlays(this)){
            Intent intent=new Intent(this, ButtonService.class);
            startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==DRAW_REQUEST_CODE) {
            if(Settings.canDrawOverlays(this))
            {
                Intent intent=new Intent(this, ButtonService.class);
                startService(intent);
            }

        }
    }
}
