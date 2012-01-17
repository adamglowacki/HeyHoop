package hey.hoop.animal;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.HHDbAdapter;
import hey.hoop.R;
import hey.hoop.appwidget.WidgetProvider;

import java.util.Date;
import java.util.Random;

public class Kangaroo implements Animal {
    private Context mCtx;
    private ImageView mWindow;
    private ImageView mBottomArtifact;
    private ImageView mCentreArtifact1;
    private ImageView mCentreArtifact2;
    private Random mRandom;
    private Executable mOnStateChange;
    private Vibrator mVibrator;
    private HHDbAdapter mDbAdapter;

    private static final long[] WAKE_VIBRATIONS = {0, 300};
    private static final long[] BED_VIBRATIONS = {250, 400, 750, 400, 750, 400};
    private static final long[] STROKE_VIBRATIONS = {0, 200, 100, 200};

    @Override
    public boolean isAsleep() {
        mDbAdapter.open(false);
        try {
            return mDbAdapter.isBool(HHDbAdapter.ZZZ_BOOL);
        } finally {
            mDbAdapter.close();
        }
    }

    public Kangaroo(Context ctx, ImageView window, ImageView artifact1, ImageView artifact2, ImageView artifact3,
                    Executable onStateChange) {
        this.mCtx = ctx;
        this.mWindow = window;
        this.mBottomArtifact = artifact1;
        this.mCentreArtifact1 = artifact2;
        this.mCentreArtifact2 = artifact3;
        this.mOnStateChange = onStateChange;
        mRandom = new Random(new Date().getTime());
        mVibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        mDbAdapter = new HHDbAdapter(ctx);
    }

    @Override
    public void resume(Context ctx) {
        refresh(ctx);
    }

    private void refresh(Context ctx) {
        if (isAsleep())
            mWindow.setImageResource(R.drawable.kangaroo_zzz);
        else
            mWindow.setImageResource(R.drawable.kangaroo_normal);
        mOnStateChange.execute();
        mWindow.invalidate();
        updateWidget(ctx);
    }

    @Override
    public void pause() {
    }

    private void showToast(int msgId) {
        Toast t = Toast.makeText(mCtx, msgId, Toast.LENGTH_LONG);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
    }

    private void slideIn(int imgId) {
        mBottomArtifact.setImageResource(imgId);
        mBottomArtifact.startAnimation(AnimationUtils.loadAnimation(mCtx, R.anim.slide));
    }

    private void flyAway(int imgId) {
        mCentreArtifact1.setImageResource(imgId);
        mCentreArtifact2.setImageResource(imgId);
        Animation fly = AnimationUtils.loadAnimation(mCtx, R.anim.fly);
        Animation flyDelayed = AnimationUtils.loadAnimation(mCtx, R.anim.fly);
        flyDelayed.setStartOffset(750);
        mCentreArtifact1.startAnimation(fly);
        mCentreArtifact2.startAnimation(flyDelayed);
    }

    private void slideRandom(int... imgIds) {
        int selected = mRandom.nextInt(imgIds.length);
        slideIn(imgIds[selected]);
    }

    @Override
    public void feed(Meal meal) {
        switch (meal) {
            case BREAKFAST:
                slideRandom(R.drawable.big_apple, R.drawable.big_apricot, R.drawable.big_hamburger,
                        R.drawable.big_pizza, R.drawable.big_banana, R.drawable.big_cookie);
                showToast(R.string.breakfast_thanks);
                break;
            case DINNER:
                slideRandom(R.drawable.big_aubergine, R.drawable.big_capsicum_1, R.drawable.big_capsicum_2,
                        R.drawable.big_corn, R.drawable.big_tomato);
                showToast(R.string.dinner_thanks);
                break;
            case SUPPER:
                slideRandom(R.drawable.big_apple, R.drawable.big_cherry, R.drawable.big_orange,
                        R.drawable.big_grapes, R.drawable.big_popcorn, R.drawable.big_tomato);
                showToast(R.string.supper_thanks);
                break;
            default:
                showToast(R.string.unknown_food_thanks);
        }
        mDbAdapter.open(true);
        try {
            mDbAdapter.registerEating(meal.name());
        } finally {
            mDbAdapter.close();
        }
    }

    @Override
    public void drink(Drink drink) {
        float amount;
        switch (drink) {
            case WATER:
                slideIn(R.drawable.big_drink);
                showToast(R.string.water_thanks);
                amount = mRandom.nextFloat() / 2 + 1;
                break;
            case CARROT_JUICE:
                slideIn(R.drawable.big_drink);
                showToast(R.string.carrot_juice_thanks);
                amount = mRandom.nextFloat() / 3 + 0.75f;
                break;
            default:
                showToast(R.string.unknown_drink_thanks);
                amount = 1;
        }
        mDbAdapter.open(true);
        try {
            mDbAdapter.registerDrinking(drink.name(), amount);
        } finally {
            mDbAdapter.close();
        }
    }

    private void updateWidget(Context ctx) {
        WidgetProvider widgetProvider = new WidgetProvider();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
        ComponentName componentName = new ComponentName(ctx, WidgetProvider.class);
        widgetProvider.onUpdate(ctx, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName));
    }

    private void setAsleepInDb(boolean toSleep) {
        mDbAdapter.open(true);
        try {
            if (toSleep)
                mDbAdapter.setBool(HHDbAdapter.ZZZ_BOOL);
            else
                mDbAdapter.unsetBool(HHDbAdapter.ZZZ_BOOL);
        } finally {
            mDbAdapter.close();
        }
    }

    @Override
    public void putToBed(Context ctx) {
        mVibrator.vibrate(BED_VIBRATIONS, -1);
        showToast(R.string.bed_thanks);
        setAsleepInDb(true);
        refresh(ctx);

    }

    @Override
    public void wakeUp(Context ctx) {
        mVibrator.vibrate(WAKE_VIBRATIONS, -1);
        showToast(R.string.bed_hello);
        setAsleepInDb(false);
        refresh(ctx);
    }

    @Override
    public void stroke() {
        flyAway(R.drawable.big_watermelon);
        mWindow.startAnimation(AnimationUtils.loadAnimation(mCtx, R.anim.shake));
        mVibrator.vibrate(STROKE_VIBRATIONS, -1);
    }
}