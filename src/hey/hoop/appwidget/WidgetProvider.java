package hey.hoop.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import hey.hoop.HeyHoopActivity;
import hey.hoop.R;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent openHeyHoop = new Intent(context, HeyHoopActivity.class);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openHeyHoop, 0);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            remoteViews.setOnClickPendingIntent(R.id.widget_button, pendingOpen);
            remoteViews.setImageViewResource(R.id.widget_animal_view, R.drawable.kangaroo_normal);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
