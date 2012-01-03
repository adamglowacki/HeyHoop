package hey.hoop.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceLaunchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ServiceManager.startListener(context);
	}
}
