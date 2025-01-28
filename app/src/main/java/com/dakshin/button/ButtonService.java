package com.dakshin.button;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class ButtonService extends Service implements View.OnTouchListener {
    private WindowManager windowManager;
    private ImageView button;
    private WindowManager.LayoutParams params;
    private Process process;

    final Handler handler = new Handler();
    Runnable longPressed = new Runnable() {
        public void run() {
            try {
                params.alpha = 0;
                windowManager.updateViewLayout(button, params);
                process.getOutputStream().write(("input keyevent " + KeyEvent.KEYCODE_POWER + '\n').getBytes());
                process.getOutputStream().flush();
                Thread.sleep(1000);
                params.alpha = 1.0f;
                windowManager.updateViewLayout(button, params);
            } catch (IOException | InterruptedException e) {
                Log.e("tag", "error");
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        button = new ImageView(this);
        button.setImageResource(R.drawable.button);
        params = new WindowManager.LayoutParams(
                100, 100,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        windowManager.addView(button, params);
        button.setOnTouchListener(this);
    }

    @Override
    public void onDestroy() {
        if (button != null)
            windowManager.removeView(button);
        super.onDestroy();
    }

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private long downTapTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long curTime = System.currentTimeMillis();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                downTapTime = curTime;
                handler.postDelayed(longPressed, ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(longPressed);
                Toast.makeText(getApplicationContext(), "Floating button clicked!", Toast.LENGTH_SHORT).show();
//                if (curTime - downTapTime <= 150) {
//                    try {
//                        process.getOutputStream().write(("su -c input keyevent " + KeyEvent.KEYCODE_BACK + "\n").getBytes());
//                        process.getOutputStream().flush();
//                    } catch (IOException e) {
//                        Log.e("tag", "Shell command failed");
//                    }
//                }

                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getRawY() - initialTouchY) > 100 && Math.abs(event.getRawX() - initialTouchX) > 100)
                    handler.removeCallbacks(longPressed);
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(button, params);
                return true;
        }
        return false;
    }
}
