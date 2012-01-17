package hey.hoop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import hey.hoop.animal.Animal;
import hey.hoop.animal.Kangaroo;
import hey.hoop.chartdroid.IntentConstants;
import hey.hoop.custom_view.WellbeingStatusView;
import hey.hoop.faller.FallerActivity;
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

    private ScheduledThreadPoolExecutor wellbeingRefreshing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
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
        WellbeingStatusView foodStatus = (WellbeingStatusView) findViewById(R.id.food_status);
        foodStatus.setText(R.string.food_wellbeing);
        foodStatus.setFetchWellbeing(new WellbeingStatusView.FetchWellbeing() {
            @Override
            public HHDbAdapter.Wellbeing fetch(HHDbAdapter dbAdapter) {
                return dbAdapter.howNourished();
            }
        });
        WellbeingStatusView drinkStatus = (WellbeingStatusView) findViewById(R.id.drink_status);
        drinkStatus.setText(R.string.drink_wellbeing);
        drinkStatus.setFetchWellbeing(new WellbeingStatusView.FetchWellbeing() {
            @Override
            public HHDbAdapter.Wellbeing fetch(HHDbAdapter dbAdapter) {
                return dbAdapter.howWatered();
            }
        });
        WellbeingStatusView walkStatus = (WellbeingStatusView) findViewById(R.id.walk_status);
        walkStatus.setText(R.string.walk_wellbeing);
        walkStatus.setFetchWellbeing(new WellbeingStatusView.FetchWellbeing() {
            @Override
            public HHDbAdapter.Wellbeing fetch(HHDbAdapter dbAdapter) {
                return dbAdapter.howWalked();
            }
        });
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
        /* Wellbeing status refresh */
        final long REFRESH_INTERVAL = getResources().getInteger(R.integer.activity_wellbeing_refresh_interval);
        final long INITIAL_DELAY = 500;
        mAnimal.resume(this);
        wellbeingRefreshing = new ScheduledThreadPoolExecutor(1);
        wellbeingRefreshing.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.wellbeing_layout).post(new Runnable() {
                    @Override
                    public void run() {
                        refreshWellbeing();
                    }
                });
            }
        }, INITIAL_DELAY, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnimal.pause();
        wellbeingRefreshing.shutdown();
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
            mAnimal.putToBed(this);
        } else if (id == R.id.wake) {
            mAnimal.wakeUp(this);
        } else if (id == R.id.walk) {
            configWalk();
        } else if (id == R.id.chartWalk || id == R.id.chartDrink || id == R.id.chartFood) {
            openChart(id);
        } else if (id == R.id.walkEntries) {
            Intent viewEntriesIntent = new Intent(HeyHoopActivity.this, ListDbActivity.class);
            startActivity(viewEntriesIntent);
        } else if (id == R.id.openFaller) {
            Intent fallerIntent = new Intent(HeyHoopActivity.this, FallerActivity.class);
            startActivity(fallerIntent);
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
                    .setPositiveButton(R.string.chartdroid_download_market, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(Market.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
                        }
                    })
                    .setNeutralButton(R.string.chartdroid_download_web, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Market.APK_DOWNLOAD_URI_CHARTDROID));
                        }
                    }).create();
        else if (id == DIALOG_START_WALK)
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.dialog_walk_title)
                    .setMessage(R.string.dialog_walk_message_stopped)
                    .setPositiveButton(R.string.dialog_walk_start, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ServiceManager.startAndRegisterListener(HeyHoopActivity.this);
                        }
                    }).create();
        else if (id == DIALOG_STOP_WALK)
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.dialog_walk_title)
                    .setMessage(R.string.dialog_walk_message_running)
                    .setPositiveButton(R.string.dialog_walk_stop, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ServiceManager.stopAndUnregisterListener(HeyHoopActivity.this);
                        }
                    }).create();
        else
            return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_CHARTDROID_DOWNLOAD) {
            boolean hasAndroidMarket = Market.isIntentAvailable(this,
                    Market.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
            Log.d(TAG, "has android market? " + hasAndroidMarket);
            dialog.findViewById(android.R.id.button1).setVisibility(
                    hasAndroidMarket ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshWellbeing() {
        ((WellbeingStatusView) findViewById(R.id.food_status)).refetch();
        ((WellbeingStatusView) findViewById(R.id.drink_status)).refetch();
        ((WellbeingStatusView) findViewById(R.id.walk_status)).refetch();
    }

    private void configWalk() {
        boolean isWalkRunning = ServiceManager.isListenerRunning(this);
        Log.d(TAG, "is walk running? " + isWalkRunning);
        if (isWalkRunning)
            showDialog(DIALOG_STOP_WALK);
        else
            showDialog(DIALOG_START_WALK);
    }

    private void openChart(int menuItemId) {
        Uri providerUri;
        if (menuItemId == R.id.chartWalk)
            providerUri = DataForChartProvider.WALK_URI;
        else if (menuItemId == R.id.chartDrink)
            providerUri = DataForChartProvider.DRINK_URI;
        else /* only food can be */
            providerUri = DataForChartProvider.FOOD_URI;
        Intent openChartIntent = new Intent(Intent.ACTION_VIEW, providerUri);
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
            mAnimal.putToBed(this);
        else if (GESTURE_WALK.equals(name))
            configWalk();
    }

    @Override
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        if (mAnimal.isAsleep())
            mAnimal.wakeUp(this);
        else {
            ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
            if (predictions.size() > 0) {
                Prediction stroke = findStrokeGesture(predictions);
                if (stroke != null && stroke.score > STROKE_FAVOUR_SCORE)
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