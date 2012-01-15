package hey.hoop.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import hey.hoop.HHDbAdapter;

public class ServiceManager {

    public static boolean isListenerRunning(Context ctx) {
        ActivityManager manager = (ActivityManager) ctx
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE))
            if (ListenerService.class.getCanonicalName().equals(
                    service.service.getClassName()))
                return true;
        return false;
    }

    public static void startListener(Context ctx) {
        Intent service = new Intent(ctx, ListenerService.class);
        ctx.startService(service);
    }

    private static void setListenerInstalled(Context ctx, boolean installed) {
        HHDbAdapter dbAdapter = new HHDbAdapter(ctx);
        dbAdapter.open(true);
        try {
            if (installed)
                dbAdapter.setBool(HHDbAdapter.INSTALLED_BOOL);
            else
                dbAdapter.unsetBool(HHDbAdapter.INSTALLED_BOOL);
        } finally {
            dbAdapter.close();
        }
    }

    public static void startAndRegisterListener(Context ctx) {
        startListener(ctx);
        setListenerInstalled(ctx, true);
    }

    public static void stopAndUnregisterListener(Context ctx) {
        Intent service = new Intent(ctx, ListenerService.class);
        ctx.stopService(service);
        setListenerInstalled(ctx, false);
    }
}
