package hey.hoop.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import hey.hoop.HHDbAdapter;
import hey.hoop.R;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ListenerService extends Service implements SensorEventListener {
    private static final int SAMPLE_SIZE = 20;
    private static int mSampleCounter;
    private static final int DIMS = 3;
    private ScheduledThreadPoolExecutor mExecutor;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private HHDbAdapter mDbAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutor = new ScheduledThreadPoolExecutor(1);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mDbAdapter = new HHDbAdapter(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSensor != null)
            mExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    synchronized (ListenerService.this) {
                        mSampleCounter = 0;
                        deleteReadings();
                        mSensorManager.registerListener(ListenerService.this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }
            }, 5L, 30L, TimeUnit.SECONDS);
        Toast.makeText(ListenerService.this, R.string.walk_track_started, Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.shutdown();
        Toast.makeText(this, R.string.walk_track_stopped, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* ignore */
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] filtered = event.values;
        float value = 0;
        for (int i = 0; i < DIMS; ++i)
            value += Math.abs(filtered[i]);
        synchronized (this) {
            insertReading(value);
            if (++mSampleCounter >= SAMPLE_SIZE) {
                mSensorManager.unregisterListener(this);
                flushReadings();
            }
        }
    }

    private void insertReading(float value) {
        mDbAdapter.open(true);
        mDbAdapter.insertReading(value);
        mDbAdapter.close();
    }

    private void flushReadings() {
        mDbAdapter.open(true);
        mDbAdapter.flushReadings();
        mDbAdapter.close();
    }

    private void deleteReadings() {
        mDbAdapter.open(true);
        mDbAdapter.deleteReadings();
        mDbAdapter.close();
    }
}
