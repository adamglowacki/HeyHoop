package hey.hoop.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import hey.hoop.HHDbAdapter;

public class ServiceLaunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        HHDbAdapter dbAdapter = new HHDbAdapter(context);
        dbAdapter.open(false);
        try {
            if (!dbAdapter.isBool(HHDbAdapter.INSTALLED_BOOL))
                return;
        } finally {
            dbAdapter.close();
        }
        ServiceManager.startListener(context);
    }
}