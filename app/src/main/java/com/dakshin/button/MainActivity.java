package com.dakshin.button;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int DRAW_REQUEST_CODE = 1; // Code for overlay permission

    private ToggleButton toggleButton; // Toggle for floating service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);

        // Set initial state based on permissions
        toggleButton.setChecked(Settings.canDrawOverlays(this) && isAccessibilityServiceEnabled());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestPermissions();
                } else {
                    stopFloatingService();
                }
            }
        });
    }

    // Requests necessary permissions
    private void requestPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            // Request Overlay permission
            Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(overlayIntent, DRAW_REQUEST_CODE);
        } else if (!isAccessibilityServiceEnabled()) {
            // Request Accessibility permission
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Enable Accessibility Service for this app", Toast.LENGTH_LONG).show();
        } else {
            // Start Floating Service if permissions are granted
            startFloatingService();
        }
    }

    // Check if the accessibility service is enabled
    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + MyAccessibilityService.class.getName();
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (enabledServices != null) {
            splitter.setString(enabledServices);
            while (splitter.hasNext()) {
                if (splitter.next().equalsIgnoreCase(service)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Start Floating Button Service
    private void startFloatingService() {
        Intent intent = new Intent(this, ButtonService.class);
        startService(intent);
    }

    // Stop Floating Button Service
    private void stopFloatingService() {
        Intent intent = new Intent(this, ButtonService.class);
        stopService(intent);
    }

    // Handle Overlay Permission Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRAW_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                requestPermissions(); // Check accessibility permission next
            } else {
                toggleButton.setChecked(false); // Disable toggle if permission is denied
            }
        }
    }
}
