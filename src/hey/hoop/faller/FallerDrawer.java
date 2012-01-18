package hey.hoop.faller;

import android.content.Context;
import android.graphics.*;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import hey.hoop.R;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static android.util.FloatMath.cos;
import static android.util.FloatMath.sin;
import static hey.hoop.faller.FallerMath.putIntoRange;
import static hey.hoop.faller.FallerMath.sqr;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;

public class FallerDrawer implements Runnable {
    private static final float ACCELERATION_SCALE_FACTOR = 0.15f;
    private static final float RESISTANCE_SCALE_FACTOR = 0.08f;
    private static final float GOAL_RADIUS = 30;
    private static final float GOAL_OUTER_RADIUS = GOAL_RADIUS + 10;
    private static final float CLOCK_MARGIN = 50;
    private static final float CLOCK_TEXT_SIZE = 40;
    private static final float GOAL_SPEED_MAX = 0.5f;
    private static final float GOAL_SPEED_MIN = 0.1f;
    private static final float GOAL_SPEED_CHANGE_ANGLE = (float) Math.PI / 4;
    private static final float PAUSED_TEXT_SIZE = 40;

    private static final String SAVED_X = "x";
    private static final String SAVED_Y = "y";
    private static final String SAVED_ANIMAL_ANGLE = "mAnimalAngle";
    private static final String SAVED_DX = "dx";
    private static final String SAVED_DY = "dy";
    private static final String SAVED_GOAL_X = "goalX";
    private static final String SAVED_GOAL_Y = "goalY";
    private static final String SAVED_GOAL_SPEED_X = "goalSpeedX";
    private static final String SAVED_GOAL_SPEED_Y = "goalSpeedY";
    private static final String SAVED_ACCIDENT = "mAccident";
    private static final String SAVED_ACCIDENTS_TIME = "mAccidentsTime";


    private final SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady;

    private Bitmap mBackground;
    private Bitmap mScaledBackground;
    private Bitmap mAnimal;
    private Paint mGoalPaint;
    private Paint mClockPaint;
    private Paint mPausedPaint;
    private String mPausedText;

    private int mCanvasWidth;
    private int mCanvasHeight;

    private long mLastTimestamp;
    private float mGravity[];
    private float mGeomagnetic[];
    private FallerSensorListener mSensorListener;
    private long mAccidentsTime;

    private boolean mDecreaseAcceleration = false;
    private float mResistanceScaleFactor = RESISTANCE_SCALE_FACTOR;
    private float mAccelerationScaleFactor = ACCELERATION_SCALE_FACTOR;

    private float x;
    private float y;
    private float mAnimalAngle;
    private float dx;
    private float dy;
    private float goalX;
    private float goalY;
    private float goalSpeedX;
    private float goalSpeedY;
    private boolean mAccident;
    private Random mRandom;
    private boolean mPaused;
    private boolean mSurfaceSizeObtained;
    private CountDownLatch mCountDown;
    Matrix mAnimalTransformationMatrix;

    public FallerDrawer(Context ctx, SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mBackground = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.faller_background);
        mAnimal = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.animal);
        mAnimalTransformationMatrix = new Matrix();
        mGoalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGoalPaint.setARGB(255, 200, 200, 0);
        mGoalPaint.setStrokeWidth(4);
        mGoalPaint.setStyle(Paint.Style.STROKE);
        mClockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClockPaint.setARGB(255, 0, 0, 0);
        mClockPaint.setTextSize(CLOCK_TEXT_SIZE);
        mPausedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPausedPaint.setARGB(255, 255, 255, 255);
        mPausedPaint.setTextSize(PAUSED_TEXT_SIZE);
        mGravity = new float[3];
        mGeomagnetic = new float[3];
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new FallerSensorListener(sensorManager, this);
        mRandom = new Random();
        mPausedText = ctx.getResources().getString(R.string.faller_paused_text);
        mSurfaceSizeObtained = false;
        mCountDown = new CountDownLatch(1);
    }

    public void pause() {
        mPaused = true;
        mSensorListener.pause();
    }

    public void switchPause() {
        if (mPaused) {
            mSensorListener.unpause();
            mPaused = false;
        } else
            pause();
    }

    public Bundle saveState(Bundle map) {
        if (map != null)
            synchronized (mSurfaceHolder) {
                map.putFloat(SAVED_X, x);
                map.putFloat(SAVED_Y, y);
                map.putFloat(SAVED_ANIMAL_ANGLE, mAnimalAngle);
                map.putFloat(SAVED_DX, dx);
                map.putFloat(SAVED_DY, dy);
                map.putFloat(SAVED_GOAL_X, goalX);
                map.putFloat(SAVED_GOAL_Y, goalY);
                map.putFloat(SAVED_GOAL_SPEED_X, goalSpeedX);
                map.putFloat(SAVED_GOAL_SPEED_Y, goalSpeedY);
                map.putBoolean(SAVED_ACCIDENT, mAccident);
                map.putLong(SAVED_ACCIDENTS_TIME, mAccidentsTime);
            }
        return map;
    }

    public void restoreState(Bundle map) {
        synchronized (mSurfaceHolder) {
            x = map.getFloat(SAVED_X);
            y = map.getFloat(SAVED_Y);
            mAnimalAngle = map.getFloat(SAVED_ANIMAL_ANGLE);
            dx = map.getFloat(SAVED_DX);
            dy = map.getFloat(SAVED_DY);
            goalX = map.getFloat(SAVED_GOAL_X);
            goalY = map.getFloat(SAVED_GOAL_Y);
            goalSpeedX = map.getFloat(SAVED_GOAL_SPEED_X);
            goalSpeedY = map.getFloat(SAVED_GOAL_SPEED_Y);
            mAccident = map.getBoolean(SAVED_ACCIDENT);
            mAccidentsTime = map.getLong(SAVED_ACCIDENTS_TIME);
            mCountDown.countDown();
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        long currentTimestamp;
        float[] rotation = new float[9];
        float[] currentOrientation;
        try { /* we wait until the canvas size is obtained */
            mCountDown.await();
        } catch (InterruptedException e) {
            return;
        }
        mSensorListener.setNeeded(true);
        Thread sensorListenerThread = new Thread(mSensorListener);
        sensorListenerThread.start();
        while (mSurfaceReady) {
            canvas = null;
            currentTimestamp = System.currentTimeMillis();
            try {
                canvas = mSurfaceHolder.lockCanvas();
                if (!mPaused) {
                    synchronized (this) {
                        SensorManager.getRotationMatrix(rotation, null, mGravity, mGeomagnetic);
                    }
                    currentOrientation = FallerMath.applyMatrix(FallerMath.transposeMatrix(rotation, 3, 3),
                            new float[]{0, 0, 1});
                    synchronized (mSurfaceHolder) {
                        updateSituation(currentOrientation, currentTimestamp);
                    }
                }
                redraw(canvas);
            } finally {
                if (canvas != null)
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
            mLastTimestamp = currentTimestamp;
        }
        mSensorListener.setNeeded(false);
        boolean notJoined = true;
        while (notJoined)
            try {
                sensorListenerThread.join();
                notJoined = false;
            } catch (InterruptedException ex) {
                /* ignore */
            }
    }

    private void updateSituation(float[] orientation, long timestamp) {
        long elapsed = timestamp - mLastTimestamp;
        float accelerationX = -orientation[0] * mAccelerationScaleFactor;
        /* We do not minus Y orientation because screen Y coordinates start with 0 on the top instead of the bottom. */
        float accelerationY = orientation[1] * mAccelerationScaleFactor;
        if (mDecreaseAcceleration) {
            accelerationX = FallerMath.decreaseAbsValue(accelerationX);
            accelerationY = FallerMath.decreaseAbsValue(accelerationY);
        }
//        { /* make it more similar to each other */
//            float absAccelerationX = abs(accelerationX);
//            float absAccelerationY = abs(accelerationY);
//            if (absAccelerationX < absAccelerationY)
//                accelerationY = copySign(absAccelerationX + absAccelerationY, accelerationY);
//            else
//                accelerationX = copySign(absAccelerationX + absAccelerationY, accelerationX);
//        }
        float accelerationAngle = findAngle(accelerationX, accelerationY);
        mAnimalAngle = normAngle(accelerationAngle + 90);
        { /* introduce resistance */
            accelerationX -= mResistanceScaleFactor * dx;
            accelerationY -= mResistanceScaleFactor * dy;
        }
        dx += accelerationX * elapsed;
        dy += accelerationY * elapsed;
        x += dx * elapsed;
        y += dy * elapsed;
        boolean[] tmp = new boolean[1];
        x = putIntoRange(x, 0, mCanvasWidth, tmp);
        if (tmp[0])
            dx = 0;
        y = putIntoRange(y, 0, mCanvasHeight, tmp);
        if (tmp[0])
            dy = 0;
        if (mRandom.nextBoolean() && mRandom.nextBoolean()) {
            float angle = (mRandom.nextBoolean() ? -1 : 1) * mRandom.nextFloat() * GOAL_SPEED_CHANGE_ANGLE;
            float sinus = sin(angle), cosinus = cos(angle);
            float a = goalSpeedX, b = goalSpeedY;
            goalSpeedX = a * cosinus - b * sinus;
            goalSpeedY = b * cosinus + a * sinus;
        }
        goalX += goalSpeedX * elapsed;
        goalY += goalSpeedY * elapsed;
        goalX = putIntoRange(goalX, 0, mCanvasWidth, null);
        goalY = putIntoRange(goalY, 0, mCanvasHeight, null);
        mAccident = sqr(x - goalX) + sqr(y - goalY) < sqr(GOAL_RADIUS);
        if (mAccident)
            mAccidentsTime += elapsed;
    }

    private void redraw(Canvas canvas) {
        if (mAccident)
            canvas.drawRGB(255, 0, 0);
        else
            canvas.drawRGB(100, 0, 0);
        canvas.drawBitmap(mScaledBackground, 0, 0, null);
        canvas.drawCircle(goalX, goalY, GOAL_RADIUS, mGoalPaint);
        canvas.drawLine(goalX, goalY - GOAL_OUTER_RADIUS, goalX, goalY + GOAL_OUTER_RADIUS, mGoalPaint);
        canvas.drawLine(goalX - GOAL_OUTER_RADIUS, goalY, goalX + GOAL_OUTER_RADIUS, goalY, mGoalPaint);
        mAnimalTransformationMatrix.setRotate(mAnimalAngle, mAnimal.getWidth() / 2, mAnimal.getHeight() / 2);
        canvas.save();
        canvas.translate(x - mAnimal.getWidth() / 2, y - mAnimal.getHeight() / 2);
        canvas.drawBitmap(mAnimal, mAnimalTransformationMatrix, null);
        canvas.restore();
        String clockText = Integer.toString(Math.round(mAccidentsTime) / 1000);
        Rect bounds = new Rect();
        mClockPaint.getTextBounds(clockText, 0, clockText.length(), bounds);
        canvas.drawText(clockText, mCanvasWidth - bounds.width() - CLOCK_MARGIN, CLOCK_MARGIN, mClockPaint);
        if (mPaused) {
            mPausedPaint.getTextBounds(mPausedText, 0, mPausedText.length(), bounds);
            canvas.drawText(mPausedText, (mCanvasWidth - bounds.width()) / 2, (mCanvasHeight - bounds.height()) / 2,
                    mPausedPaint);
        }
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
        if (!mSurfaceSizeObtained) {
            /* signal the first configuration */
            mSurfaceSizeObtained = true;
            synchronized (mSurfaceHolder) {
                x = mRandom.nextInt(mCanvasWidth);
                y = mRandom.nextInt(mCanvasHeight);
                goalX = mRandom.nextInt(mCanvasWidth);
                goalY = mRandom.nextInt(mCanvasHeight);
                goalSpeedX = 0;
                goalSpeedY = mRandom.nextFloat() * (GOAL_SPEED_MAX - GOAL_SPEED_MIN) + GOAL_SPEED_MIN;
                mPaused = true;
            }
            mCountDown.countDown();
        }
    }

    public void setGravity(float[] newGravity) {
        mGravity = newGravity;
    }

    public void setGeomagnetic(float[] newGeomagnetic) {
        mGeomagnetic = newGeomagnetic;
    }

    private float findAngle(float a, float b) {
        return (float) (atan2(b, a) * 180 / PI);
    }

    private float normAngle(float alpha) {
        alpha = alpha % 360;
        if (alpha < 0)
            alpha += 360;
        return alpha;
    }
}