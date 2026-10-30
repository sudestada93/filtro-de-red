package com.filtrored.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQUEST_VPN = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private Button toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        toggle = findViewById(R.id.toggle);
        toggle.setOnClickListener(v -> {
            if (BlockService.isRunning()) {
                stopBlocking();
            } else {
                startBlocking();
            }
        });
        findViewById(R.id.vpn_settings).setOnClickListener(v -> openVpnSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void startBlocking() {
        if (!isTargetInstalled()) {
            refresh();
            return;
        }
        Intent consent = VpnService.prepare(this);
        if (consent != null) {
            startActivityForResult(consent, REQUEST_VPN);
        } else {
            onActivityResult(REQUEST_VPN, RESULT_OK, null);
        }
    }

    private void stopBlocking() {
        startService(new Intent(this, BlockService.class).setAction(BlockService.ACTION_STOP));
        handler.postDelayed(this::refresh, 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VPN && resultCode == RESULT_OK) {
            startService(new Intent(this, BlockService.class));
            handler.postDelayed(this::refresh, 500);
        }
    }

    private void refresh() {
        boolean running = BlockService.isRunning();
        if (running) {
            status.setText(R.string.status_on);
            toggle.setText(R.string.action_off);
            toggle.setEnabled(true);
        } else if (!isTargetInstalled()) {
            status.setText(R.string.status_not_installed);
            toggle.setText(R.string.action_on);
            toggle.setEnabled(false);
        } else {
            status.setText(R.string.status_off);
            toggle.setText(R.string.action_on);
            toggle.setEnabled(true);
        }
    }

    private boolean isTargetInstalled() {
        try {
            getPackageManager().getPackageInfo(BlockService.TARGET_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openVpnSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }
}
