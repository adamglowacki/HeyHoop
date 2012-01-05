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
    private static final String MAIN_TABLE_NAME = "acceleration";
    private static final String SIDE_TABLE_NAME = "readings";
    public static final String MAIN_COLUMN_ID = "_id";
    public static final String MAIN_COLUMN_VALUE = "value";
    public static final String MAIN_COLUMN_DATE = "date";
    public static final String SIDE_COLUMN_ID = "_id";
    public static final String SIDE_COLUMN_VALUE = "value";
    private static final String CREATE_MAIN_TABLE = "create table " + MAIN_TABLE_NAME + " ("
            + MAIN_COLUMN_ID + " integer primary key autoincrement,"
            + MAIN_COLUMN_VALUE + " real not null,"
            + MAIN_COLUMN_DATE + " integer)";
    private static final String CREATE_SIDE_TABLE = "create table " + SIDE_TABLE_NAME + " ("
            + SIDE_COLUMN_ID + " integer primary key autoincrement,"
            + SIDE_COLUMN_VALUE + " real not null)";

    private DbHelper mDbHelper;
    private Context mCtx;
    private SQLiteDatabase mDb;

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_MAIN_TABLE);
            db.execSQL(CREATE_SIDE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading db from " + oldVersion + " to " + newVersion);
            db.execSQL("drop table " + MAIN_TABLE_NAME + " if exists");
            db.execSQL("drop table " + SIDE_TABLE_NAME + " if exists");
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

    public void deleteReadings() {
        mDb.delete(SIDE_TABLE_NAME, null, null);
    }

    public void flushReadings() {
        // Cursor p = mDb.query(SIDE_TABLE_NAME, new String[] { SIDE_COLUMN_ID,
        // SIDE_COLUMN_VALUE }, null, null, null, null, null);
        // while (p.moveToNext())
        // Log.i(TAG,
        // Integer.toString(p.getInt(0)) + ": "
        // + Float.toString(p.getFloat(1)));
        Cursor c = mDb.query(SIDE_TABLE_NAME, new String[]{"AVG("
                + SIDE_COLUMN_VALUE + "), STRFTIME('%s', 'now')"}, null, null, null, null, null);
        if (c.moveToFirst()) {
            float value = c.getFloat(0);
            int currentTime = c.getInt(1);
            ContentValues cv = new ContentValues();
            cv.put(MAIN_COLUMN_VALUE, value);
            cv.put(MAIN_COLUMN_DATE, currentTime);
            mDb.insert(MAIN_TABLE_NAME, null, cv);
        }
        c.close();
    }

    public Cursor fetchEntries() {
        final String CURRENT_TIME = "STRFTIME('%s', 'now')";
        final int DAY_IN_SECONDS = 24 * 60 * 60;
        return mDb.query(MAIN_TABLE_NAME, new String[]{MAIN_COLUMN_ID,
                MAIN_COLUMN_VALUE, MAIN_COLUMN_DATE}, MAIN_COLUMN_DATE
                + " BETWEEN (" + CURRENT_TIME + " - " + DAY_IN_SECONDS + ") AND "
                + CURRENT_TIME, null, null, null, MAIN_COLUMN_DATE + " DESC");
    }
}
