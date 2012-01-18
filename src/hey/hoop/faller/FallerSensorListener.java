package hey.hoop.faller;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class FallerSensorListener implements Runnable, SensorEventListener {
    private static final long MAX_SLEEP_TIME = 2000;
    private static final String TAG = FallerSensorListener.class.getCanonicalName();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGeomagneticFieldMeter;
    private boolean mNeeded;
    private final FallerDrawer mDrawer;

    public FallerSensorListener(SensorManager sensorManager, FallerDrawer drawer) {
        mSensorManager = sensorManager;
        mDrawer = drawer;
        mNeeded = false;
    }

    @Override
    public void run() {
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagneticFieldMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        unpause();
        while (mNeeded) {
            try {
                Thread.sleep(MAX_SLEEP_TIME);
            } catch (InterruptedException ex) {
                /* ignore */
            }
        }
    }

    public void setNeeded(boolean needed) {
        mNeeded = needed;
    }

    public void pause() {
        mSensorManager.unregisterListener(this);
    }

    public void unpause() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGeomagneticFieldMeter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (mDrawer) {
            if (sensorEvent.sensor == mAccelerometer)
                mDrawer.setGravity(sensorEvent.values);
            else if (sensorEvent.sensor == mGeomagneticFieldMeter)
                mDrawer.setGeomagnetic(sensorEvent.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "Accuracy of " + sensor + " has changed to " + i + ".");
    }
}