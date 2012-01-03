package hey.hoop;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class ListenerService extends Service implements SensorEventListener {
	private static final int SAMPLE_SIZE = 20;
	private static int mSampleCounter;
	private static final float ALPHA = 0.8f;
	private static final int DIMS = 3;
	private ScheduledThreadPoolExecutor mExecutor;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private HHDbAdapter mDbAdapter;
	private float[] gravity;

	@Override
	public void onCreate() {
		super.onCreate();
		gravity = new float[DIMS];
		mExecutor = new ScheduledThreadPoolExecutor(1);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mDbAdapter = new HHDbAdapter(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				synchronized (ListenerService.this) {
					mSampleCounter = 0;
					mDbAdapter.deleteReadings();
					mSensorManager.registerListener(ListenerService.this,
							mSensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
			}
		}, 5L, 30L, TimeUnit.SECONDS);
		Toast.makeText(ListenerService.this, "Task scheduled",
				Toast.LENGTH_LONG).show();
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service about to be destroyed!",
				Toast.LENGTH_LONG).show();
		mExecutor.shutdown();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		/* ignore */
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// float[] filtered = filterOutNoise(filterOutGravity(event.values));
		float[] filtered = event.values;
		float value = 0;
		for (int i = 0; i < DIMS; ++i)
			value += Math.abs(filtered[i]);
		// value += filtered[i] * filtered[i];
		// value = (float) Math.sqrt(value);
		mDbAdapter.open(true);
		mDbAdapter.insertReading(value);
		mDbAdapter.close();
		synchronized (this) {
			if (++mSampleCounter >= SAMPLE_SIZE) {
				mSensorManager.unregisterListener(this);
				flushReadings();
			}
		}
	}

	private void flushReadings() {
		mDbAdapter.open(true);
		mDbAdapter.flushReadings();
		mDbAdapter.close();
	}

	private float[] filterOutGravity(float[] values) {
		float[] filtered = new float[DIMS];
		for (int i = 0; i < DIMS; ++i) {
			gravity[i] = ALPHA * gravity[i] + (1 - ALPHA) * values[i];
			filtered[i] = values[i] - gravity[i];
		}
		return filtered;
	}

	private float[] filterOutNoise(float[] values) {
		float[] filtered = new float[DIMS];
		for (int i = 0; i < DIMS; ++i)
			filtered[i] = Math.round(values[i]);
		return filtered;
	}

}
