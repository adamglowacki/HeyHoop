package hey.hoop.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import hey.hoop.HHDbAdapter;
import hey.hoop.R;
import hey.hoop.animal.Animal;
import hey.hoop.chartdroid.ColumnSchema;

public class DataForChartProvider extends ContentProvider {

    static final String AUTHORITY = "hey.hoop.provider";

    public static final String WALK = "walk", FOOD = "food", DRINK = "drink";
    private static final String TYPE_PARAMETER = "type";
    public static final Uri WALK_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
            .appendQueryParameter("type", WALK).build();
    public static final Uri FOOD_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
            .appendQueryParameter("type", FOOD).build();
    public static final Uri DRINK_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
            .appendQueryParameter("type", DRINK).build();

    public HHDbAdapter dbAdapter;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        return ColumnSchema.PlotData.CONTENT_TYPE_PLOT_DATA;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        dbAdapter = new HHDbAdapter(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Resources r = getContext().getResources();
        String type = uri.getQueryParameter(TYPE_PARAMETER);
        if (WALK.equals(type))
            return queryWalk(uri, r);
        else if (FOOD.equals(type))
            return queryFood(uri, r);
        else if (DRINK.equals(type))
            return queryDrink(uri, r);
        else
            return null;
    }

    private Cursor queryWalk(Uri uri, Resources r) {
        String aspect = uri
                .getQueryParameter(ColumnSchema.DATASET_ASPECT_PARAMETER);
        if (ColumnSchema.Aspect.DATASET_ASPECT_AXES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Axes.COLUMN_AXIS_LABEL});
            c.newRow().add(0)
                    .add(r.getString(R.string.chart_acceleration_x_axis_label));
            c.newRow().add(1)
                    .add(r.getString(R.string.chart_acceleration_y_axis_label));
            return c;
        } else if (ColumnSchema.Aspect.DATASET_ASPECT_SERIES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Series.COLUMN_SERIES_LABEL});
            c.newRow().add(0)
                    .add(r.getString(R.string.chart_acceleration_title));
            return c;
        } else {
            /* data */
            MatrixCursor mc = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Data.COLUMN_AXIS_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_SERIES_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_VALUE,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_LABEL});
            dbAdapter.open(false);
            Cursor c = dbAdapter.fetchWalk();
            int row_index = 0;
            while (c.moveToNext()) {
                mc.newRow().add(row_index).add(ColumnSchema.Y_AXIS_INDEX)
                        .add(0).add(c.getFloat(1)).add(null);
                ++row_index;
                mc.newRow().add(row_index).add(ColumnSchema.X_AXIS_INDEX)
                        .add(0).add(c.getInt(2)).add(null);
                ++row_index;
            }
            dbAdapter.close();
            return mc;
        }
    }

    private Cursor queryFood(Uri uri, Resources r) {
        String aspect = uri.getQueryParameter(ColumnSchema.DATASET_ASPECT_PARAMETER);
        if (ColumnSchema.Aspect.DATASET_ASPECT_AXES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Axes.COLUMN_AXIS_LABEL});
            c.newRow().add(0).add(r.getString(R.string.chart_food_x_axis_label));
            c.newRow().add(1).add(r.getString(R.string.chart_food_y_axis_label));
            return c;
        } else if (ColumnSchema.Aspect.DATASET_ASPECT_SERIES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Series.COLUMN_SERIES_LABEL});
            c.newRow().add(0).add(r.getString(R.string.chart_food_breakfast_title));
            c.newRow().add(1).add(r.getString(R.string.chart_food_dinner_title));
            c.newRow().add(2).add(r.getString(R.string.chart_food_supper_title));
            return c;
        } else {
            /* data */
            MatrixCursor mc = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Data.COLUMN_AXIS_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_SERIES_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_VALUE,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_LABEL});
            dbAdapter.open(false);
            Cursor c = dbAdapter.fetchFood();
            int row_index = 0;
            while (c.moveToNext()) {
                int mealType;
                switch (Animal.Meal.valueOf(c.getString(1))) {
                    case BREAKFAST:
                        mealType = 0;
                        break;
                    case DINNER:
                        mealType = 1;
                        break;
                    case SUPPER:
                        mealType = 2;
                        break;
                    default:
                        mealType = 0;
                }
                mc.newRow().add(row_index).add(ColumnSchema.Y_AXIS_INDEX).add(mealType).add(1.0 + mealType).add(null);
                ++row_index;
                mc.newRow().add(row_index).add(ColumnSchema.X_AXIS_INDEX).add(mealType).add(c.getInt(2)).add(null);
                ++row_index;
            }
            dbAdapter.close();
            return mc;
        }
    }

    private Cursor queryDrink(Uri uri, Resources r) {
        String aspect = uri.getQueryParameter(ColumnSchema.DATASET_ASPECT_PARAMETER);
        if (ColumnSchema.Aspect.DATASET_ASPECT_AXES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Axes.COLUMN_AXIS_LABEL});
            c.newRow().add(0).add(r.getString(R.string.chart_drink_x_axis_label));
            c.newRow().add(1).add(r.getString(R.string.chart_drink_y_axis_label));
            return c;
        } else if (ColumnSchema.Aspect.DATASET_ASPECT_SERIES.equals(aspect)) {
            MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Series.COLUMN_SERIES_LABEL});
            c.newRow().add(0).add(r.getString(R.string.chart_drink_water_title));
            c.newRow().add(1).add(r.getString(R.string.chart_drink_carrot_juice_title));
            return c;
        } else {
            /* data */
            MatrixCursor mc = new MatrixCursor(new String[]{BaseColumns._ID,
                    ColumnSchema.Aspect.Data.COLUMN_AXIS_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_SERIES_INDEX,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_VALUE,
                    ColumnSchema.Aspect.Data.COLUMN_DATUM_LABEL});
            dbAdapter.open(false);
            Cursor c = dbAdapter.fetchDrink();
            int row_index = 0;
            while (c.moveToNext()) {
                int drinkType;
                switch (Animal.Drink.valueOf(c.getString(1))) {
                    case WATER:
                        drinkType = 0;
                        break;
                    case CARROT_JUICE:
                        drinkType = 1;
                        break;
                    default:
                        drinkType = 0;
                }
                mc.newRow().add(row_index).add(ColumnSchema.Y_AXIS_INDEX).add(drinkType).add(c.getFloat(2)).add(null);
                ++row_index;
                mc.newRow().add(row_index).add(ColumnSchema.X_AXIS_INDEX).add(drinkType).add(c.getInt(3)).add(null);
                ++row_index;
            }
            dbAdapter.close();
            return mc;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}