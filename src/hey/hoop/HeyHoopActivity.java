package hey.hoop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.animal.Animal;
import hey.hoop.animal.Kangaroo;
import hey.hoop.chartdroid.IntentConstants;
import hey.hoop.provider.DataForChartProvider;
import hey.hoop.services.ServiceManager;

public class HeyHoopActivity extends Activity {
    private static final int DIALOG_CHARTDROID_DOWNLOAD = 0;
    private static final int DIALOG_START_WALK = 1;
    private static final int DIALOG_STOP_WALK = 2;
    private static final String TAG = "Hey Hoop";

    private Animal animal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.viewEntriesButton)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewEntriesIntent = new Intent(
                                HeyHoopActivity.this, ListDbActivity.class);
                        startActivity(viewEntriesIntent);
                    }
                });
        findViewById(R.id.openChartButton)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openChartIntent = new Intent(Intent.ACTION_VIEW,
                                DataForChartProvider.PROVIDER_URI);
                        openChartIntent.addCategory(IntentConstants.CATEGORY_XY_CHART);
                        if (Market.isIntentAvailable(HeyHoopActivity.this,
                                openChartIntent))
                            startActivity(openChartIntent);
                        else
                            showDialog(DIALOG_CHARTDROID_DOWNLOAD);
                    }
                });
        animal = new Kangaroo(this, (ImageView) findViewById(R.id.animalWindow));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.food) {
            animal.feed(Animal.Meal.BREAKFAST);
            return true;
        } else if (id == R.id.bed) {
            animal.putToBed();
            return true;
        } else if (id == R.id.walk) {
            configWalk();
            return true;
        } else
            return super.onOptionsItemSelected(item);
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
                                    startActivity(Market
                                            .getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
                                }
                            })
                    .setNeutralButton(R.string.chartdroid_download_web,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Market.APK_DOWNLOAD_URI_CHARTDROID));
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
                                    ServiceManager
                                            .startAndRegisterListener(HeyHoopActivity.this);
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
                                    ServiceManager
                                            .stopAndUnregisterListener(HeyHoopActivity.this);
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

    private void configWalk() {
        boolean isWalkRunning = ServiceManager.isListenerRunning(this);
        Log.d(TAG, "is walk running? " + isWalkRunning);
        if (isWalkRunning)
            showDialog(DIALOG_STOP_WALK);
        else
            showDialog(DIALOG_START_WALK);
    }
}