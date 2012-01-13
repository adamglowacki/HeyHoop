package hey.hoop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.*;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import hey.hoop.animal.Animal;
import hey.hoop.animal.Kangaroo;
import hey.hoop.chartdroid.IntentConstants;
import hey.hoop.provider.DataForChartProvider;
import hey.hoop.services.ServiceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeyHoopActivity extends Activity implements GestureOverlayView.OnGesturePerformedListener {
    private static final int DIALOG_CHARTDROID_DOWNLOAD = 0;
    private static final int DIALOG_START_WALK = 1;
    private static final int DIALOG_STOP_WALK = 2;
    private static final String TAG = "Hey Hoop";

    private String GESTURE_STROKE;
    private String GESTURE_WALK;
    private String GESTURE_BED;

    private Animal mAnimal;
    private GestureLibrary mLibrary;
    private static final double STROKE_FAVOUR_SCORE = 5.0;
    private static final double ACCEPT_SCORE = 1.0;

    private HHDbAdapter dbAdapter;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private static final long REFRESH_WELLBEING_INTERVAL = 5L;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.title);
        Animal.Executable menuInvalidator = new Animal.Executable() {
            @Override
            public void execute() {
                callInvalidateOptionsMenu();
            }
        };
        mAnimal = new Kangaroo(this, (ImageView) findViewById(R.id.animalWindow),
                (ImageView) findViewById(R.id.artifact1), (ImageView) findViewById(R.id.artifact2),
                (ImageView) findViewById(R.id.artifact3), menuInvalidator);
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load())
            finish();
        ((GestureOverlayView) findViewById(R.id.gesturesOverlay)).addOnGesturePerformedListener(this);
        GESTURE_STROKE = getResources().getString(R.string.gesture_stroke);
        GESTURE_WALK = getResources().getString(R.string.gesture_walk);
        GESTURE_BED = getResources().getString(R.string.gesture_bed);
        dbAdapter = new HHDbAdapter(this);
    }

    private void callInvalidateOptionsMenu() {
        try {
            Method m = getClass().getMethod("invalidateOptionsMenu");
            m.invoke(this);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "We are before Honeycomb.");
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimal.resume();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.wellbeing_layout).post(new Runnable() {
                    @Override
                    public void run() {
                        refreshWellbeing();
                    }
                });
            }
        }, REFRESH_WELLBEING_INTERVAL, REFRESH_WELLBEING_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnimal.pause();
        scheduledThreadPoolExecutor.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.sleeping_related_group, true);
        if (mAnimal.isAsleep()) {
            menu.findItem(R.id.food).setVisible(false);
            menu.findItem(R.id.drink).setVisible(false);
            menu.findItem(R.id.bed).setVisible(false);
        } else {
            menu.findItem(R.id.wake).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.breakfast) {
            mAnimal.feed(Animal.Meal.BREAKFAST);
        } else if (id == R.id.dinner) {
            mAnimal.feed(Animal.Meal.DINNER);
        } else if (id == R.id.supper) {
            mAnimal.feed(Animal.Meal.SUPPER);
        } else if (id == R.id.water) {
            mAnimal.drink(Animal.Drink.WATER);
        } else if (id == R.id.carrot_juice) {
            mAnimal.drink(Animal.Drink.CARROT_JUICE);
        } else if (id == R.id.bed) {
            mAnimal.putToBed();
        } else if (id == R.id.wake) {
            mAnimal.wakeUp();
        } else if (id == R.id.walk) {
            configWalk();
        } else if (id == R.id.chartWalk) {
            openWalkChart();
        } else if (id == R.id.walkEntries) {
            Intent viewEntriesIntent = new Intent(HeyHoopActivity.this, ListDbActivity.class);
            startActivity(viewEntriesIntent);
        } else
            return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == DIALOG_CHARTDROID_DOWNLOAD)
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.chartdroid_download_title)
                    .setMessage(R.string.chartdroid_download_message)
                    .setPositiveButton(R.string.chartdroid_download_market,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    startActivity(Market.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
                                }
                            })
                    .setNeutralButton(R.string.chartdroid_download_web,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Market.APK_DOWNLOAD_URI_CHARTDROID));
                                }
                            }).create();
        else if (id == DIALOG_START_WALK)
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.dialog_walk_title)
                    .setMessage(R.string.dialog_walk_message_stopped)
                    .setPositiveButton(R.string.dialog_walk_start,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ServiceManager.startAndRegisterListener(HeyHoopActivity.this);
                                }
                            }).create();
        else if (id == DIALOG_STOP_WALK)
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.dialog_walk_title)
                    .setMessage(R.string.dialog_walk_message_running)
                    .setPositiveButton(R.string.dialog_walk_stop,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ServiceManager.stopAndUnregisterListener(HeyHoopActivity.this);
                                }
                            }).create();
        else
            return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_CHARTDROID_DOWNLOAD) {
            boolean hasAndroidMarket = Market.isIntentAvailable(this, Market
                    .getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
            Log.d(TAG, "has android market? " + hasAndroidMarket);
            dialog.findViewById(android.R.id.button1).setVisibility(
                    hasAndroidMarket ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshWellbeing() {
        findViewById(R.id.food_wellbeing_text).setBackgroundColor(getColorForWellbeing(dbAdapter.howNourished()));
        findViewById(R.id.drink_wellbeing_text).setBackgroundColor(getColorForWellbeing(dbAdapter.howWatered()));
        findViewById(R.id.walk_wellbeing_text).setBackgroundColor(getColorForWellbeing(dbAdapter.howWalked()));
    }

    private int getColorForWellbeing(HHDbAdapter.Wellbeing wellbeing) {
        switch (wellbeing) {
            case GOOD:
                return getResources().getColor(R.color.wellbeing_good);
            case POOR:
                return getResources().getColor(R.color.wellbeing_poor);
            case FATAL:
                return getResources().getColor(R.color.wellbeing_fatal);
            default:
                return Color.WHITE;
        }
    }

    private void configWalk() {
        boolean isWalkRunning = ServiceManager.isListenerRunning(this);
        Log.d(TAG, "is walk running? " + isWalkRunning);
        if (isWalkRunning)
            showDialog(DIALOG_STOP_WALK);
        else
            showDialog(DIALOG_START_WALK);
    }

    private void openWalkChart() {
        Intent openChartIntent = new Intent(Intent.ACTION_VIEW, DataForChartProvider.WALK_URI);
        openChartIntent.addCategory(IntentConstants.CATEGORY_XY_CHART);
        if (Market.isIntentAvailable(HeyHoopActivity.this, openChartIntent))
            startActivity(openChartIntent);
        else
            showDialog(DIALOG_CHARTDROID_DOWNLOAD);
    }

    private Prediction findStrokeGesture(ArrayList<Prediction> predictions) {
        for (Prediction prediction : predictions)
            if (GESTURE_STROKE.equals(prediction.name))
                return prediction;
        return null;
    }

    public void executeGesture(String name) {
        if (GESTURE_STROKE.equals(name))
            mAnimal.stroke();
        else if (GESTURE_BED.equals(name))
            mAnimal.putToBed();
        else if (GESTURE_WALK.equals(name))
            configWalk();
    }

    @Override
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        if (mAnimal.isAsleep())
            mAnimal.wakeUp();
        else {
            ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
            if (predictions.size() > 0) {
                Prediction stroke = findStrokeGesture(predictions);
                if (stroke.score > STROKE_FAVOUR_SCORE)
                    mAnimal.stroke();
                else {
                    Prediction theFirst = predictions.get(0);
                    if (theFirst.score > ACCEPT_SCORE)
                        executeGesture(theFirst.name);
                }
            }
        }
    }
}