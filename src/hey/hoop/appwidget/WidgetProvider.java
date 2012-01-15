package hey.hoop.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import hey.hoop.R;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        remoteViews.setImageViewResource(R.id.animalWindow, R.drawable.kangaroo_normal);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
