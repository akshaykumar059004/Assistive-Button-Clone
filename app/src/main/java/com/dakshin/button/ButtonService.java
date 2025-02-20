package com.dakshin.button;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;

public class ButtonService extends AccessibilityService implements View.OnTouchListener {
    private WindowManager windowManager;
    private ImageView button;
    private WindowManager.LayoutParams params;

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("ButtonService", "Accessibility Service Connected");

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
        super.onDestroy();
        if (button != null) {
            windowManager.removeView(button);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;

            case MotionEvent.ACTION_UP:
                Toast.makeText(getApplicationContext(), "Floating button clicked!", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    if (rootNode != null) {
                        extractTextFromNode(rootNode);
                    } else {
                        Log.e("ButtonService", "Root node is still null after delay. Ensure service is enabled.");
                    }
                }, 500); // Delay by 500ms

                return true;

            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(button, params);
                return true;
        }
        return false;
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    private void extractTextFromNode(AccessibilityNodeInfo node) {
        if (node == null) return;

        // Check if the node has text
        if (node.getText() != null) {
            Log.d("ScreenReader", "Found text: " + node.getText().toString());
        }

        // Recursively check child nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            extractTextFromNode(node.getChild(i));
        }
    }


    @Override
    public void onInterrupt() {
        Log.d("ButtonService", "Accessibility Service Interrupted");
    }
}
