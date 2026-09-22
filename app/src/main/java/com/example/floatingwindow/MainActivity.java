package com.example.floatingwindow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private Button floatButton;
    private WindowManager.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Button openBtn = new Button(this);
        openBtn.setText("点击开启悬浮窗");
        openBtn.setOnClickListener(v -> checkPermissionAndShow());
        setContentView(openBtn);
    }

    private void checkPermissionAndShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "请开启悬浮窗权限后返回", Toast.LENGTH_LONG).show();
                return;
            }
        }
        showFloatingWindow();
    }

    private void showFloatingWindow() {
        if (floatButton != null) return;

        floatButton = new Button(this);
        floatButton.setText("控制");
        floatButton.setBackgroundColor(Color.parseColor("#FF4081")); // 初始粉色
        floatButton.setTextColor(Color.WHITE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        // ================= 拖动逻辑 =================
        floatButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatButton, params);
                        return true;
                }
                return false;
            }
        });

        // ================= 功能 1：单击弹出菜单 =================
        floatButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("悬浮窗控制台");
            String[] options = {"变成绿色", "变成蓝色", "关闭悬浮窗"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    floatButton.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
                } else if (which == 1) {
                    floatButton.setBackgroundColor(Color.parseColor("#2196F3")); // 蓝色
                } else if (which == 2) {
                    closeFloatingWindow();
                }
            });
            builder.show();
        });

        // ================= 功能 2：长按直接关闭 =================
        floatButton.setOnLongClickListener(v -> {
            closeFloatingWindow();
            Toast.makeText(MainActivity.this, "已关闭悬浮窗", Toast.LENGTH_SHORT).show();
            return true;
        });

        windowManager.addView(floatButton, params);
        Toast.makeText(this, "悬浮窗已开启，单击变色，长按关闭", Toast.LENGTH_LONG).show();
    }

    // 关闭悬浮窗的方法
    private void closeFloatingWindow() {
        if (floatButton != null) {
            windowManager.removeView(floatButton);
            floatButton = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeFloatingWindow();
    }
          }
