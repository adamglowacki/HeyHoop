package hey.hoop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import hey.hoop.animal.Animal;

import java.util.HashMap;
import java.util.Map;

public class HHDbAdapter {
    private static final String TAG = "HeyHoopDbAdapter";

    private static final String DB_NAME = "heyhoopdb";
    private static final int DB_VERSION = 1;
    private static final String WALK_TABLE_NAME = "acceleration";
    private static final String SIDE_TABLE_NAME = "readings";
    private static final String FOOD_TABLE_NAME = "food";
    private static final String DRINK_TABLE_NAME = "drink";
    private static final String BOOL_TABLE_NAME = "bool";
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
    public static final String BOOL_COLUMN_ID = "_id";
    public static final String BOOL_COLUMN_NAME = "name";
    private static final String CREATE_WALK_TABLE = "create table " + WALK_TABLE_NAME + " ("
            + WALK_COLUMN_ID + " integer primary key autoincrement,"
            + WALK_COLUMN_VALUE + " real not null,"
            + WALK_COLUMN_DATE + " integer)";
    private static final String CREATE_SIDE_TABLE = "create table " + SIDE_TABLE_NAME + " ("
            + SIDE_COLUMN_ID + " integer primary key autoincrement,"
            + SIDE_COLUMN_VALUE + " real not null)";
    private static final String CREATE_FOOD_TABLE = "create table " + FOOD_TABLE_NAME + " ("
            + FOOD_COLUMN_ID + " integer primary key autoincrement,"
            + FOOD_COLUMN_MEAL + " varchar not null,"
            + FOOD_COLUMN_DATE + " integer)";
    private static final String CREATE_DRINK_TABLE = "create table " + DRINK_TABLE_NAME + " ("
            + DRINK_COLUMN_ID + " integer primary key autoincrement,"
            + DRINK_COLUMN_KIND + " varchar not null,"
            + DRINK_COLUMN_AMOUNT + " real not null,"
            + DRINK_COLUMN_DATE + " integer)";
    private static final String CREATE_BOOL_TABLE = "create table " + BOOL_TABLE_NAME + " ("
            + BOOL_COLUMN_ID + " integer primary key autoincrement,"
            + BOOL_COLUMN_NAME + " varchar)";

    /**
     * Whether an animal is asleep.
     */
    public static final String ZZZ_BOOL = "zzz";
    /**
     * Iff it is switched on then the walk tracker starts when system startup.
     */
    public static final String INSTALLED_BOOL = "installed";

    public static final int DRINK_GOOD_ABOVE = 10;
    public static final int DRINK_POOR_ABOVE = 6;
    public static final int MEAL_GOOD_KINDS_MIN = 3;
    public static final int MEAL_GOOD_COUNT_MIN = 4;
    public static final int MEAL_POOR_COUNT_MIN = 2;
    private static final float WALK_GOOD_ABOVE = 2 * 60 * 60 * 2;
    private static final float WALK_POOR_ABOVE = 0.5f * 60 * 60 * 2;
    private static final float WALK_DROP_BELOW = 0.25f;

    public static final String CURRENT_TIME_CLAUSE = "STRFTIME('%s', 'now')";

    private DbHelper mDbHelper;
    private Context mCtx;
    private SQLiteDatabase mDb;

    public enum Wellbeing {
        FATAL, POOR, GOOD
    }

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
            db.execSQL(CREATE_BOOL_TABLE);
        }

        private void dropTable(String tableName, SQLiteDatabase db) {
            db.execSQL("drop table " + tableName + " if exists");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading db from " + oldVersion + " to " + newVersion);
            dropTable(WALK_TABLE_NAME, db);
            dropTable(SIDE_TABLE_NAME, db);
            dropTable(FOOD_TABLE_NAME, db);
            dropTable(DRINK_TABLE_NAME, db);
            dropTable(BOOL_TABLE_NAME, db);
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

    public void registerEating(String meal) {
        mDb.execSQL("INSERT INTO " + FOOD_TABLE_NAME + " (" + FOOD_COLUMN_MEAL + ", "
                + FOOD_COLUMN_DATE + ") VALUES ('" + meal + "', " + CURRENT_TIME_CLAUSE + ")");
    }

    public void registerDrinking(String kind, float amount) {
        mDb.execSQL("INSERT INTO " + DRINK_TABLE_NAME + " (" + DRINK_COLUMN_KIND + ", " + DRINK_COLUMN_AMOUNT + ", "
                + DRINK_COLUMN_DATE + ") VALUES ('" + kind + "', " + amount + ", " + CURRENT_TIME_CLAUSE + ")");
    }

    public void deleteReadings() {
        mDb.delete(SIDE_TABLE_NAME, null, null);
    }

    public void flushReadings() {
        Cursor c = mDb.query(SIDE_TABLE_NAME, new String[]{"AVG("
                + SIDE_COLUMN_VALUE + "), " + CURRENT_TIME_CLAUSE}, null, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                float value = c.getFloat(0);
                int currentTime = c.getInt(1);
                ContentValues cv = new ContentValues();
                cv.put(WALK_COLUMN_VALUE, value);
                cv.put(WALK_COLUMN_DATE, currentTime);
                mDb.insert(WALK_TABLE_NAME, null, cv);
            }
        } finally {
            c.close();
        }
    }

    private String getLastDayClause(String fieldName) {
        final int DAY_IN_SECONDS = 24 * 60 * 60;
        return fieldName + " BETWEEN (" + CURRENT_TIME_CLAUSE + "-" + DAY_IN_SECONDS + ") AND " + CURRENT_TIME_CLAUSE;
    }

    public Cursor fetchWalk() {
        return mDb.query(WALK_TABLE_NAME, new String[]{WALK_COLUMN_ID, WALK_COLUMN_VALUE, WALK_COLUMN_DATE},
                getLastDayClause(WALK_COLUMN_DATE), null, null, null, WALK_COLUMN_DATE + " DESC");
    }

    public Cursor fetchFood() {
        return mDb.query(FOOD_TABLE_NAME, new String[]{FOOD_COLUMN_ID, FOOD_COLUMN_MEAL, FOOD_COLUMN_DATE},
                getLastDayClause(FOOD_COLUMN_DATE), null, null, null, WALK_COLUMN_DATE + " DESC");
    }

    public Cursor fetchDrink() {
        return mDb.query(DRINK_TABLE_NAME, new String[]{DRINK_COLUMN_ID, DRINK_COLUMN_KIND, DRINK_COLUMN_AMOUNT,
                DRINK_COLUMN_DATE}, getLastDayClause(DRINK_COLUMN_DATE), null, null, null, DRINK_COLUMN_DATE + " DESC");
    }

    public Wellbeing howNourished() {
        Map<Animal.Meal, Integer> meals = new HashMap<Animal.Meal, Integer>(Animal.Meal.values().length);
        Cursor c = mDb.query(FOOD_TABLE_NAME, new String[]{FOOD_COLUMN_MEAL, "COUNT(*)"},
                getLastDayClause(FOOD_COLUMN_DATE), null, FOOD_COLUMN_MEAL, null, null);
        try {
            while (c.moveToNext())
                meals.put(Animal.Meal.valueOf(c.getString(0)), c.getInt(1));
        } finally {
            c.close();
        }
        int mealKinds = meals.keySet().size();
        int mealCount = 0;
        for (int x : meals.values())
            mealCount += x;
        if (mealKinds >= MEAL_GOOD_KINDS_MIN && mealCount >= MEAL_GOOD_COUNT_MIN)
            return Wellbeing.GOOD;
        else if (meals.keySet().size() >= MEAL_POOR_COUNT_MIN)
            return Wellbeing.POOR;
        else
            return Wellbeing.FATAL;
    }

    public Wellbeing howWatered() {
        int total = 0;
        Cursor c = mDb.query(DRINK_TABLE_NAME, new String[]{"SUM(" + DRINK_COLUMN_AMOUNT + ")"},
                getLastDayClause(DRINK_COLUMN_DATE), null, null, null, null);
        try {
            c.moveToNext();
            total = c.getInt(0);
        } finally {
            c.close();
        }
        if (total > DRINK_GOOD_ABOVE)
            return Wellbeing.GOOD;
        else if (total > DRINK_POOR_ABOVE)
            return Wellbeing.POOR;
        else
            return Wellbeing.FATAL;
    }

    public Wellbeing howWalked() {
        float integrata = 0;
        float lastVal = -1, currentVal;
        long lastTime = -1, currentTime;
        Cursor c = fetchWalk();
        try {
            while (c.moveToNext()) {
                currentVal = c.getFloat(1);
                currentTime = c.getLong(2);
                if (lastTime > 0 && currentVal >= WALK_DROP_BELOW)
                    integrata += (currentTime - lastTime) * (currentVal + lastVal) / 2;
                lastVal = currentVal;
                lastTime = currentTime;
            }
        } finally {
            c.close();
        }
        if (integrata > WALK_GOOD_ABOVE)
            return Wellbeing.GOOD;
        else if (integrata > WALK_POOR_ABOVE)
            return Wellbeing.POOR;
        else
            return Wellbeing.FATAL;
    }

    public boolean isBool(String boolName) {
        Cursor c = mDb.query(BOOL_TABLE_NAME, new String[]{BOOL_COLUMN_ID}, BOOL_COLUMN_NAME + " = ?",
                new String[]{boolName}, null, null, null);
        try {
            return c.moveToNext();
        } finally {
            c.close();
        }
    }

    public void setBool(String boolName) {
        ContentValues cv = new ContentValues();
        cv.put(BOOL_COLUMN_NAME, boolName);
        mDb.insert(BOOL_TABLE_NAME, null, cv);
    }

    public void unsetBool(String boolName) {
        mDb.delete(BOOL_TABLE_NAME, BOOL_COLUMN_NAME + " = ?", new String[]{boolName});
    }
}