package hey.hoop.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import hey.hoop.HHDbAdapter;
import hey.hoop.HeyHoopActivity;
import hey.hoop.R;

public class WidgetProvider extends AppWidgetProvider {
    public static final String APPWIDGET_UPDATE = "hey.hoop.APPWIDGET_UPDATE";

    private boolean isAsleep(Context ctx) {
        HHDbAdapter dbAdapter = new HHDbAdapter(ctx);
        dbAdapter.open(false);
        try {
            return dbAdapter.isBool(HHDbAdapter.ZZZ_BOOL);
        } finally {
            dbAdapter.close();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, WidgetProvider.class);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(provider));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int imgId = isAsleep(context) ? R.drawable.kangaroo_zzz : R.drawable.kangaroo_zzz;
        for (int appWidgetId : appWidgetIds) {
            Intent openHeyHoop = new Intent(context, HeyHoopActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openHeyHoop, 0);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            remoteViews.setOnClickPendingIntent(R.id.widget_button, pendingOpen);
            remoteViews.setImageViewResource(R.id.widget_animal_view, imgId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
