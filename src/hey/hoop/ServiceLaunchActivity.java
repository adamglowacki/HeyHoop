package hey.hoop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ServiceLaunchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent startIntent = new Intent(this, ListenerService.class);
		startService(startIntent);
		finish();
	}
}
