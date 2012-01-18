package hey.hoop.faller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import hey.hoop.R;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static android.util.FloatMath.cos;
import static android.util.FloatMath.sin;
import static hey.hoop.faller.FallerMath.*;
import static java.lang.Math.min;

public class FallerDrawer implements Runnable {
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
    private static final String SAVED_DECREASE_ACCELERATION = "mDecreaseAcceleration";
    private static final String SAVED_RESISTANCE_SCALE_FACTOR = "mResistanceScaleFactor";
    private static final String SAVED_ACCELERATION_SCALE_FACTOR = "mAccelerationScaleFactor";
    private static final String SAVED_GOAL_ANGLE = "goalAngle";
    private static final String SAVED_GOAL_ANGLE_CHANGE_SPEED = "mGoalAngleChangeSpeed";

    private String mPreferenceGoalNumber;
    private String mPreferenceResistanceScaleFactor;
    private String mPreferenceAccelerationScaleFactor;
    private String mPreferenceGoalAngleChangeSpeed;
    private String mPreferenceGoalSpeedMax;
    private String mPreferenceGoalSpeedMin;
    private String mPreferenceDecreaseAcceleration;

    private final SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady;

    private Bitmap mBackground;
    private Bitmap mScaledBackground;
    private Bitmap mAnimal;
    private Bitmap mGoal;
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
    private int mUsualBackground;
    private int mAccidentBackground;

    private boolean mDecreaseAcceleration = false;
    private float mResistanceScaleFactor;
    private float mAccelerationScaleFactor;
    private int mGoalNumber;
    private float mGoalAngleChangeSpeed;
    private float mGoalSpeedMax;
    private float mGoalSpeedMin;
    private float mClockMargin;
    private float mGoalRadius;

    private float x;
    private float y;
    private float mAnimalAngle;
    private float dx;
    private float dy;
    private float[] goalX;
    private float[] goalY;
    private float[] goalSpeedX;
    private float[] goalSpeedY;
    private float[] goalAngle;
    private boolean mAccident;
    private Random mRandom;
    private boolean mPaused;
    private boolean mSurfaceSizeObtained;
    private CountDownLatch mCountDown;

    public FallerDrawer(Context ctx, SurfaceHolder surfaceHolder) {
        Resources r = ctx.getResources();
        mSurfaceHolder = surfaceHolder;
        mBackground = BitmapFactory.decodeResource(r, R.drawable.faller_background);
        mAnimal = BitmapFactory.decodeResource(r, R.drawable.animal);
        mGoal = BitmapFactory.decodeResource(r, R.drawable.goal);
        mUsualBackground = r.getColor(R.color.faller_usual_background);
        mAccidentBackground = r.getColor(R.color.faller_accident_background);
        mClockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClockPaint.setColor(r.getColor(R.color.faller_clock));
        mClockPaint.setTextSize(r.getDimension(R.dimen.faller_clock_text));
        mPausedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPausedPaint.setColor(r.getColor(R.color.faller_paused));
        mPausedPaint.setTextSize(r.getDimension(R.dimen.faller_paused_text));
        mGravity = new float[3];
        mGeomagnetic = new float[3];
        SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new FallerSensorListener(sensorManager, this);
        mRandom = new Random();
        mPausedText = ctx.getResources().getString(R.string.faller_paused_text);
        mSurfaceSizeObtained = false;
        mGoalRadius = r.getDimension(R.dimen.faller_goal_radius);
        mClockMargin = r.getDimension(R.dimen.faller_clock_margin);
        mPreferenceAccelerationScaleFactor = r.getString(R.string.faller_preference_acceleration_scale_factor);
        mPreferenceResistanceScaleFactor = r.getString(R.string.faller_preference_resistance_scale_factor);
        mPreferenceDecreaseAcceleration = r.getString(R.string.faller_preference_decrease_acceleration);
        mPreferenceGoalNumber = r.getString(R.string.faller_preference_goal_number);
        mPreferenceGoalAngleChangeSpeed = r.getString(R.string.faller_preference_goal_angle_change_speed);
        mPreferenceGoalSpeedMin = r.getString(R.string.faller_preference_goal_speed_min);
        mPreferenceGoalSpeedMax = r.getString(R.string.faller_preference_goal_speed_max);
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
                map.putFloatArray(SAVED_GOAL_X, goalX);
                map.putFloatArray(SAVED_GOAL_Y, goalY);
                map.putFloatArray(SAVED_GOAL_SPEED_X, goalSpeedX);
                map.putFloatArray(SAVED_GOAL_SPEED_Y, goalSpeedY);
                map.putFloatArray(SAVED_GOAL_ANGLE, goalAngle);
                map.putBoolean(SAVED_ACCIDENT, mAccident);
                map.putLong(SAVED_ACCIDENTS_TIME, mAccidentsTime);
                map.putBoolean(SAVED_DECREASE_ACCELERATION, mDecreaseAcceleration);
                map.putFloat(SAVED_RESISTANCE_SCALE_FACTOR, mResistanceScaleFactor);
                map.putFloat(SAVED_ACCELERATION_SCALE_FACTOR, mAccelerationScaleFactor);
                map.putFloat(SAVED_GOAL_ANGLE_CHANGE_SPEED, mGoalAngleChangeSpeed);
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
            goalX = map.getFloatArray(SAVED_GOAL_X);
            goalY = map.getFloatArray(SAVED_GOAL_Y);
            goalSpeedX = map.getFloatArray(SAVED_GOAL_SPEED_X);
            goalSpeedY = map.getFloatArray(SAVED_GOAL_SPEED_Y);
            goalAngle = map.getFloatArray(SAVED_GOAL_ANGLE);
            mGoalNumber = goalX.length;
            assert goalY.length == mGoalNumber;
            assert goalSpeedX.length == mGoalNumber;
            assert goalSpeedY.length == mGoalNumber;
            mAccident = map.getBoolean(SAVED_ACCIDENT);
            mAccidentsTime = map.getLong(SAVED_ACCIDENTS_TIME);
            mDecreaseAcceleration = map.getBoolean(SAVED_DECREASE_ACCELERATION);
            mResistanceScaleFactor = map.getFloat(SAVED_RESISTANCE_SCALE_FACTOR);
            mAccelerationScaleFactor = map.getFloat(SAVED_ACCELERATION_SCALE_FACTOR);
            mGoalAngleChangeSpeed = map.getFloat(SAVED_GOAL_ANGLE_CHANGE_SPEED);
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

    /* Assuming that the object is pointing currently (0, -1) and wants to be (vectorX, vectorY). */
    private float getDesiredRotationAngle(float vectorX, float vectorY) {
        float desiredAngle = findAngle(vectorX, vectorY);
        return normAngle(desiredAngle + 90);
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
        mAnimalAngle = getDesiredRotationAngle(accelerationX, accelerationY);
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
        mAccident = false;
        for (int i = 0; i < mGoalNumber; ++i) {
            if (mRandom.nextBoolean() && mRandom.nextBoolean()) {
                float angle = (mRandom.nextBoolean() ? -1 : 1) * mRandom.nextFloat() * mGoalAngleChangeSpeed;
                float sine = sin(angle), cosine = cos(angle);
                float a = goalSpeedX[i], b = goalSpeedY[i];
                goalSpeedX[i] = a * cosine - b * sine;
                goalSpeedY[i] = b * cosine + a * sine;
                goalAngle[i] = getDesiredRotationAngle(goalSpeedX[i], goalSpeedY[i]);
            }
            goalX[i] += goalSpeedX[i] * elapsed;
            goalY[i] += goalSpeedY[i] * elapsed;
            goalX[i] = putIntoRange(goalX[i], 0, mCanvasWidth, null);
            goalY[i] = putIntoRange(goalY[i], 0, mCanvasHeight, null);
            mAccident |= sqr(x - goalX[i]) + sqr(y - goalY[i]) < sqr(mGoalRadius);
        }
        if (mAccident)
            mAccidentsTime += elapsed;
    }

    private void redraw(Canvas canvas) {
        if (mAccident)
            canvas.drawColor(mAccidentBackground);
        else
            canvas.drawColor(mUsualBackground);
        canvas.drawBitmap(mScaledBackground, 0, 0, null);
        for (int i = 0; i < mGoalNumber; ++i)
            drawPlane(canvas, goalX[i], goalY[i], goalAngle[i], mGoal);
        drawPlane(canvas, x, y, mAnimalAngle, mAnimal);
        String clockText = Integer.toString(Math.round(mAccidentsTime) / 1000);
        Rect bounds = new Rect();
        mClockPaint.getTextBounds(clockText, 0, clockText.length(), bounds);
        canvas.drawText(clockText, mCanvasWidth - bounds.width() - mClockMargin, mClockMargin, mClockPaint);
        if (mPaused) {
            mPausedPaint.getTextBounds(mPausedText, 0, mPausedText.length(), bounds);
            canvas.drawText(mPausedText, (mCanvasWidth - bounds.width()) / 2, (mCanvasHeight - bounds.height()) / 2,
                    mPausedPaint);
        }
    }

    private void drawPlane(Canvas canvas, float planeX, float planeY, float planeAngle, Bitmap image) {
        canvas.save();
        Matrix rotation = new Matrix();
        rotation.setRotate(planeAngle, image.getWidth() / 2, image.getHeight() / 2);
        canvas.translate(planeX - image.getWidth() / 2, planeY - image.getHeight() / 2);
        canvas.drawBitmap(image, rotation, null);
        canvas.restore();
    }

    public void setSurfaceReady(boolean ready) {
        mSurfaceReady = ready;
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;
            mScaledBackground = Bitmap.createScaledBitmap(mBackground, mCanvasWidth, mCanvasHeight, true);
            if (!mSurfaceSizeObtained) {
                /* signal the first configuration */
                mSurfaceSizeObtained = true;
                generateRandomSituation();
                mPaused = true;
                mCountDown.countDown();
            }
        }
    }

    public void setGravity(float[] newGravity) {
        mGravity = newGravity;
    }

    public void setGeomagnetic(float[] newGeomagnetic) {
        mGeomagnetic = newGeomagnetic;
    }

    public void configure(SharedPreferences sharedPref, Resources r) {
        synchronized (mSurfaceHolder) {
            mGoalNumber = Integer.valueOf(sharedPref.getString(mPreferenceGoalNumber,
                    r.getString(R.string.faller_default_goal_number)));
            mDecreaseAcceleration = sharedPref.getBoolean(mPreferenceDecreaseAcceleration, false);
            mResistanceScaleFactor = sharedPref.getFloat(mPreferenceResistanceScaleFactor,
                    r.getDimension(R.dimen.faller_resistance_scale));
            mAccelerationScaleFactor = sharedPref.getFloat(mPreferenceAccelerationScaleFactor,
                    r.getDimension(R.dimen.faller_acceleration_scale));
            mGoalAngleChangeSpeed = sharedPref.getFloat(mPreferenceGoalAngleChangeSpeed,
                    r.getDimension(R.dimen.faller_goal_angle_speed_change));
            mGoalSpeedMin = sharedPref.getFloat(mPreferenceGoalSpeedMin,
                    r.getDimension(R.dimen.faller_goal_speed_min));
            mGoalSpeedMax = sharedPref.getFloat(mPreferenceGoalSpeedMax,
                    r.getDimension(R.dimen.faller_goal_speed_max));
            if (mSurfaceSizeObtained && mGoalNumber > goalX.length) {
                float[] oldGoalX = goalX, oldGoalY = goalY;
                float[] oldGoalSpeedX = goalSpeedX, oldGoalSpeedY = goalSpeedY;
                float[] oldGoalAngle = goalAngle;
                generateRandomSituation();
                copyArrayPrefix(oldGoalX, goalX);
                copyArrayPrefix(oldGoalY, goalY);
                copyArrayPrefix(oldGoalSpeedX, goalSpeedX);
                copyArrayPrefix(oldGoalSpeedY, goalSpeedY);
                copyArrayPrefix(oldGoalAngle, goalAngle);
            }
        }
    }

    private void generateRandomSituation() {
        x = mRandom.nextInt(mCanvasWidth);
        y = mRandom.nextInt(mCanvasHeight);
        goalX = new float[mGoalNumber];
        goalY = new float[mGoalNumber];
        goalSpeedX = new float[mGoalNumber];
        goalSpeedY = new float[mGoalNumber];
        goalAngle = new float[mGoalNumber];
        for (int i = 0; i < mGoalNumber; ++i) {
            goalX[i] = mRandom.nextInt(mCanvasWidth);
            goalY[i] = mRandom.nextInt(mCanvasHeight);
            goalSpeedX[i] = 0;
            goalSpeedY[i] = mRandom.nextFloat() * (mGoalSpeedMax - mGoalSpeedMin) + mGoalSpeedMin;
        }
    }

    private void copyArrayPrefix(float[] in, float[] out) {
        System.arraycopy(in, 0, out, 0, min(in.length, out.length));
    }
}