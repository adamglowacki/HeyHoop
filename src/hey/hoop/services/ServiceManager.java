package hey.hoop.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

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

    public static void startAndRegisterListener(Context ctx) {
        startListener(ctx);
//        ctx.getPackageManager().setComponentEnabledSetting(
//                new ComponentName(ctx, ServiceLaunchReceiver.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
    }

    public static void stopAndUnregisterListener(Context ctx) {
        Intent service = new Intent(ctx, ListenerService.class);
        ctx.stopService(service);
//        ctx.getPackageManager().setComponentEnabledSetting(
//                new ComponentName(ctx, ServiceLaunchReceiver.class),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
    }
}
