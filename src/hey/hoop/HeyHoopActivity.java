package hey.hoop;

import hey.hoop.provider.DataForChartProvider;
import hey.hoop.services.ListenerService;
import hey.hoop.services.ServiceLaunchReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HeyHoopActivity extends Activity implements SensorEventListener,
		OnClickListener {
	private static final int DIALOG_CHARTDROID_DOWNLOAD = 0;

	private static final String TAG = "Hey Hoop";

	final int DIMS = 3;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private TextView mTextX;
	private TextView mTextY;
	private TextView mTextZ;
//	private TextView mTextResolution;
//	private TextView mTextMinDelay;
	private ProgressBar mCircle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mTextX = (TextView) findViewById(R.id.editX);
		mTextX.setEnabled(false);
		mTextY = (TextView) findViewById(R.id.editY);
		mTextY.setEnabled(false);
		mTextZ = (TextView) findViewById(R.id.editZ);
		mTextZ.setEnabled(false);
		// mTextResolution = (TextView) findViewById(R.id.editResolution);
		// mTextMinDelay = (TextView) findViewById(R.id.editMinDelay);
		mCircle = (ProgressBar) findViewById(R.id.progressBar1);
		((Button) findViewById(R.id.startServiceButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent starter = new Intent(HeyHoopActivity.this,
								ServiceLaunchReceiver.class);
						startActivity(starter);
						vibrator.vibrate(500);
					}
				});
		((Button) findViewById(R.id.stopServiceButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent stoper = new Intent(HeyHoopActivity.this,
								ListenerService.class);
						stopService(stoper);
						mCircle.setEnabled(false);
						vibrator.vibrate(250);
					}
				});
		((Button) findViewById(R.id.viewEntriesButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent viewEntriesIntent = new Intent(
								HeyHoopActivity.this, ListDbActivity.class);
						startActivity(viewEntriesIntent);
					}
				});
		((Button) findViewById(R.id.openChartButton))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent openChartIntent = new Intent(Intent.ACTION_VIEW,
								DataForChartProvider.PROVIDER_URI);
						if (Market.isIntentAvailable(HeyHoopActivity.this,
								openChartIntent))
							startActivity(openChartIntent);
						else
							showDialog(DIALOG_CHARTDROID_DOWNLOAD);
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// showAccuracy();
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == DIALOG_CHARTDROID_DOWNLOAD)
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.download_title)
					.setMessage(R.string.download_message)
					.setPositiveButton(R.string.download_market,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(Market
											.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
								}
							})
					.setNeutralButton(R.string.download_web,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Intent.ACTION_VIEW,
											Market.APK_DOWNLOAD_URI_CHARTDROID));
								}
							}).create();
		else
			return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DIALOG_CHARTDROID_DOWNLOAD) {
			boolean has_android_market = Market.isIntentAvailable(this, Market
					.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
			Log.d(TAG, "has android market? " + has_android_market);
			dialog.findViewById(android.R.id.button1).setVisibility(
					has_android_market ? View.VISIBLE : View.GONE);
		}
	}

	//
	// private void showAccuracy() {
	// mTextResolution.setText(Float.toString(mSensor.getResolution()));
	// mTextMinDelay.setText(Float.toString(mSensor.getMinDelay()));
	// }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// showAccuracy();
		Toast.makeText(this, R.string.accuracy_changed, Toast.LENGTH_SHORT);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] filtered = event.values;
		mTextX.setText(Float.toString(filtered[0]));
		mTextY.setText(Float.toString(filtered[1]));
		mTextZ.setText(Float.toString(filtered[2]));
	}

	@Override
	public void onClick(View v) {
		finish();
	}
}