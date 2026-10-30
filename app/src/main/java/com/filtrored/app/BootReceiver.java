package com.filtrored.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

/** Reactiva el bloqueo al reiniciar. La opción "VPN siempre activa" es más fiable. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || !Prefs.isEnabled(context)
                || VpnService.prepare(context) != null) {
            return;
        }
        try {
            context.startService(new Intent(context, BlockService.class));
        } catch (IllegalStateException e) {
            Log.w("BootReceiver", "No se pudo iniciar al arrancar", e);
        }
    }
}
