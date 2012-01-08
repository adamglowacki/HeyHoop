package hey.hoop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HHDbAdapter {
    private static final String TAG = "HeyHoopDbAdapter";

    private static final String DB_NAME = "heyhoopdb";
    private static final int DB_VERSION = 1;
    private static final String WALK_TABLE_NAME = "acceleration";
    private static final String SIDE_TABLE_NAME = "readings";
    private static final String FOOD_TABLE_NAME = "food";
    private static final String DRINK_TABLE_NAME = "drink";
    public static final String WALK_COLUMN_ID = "_id";
    public static final String WALK_COLUMN_VALUE = "value";
    public static final String WALK_COLUMN_DATE = "date";
    public static final String SIDE_COLUMN_ID = "_id";
    public static final String SIDE_COLUMN_VALUE = "value";
    public static final String FOOD_COLUMN_ID = "_id";
    public static final String FOOD_COLUMN_MEAL = "meal";
    public static final String FOOD_COLUMN_DATE = "date";
    public static final String DRINK_COLUMN_ID = "_id";
    public static final String DRINK_COLUMN_KIND = "kind";
    public static final String DRINK_COLUMN_AMOUNT = "amount";
    public static final String DRINK_COLUMN_DATE = "date";
    private static final String CREATE_WALK_TABLE = "create table " + WALK_TABLE_NAME + " ("
            + WALK_COLUMN_ID + " integer primary key autoincrement,"
            + WALK_COLUMN_VALUE + " real not null,"
            + WALK_COLUMN_DATE + " integer)";
    private static final String CREATE_SIDE_TABLE = "create table " + SIDE_TABLE_NAME + " ("
            + SIDE_COLUMN_ID + " integer primary key autoincrement,"
            + SIDE_COLUMN_VALUE + " real not null)";
    private static final String CREATE_FOOD_TABLE = "create table " + FOOD_TABLE_NAME + " ("
            + FOOD_COLUMN_ID + " integer primary key autoincrement,"
            + FOOD_COLUMN_MEAL + " real not null,"
            + FOOD_COLUMN_DATE + " integer)";
    private static final String CREATE_DRINK_TABLE = "create table " + DRINK_TABLE_NAME + " ("
            + DRINK_COLUMN_ID + " integer primary key autoincrement,"
            + DRINK_COLUMN_KIND + " real not null,"
            + DRINK_COLUMN_AMOUNT + " real not null,"
            + DRINK_COLUMN_DATE + " integer)";

    private DbHelper mDbHelper;
    private Context mCtx;
    private SQLiteDatabase mDb;

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_WALK_TABLE);
            db.execSQL(CREATE_SIDE_TABLE);
            db.execSQL(CREATE_FOOD_TABLE);
            db.execSQL(CREATE_DRINK_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading db from " + oldVersion + " to " + newVersion);
            db.execSQL("drop table " + WALK_TABLE_NAME + " if exists");
            db.execSQL("drop table " + SIDE_TABLE_NAME + " if exists");
            db.execSQL("drop table " + FOOD_TABLE_NAME + " if exists");
            db.execSQL("drop table " + DRINK_TABLE_NAME + " if exists");
            onCreate(db);
        }

    }

    public HHDbAdapter(Context ctx) {
        mCtx = ctx;
    }

    public HHDbAdapter open(boolean forWrite) {
        mDbHelper = new DbHelper(mCtx);
        if (forWrite)
            mDb = mDbHelper.getWritableDatabase();
        else
            mDb = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long insertReading(float value) {
        ContentValues cv = new ContentValues();
        cv.put(SIDE_COLUMN_VALUE, value);
        return mDb.insert(SIDE_TABLE_NAME, null, cv);
    }

    public long registerEating(int meal) {
        ContentValues cv = new ContentValues();
        cv.put(FOOD_COLUMN_MEAL, meal);
        return mDb.insert(FOOD_TABLE_NAME, null, cv);
    }

    public long registerDrinking(int kind, float amount) {
        ContentValues cv = new ContentValues();
        cv.put(DRINK_COLUMN_KIND, kind);
        cv.put(DRINK_COLUMN_AMOUNT, amount);
        return mDb.insert(DRINK_TABLE_NAME, null, cv);
    }

    public void deleteReadings() {
        mDb.delete(SIDE_TABLE_NAME, null, null);
    }

    public void flushReadings() {
        Cursor c = mDb.query(SIDE_TABLE_NAME, new String[]{"AVG("
                + SIDE_COLUMN_VALUE + "), STRFTIME('%s', 'now')"}, null, null, null, null, null);
        if (c.moveToFirst()) {
            float value = c.getFloat(0);
            int currentTime = c.getInt(1);
            ContentValues cv = new ContentValues();
            cv.put(WALK_COLUMN_VALUE, value);
            cv.put(WALK_COLUMN_DATE, currentTime);
            mDb.insert(WALK_TABLE_NAME, null, cv);
        }
        c.close();
    }

    private String getLastDayClause(String fieldName) {
        final String CURRENT_TIME = "STRFTIME('%s', 'now')";
        final int DAY_IN_SECONDS = 24 * 60 * 60;
        return fieldName + " BETWEEN (" + CURRENT_TIME + "-" + DAY_IN_SECONDS + ") AND " + CURRENT_TIME;
    }

    public Cursor fetchWalk() {
        return mDb.query(WALK_TABLE_NAME, new String[]{WALK_COLUMN_ID,
                WALK_COLUMN_VALUE, WALK_COLUMN_DATE}, getLastDayClause(WALK_COLUMN_DATE), null, null, null,
                WALK_COLUMN_DATE + " DESC");
    }

    public Cursor fetchFood() {
        return mDb.query(FOOD_TABLE_NAME, new String[]{FOOD_COLUMN_ID, FOOD_COLUMN_MEAL, FOOD_COLUMN_DATE},
                getLastDayClause(FOOD_COLUMN_DATE), null, null, null, WALK_COLUMN_DATE + " DESC");
    }

    public Cursor fetchDrink() {
        return mDb.query(DRINK_TABLE_NAME, new String[]{DRINK_COLUMN_ID, DRINK_COLUMN_KIND, DRINK_COLUMN_AMOUNT,
                DRINK_COLUMN_DATE}, getLastDayClause(DRINK_COLUMN_DATE), null, null, null, DRINK_COLUMN_DATE + " DESC");
    }
}
