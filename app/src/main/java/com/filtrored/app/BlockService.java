package com.filtrored.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * VPN local que solo captura el tráfico de Life360 y lo descarta.
 * El resto de las apps no pasa por la VPN y usa internet normalmente.
 */
public class BlockService extends VpnService {
    static final String TARGET_PACKAGE = "com.life360.android.safetymapd";
    static final String ACTION_STOP = "com.filtrored.app.STOP";
    private static final String TAG = "BlockService";

    private static volatile boolean running;

    private ParcelFileDescriptor tun;
    private Thread drainThread;

    static boolean isRunning() {
        return running;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            Prefs.setEnabled(this, false);
            stopTunnel();
            stopSelf();
            return START_NOT_STICKY;
        }
        Prefs.setEnabled(this, true);
        if (!startTunnel()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private synchronized boolean startTunnel() {
        if (tun != null) {
            return true;
        }
        Builder builder = new Builder()
                .setSession(getString(R.string.app_name))
                .addAddress("10.215.173.1", 32)
                .addRoute("0.0.0.0", 0)
                .addAddress("fd00:d7:ad::1", 128)
                .addRoute("::", 0);
        try {
            // Sin esta línea la VPN capturaría todas las apps, así que si falla no se arranca.
            builder.addAllowedApplication(TARGET_PACKAGE);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Life360 no está instalada");
            return false;
        }
        tun = builder.establish();
        if (tun == null) {
            Log.w(TAG, "Falta el permiso de VPN");
            return false;
        }
        running = true;
        final ParcelFileDescriptor fd = tun;
        drainThread = new Thread(() -> drain(fd), "tun-drain");
        drainThread.start();
        return true;
    }

    // Lee y descarta los paquetes de Life360 para que nunca salgan del teléfono.
    private static void drain(ParcelFileDescriptor fd) {
        FileInputStream in = new FileInputStream(fd.getFileDescriptor());
        byte[] buffer = new byte[32767];
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int length = in.read(buffer);
                if (length < 0) {
                    break;
                }
                if (length == 0) {
                    Thread.sleep(200);
                }
            }
        } catch (IOException | InterruptedException e) {
            // El túnel se cerró.
        }
    }

    private synchronized void stopTunnel() {
        running = false;
        if (drainThread != null) {
            drainThread.interrupt();
            drainThread = null;
        }
        if (tun != null) {
            try {
                tun.close();
            } catch (IOException ignored) {
            }
            tun = null;
        }
    }

    @Override
    public void onRevoke() {
        // Otra VPN tomó el control o se desconectó desde Ajustes.
        Prefs.setEnabled(this, false);
        stopTunnel();
        super.onRevoke();
    }

    @Override
    public void onDestroy() {
        stopTunnel();
        super.onDestroy();
    }
}
