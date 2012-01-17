package hey.hoop.faller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.SurfaceHolder;
import hey.hoop.R;

import java.util.Date;

import static hey.hoop.faller.FallerMath.sqr;
import static java.lang.Math.sqrt;

public class FallerDrawer extends Thread {
    private static final float ACCELERATION_SCALE_FACTOR = 0.05f;
    private static final float ANIMAL_RADIUS = 20;
    private static final float GOAL_RADIUS = 30;
    //    private static final long[] VIBRATION_PATTERN = new long[]{100, 100, 50};
    private static final float FREE_FALL_THRESHOLD = 0.001f;
    private static final long FREE_FALL_TIME = 1000;
    private static final float CLOCK_WIDTH = 100;
    private static final float CLOCK_MARGIN = 50;
    private static final float CLOCK_TEXT_SIZE = 40;

    private final SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady;

    private Bitmap mBackground;
    private Bitmap mScaledBackground;
    private Paint mArrowPaint;
    private Paint mGoalPaint;
    private Paint mAnimalPaint;
    private Paint mClockPaint;

    private int mCanvasWidth;
    private int mCanvasHeight;
    private java.text.DateFormat mDateFormat;

    private long mLastTimestamp;
    private float mLastOrientationX;
    private float mLastOrientationY;
    private float mLastAccelerationX;
    private float mLastAccelerationY;
    private float mGravity[];
    private float mGeomagnetic[];
    private FallerSensorListener sensorListener;
    private long mGameTime;
//    private Vibrator mVibrator;

    private float x = 400;
    private float y = 400;
    private float dx;
    private float dy;
    private float goalX = 100;
    private float goalY = 100;
    private boolean mAccident;
    //    private boolean mVibrating;
    private long mFreeFallStart;
    private float mClimbingSpeed = 0.1f;
    private boolean mFreeFalling;

    public FallerDrawer(Context ctx, SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mBackground = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.faller_background);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setARGB(255, 255, 255, 255);
        mArrowPaint.setStrokeWidth(4);
        mAnimalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimalPaint.setARGB(255, 0, 0, 200);
        mGoalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGoalPaint.setARGB(255, 200, 200, 0);
        mClockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClockPaint.setARGB(255, 0, 0, 0);
        mClockPaint.setTextSize(CLOCK_TEXT_SIZE);
        mDateFormat = DateFormat.getTimeFormat(ctx);
        mGravity = new float[3];
        mGeomagnetic = new float[3];
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new FallerSensorListener(sensorManager, this);
//        mVibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void pause() {
        /* ... */
    }

    public void unpause() {
        /* ... */
    }

    public Bundle saveState(Bundle map) {
        if (map != null)
            synchronized (mSurfaceHolder) {
                /* ... */
            }
        return map;
    }

    public void restoreState(Bundle map) {
        synchronized (mSurfaceHolder) {
            /* ... */
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        long currentTimestamp;
        float[] rotation = new float[9];
        float[] currentOrientation;
        sensorListener.setNeeded(true);
        sensorListener.start();
        try {
            while (mSurfaceReady) {
                canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    synchronized (this) {
                        SensorManager.getRotationMatrix(rotation, null, mGravity, mGeomagnetic);
                    }
                    currentTimestamp = System.currentTimeMillis();
                    currentOrientation = FallerMath.applyMatrix(FallerMath.transposeMatrix(rotation, 3, 3),
                            new float[]{0, 0, 1});
                    synchronized (mSurfaceHolder) {
                        updateSituation(currentOrientation, currentTimestamp);
                        redraw(canvas);
                    }
                } finally {
                    if (canvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        } finally {
            sensorListener.setNeeded(false);
            boolean notJoined = true;
            while (notJoined)
                try {
                    sensorListener.join();
                    notJoined = false;
                } catch (InterruptedException ex) {
                    /* ignore */
                }
        }
    }

    private void updateSituation(float[] orientation, long timestamp) {
        long elapsed = timestamp - mLastTimestamp;
        mGameTime += elapsed;
        mLastOrientationX = orientation[0];
        mLastOrientationY = -orientation[1];
        float accelerationX = -mLastOrientationX * ACCELERATION_SCALE_FACTOR;
        float accelerationY = -mLastOrientationY * ACCELERATION_SCALE_FACTOR;
        float accelerationChange = sqr(mLastAccelerationX - accelerationX) + sqr(mLastAccelerationY - accelerationY);
        if (accelerationChange > FREE_FALL_THRESHOLD) {
            mFreeFalling = true;
            mFreeFallStart = timestamp;
        } else {
            mFreeFalling = timestamp - mFreeFallStart < FREE_FALL_TIME;
        }
        float speedX, speedY;
        if (mFreeFalling) {
            speedX = dx;
            speedY = dy;
        } else {
            dx = 0;
            dy = 0;
            float distance = (float) sqrt(sqr(goalX - x) + sqr(goalY - y));
            speedX = goalX - x;
            speedY = goalY - y;
            if (sqr(speedX) + sqr(speedY) > sqr(mClimbingSpeed)) {
                speedX = mClimbingSpeed * (goalX - x) / distance;
                speedY = mClimbingSpeed * (goalY - y) / distance;
            }
        }
        x += speedX * elapsed;
        y += speedY * elapsed;
        if (x < 0 || x >= mCanvasWidth) {
            dx = 0;
            if (x < 0) x = 0;
            else x = mCanvasWidth - 1;
        }
        if (y < 0 || y >= mCanvasHeight) {
            dy = 0;
            if (y < 0) y = 0;
            else y = mCanvasHeight - 1;
        }
        mAccident = sqr(x - goalX) + sqr(y - goalY) < sqr(GOAL_RADIUS + ANIMAL_RADIUS);
//        if (mAccident != mVibrating) {
//            if (mAccident)
//                mVibrator.vibrate(VIBRATION_PATTERN, 1);
//            else
//                mVibrator.cancel();
//            mVibrating = mAccident;
//        }
        { /* make it slower but after deciding about free fall */
            accelerationX = FallerMath.decreaseAbsValue(accelerationX);
            accelerationY = FallerMath.decreaseAbsValue(accelerationY);
        }
        dx += accelerationX * elapsed;
        dy += accelerationY * elapsed;
//        { /* correct readings */
//            float absAccelerationX = Math.abs(accelerationX);
//            float absAccelerationY = Math.abs(accelerationY);
//            float avgAbsAcceleration = (absAccelerationX + absAccelerationY) / 2;
//            if (absAccelerationX < absAccelerationY)
//                accelerationX = Math.signum(accelerationX) * avgAbsAcceleration;
//            else
//                accelerationY = Math.signum(accelerationY) * avgAbsAcceleration;
//        }
        mLastTimestamp = timestamp;
        mLastAccelerationX = accelerationX;
        mLastAccelerationY = accelerationY;
    }

    private void redraw(Canvas canvas) {
        if (mAccident)
            canvas.drawRGB(255, 0, 0);
        else
            canvas.drawRGB(100, 0, 0);
        canvas.drawBitmap(mScaledBackground, 0, 0, null);
        canvas.drawCircle(goalX, goalY, GOAL_RADIUS, mGoalPaint);
        canvas.drawCircle(x, y, ANIMAL_RADIUS, mAnimalPaint);
        canvas.drawText(mDateFormat.format(new Date(mGameTime)), mCanvasWidth - CLOCK_WIDTH - CLOCK_MARGIN,
                CLOCK_MARGIN, mClockPaint);
    }

    public void setSurfaceReady(boolean ready) {
        mSurfaceReady = ready;
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;
            mScaledBackground = Bitmap.createScaledBitmap(mBackground, mCanvasWidth, mCanvasHeight, true);
        }
    }

    public void setGravity(float[] newGravity) {
        mGravity = newGravity;
    }

    public void setGeomagnetic(float[] newGeomagnetic) {
        mGeomagnetic = newGeomagnetic;
    }
}
