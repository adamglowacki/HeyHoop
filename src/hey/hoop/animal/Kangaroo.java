package hey.hoop.animal;

import android.content.Context;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.R;

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
    private static final long[] WAKE_VIBRATIONS = {0, 300};
    private static final long[] BED_VIBRATIONS = {250, 400, 750, 400, 750, 400};
    private static final long[] STROKE_VIBRATIONS = {0, 200, 100, 200};

    private boolean asleep;

    @Override
    public boolean isAsleep() {
        return asleep;
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
        asleep = false;
        mVibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void resume() {
        refresh();
    }

    private void refresh() {
        if (asleep)
            mWindow.setImageResource(R.drawable.kangaroo_zzz);
        else
            mWindow.setImageResource(R.drawable.kangaroo_normal);
        mOnStateChange.execute();
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
    }

    @Override
    public void drink(Drink drink) {
        switch (drink) {
            case WATER:
                slideIn(R.drawable.big_drink);
                showToast(R.string.water_thanks);
                break;
            case CARROT_JUICE:
                slideIn(R.drawable.big_drink);
                showToast(R.string.carrot_juice_thanks);
                break;
            default:
                showToast(R.string.unknown_drink_thanks);
        }
    }

    @Override
    public void putToBed() {
        mVibrator.vibrate(BED_VIBRATIONS, -1);
        showToast(R.string.bed_thanks);
        asleep = true;
        refresh();
    }

    @Override
    public void wakeUp() {
        mVibrator.vibrate(WAKE_VIBRATIONS, -1);
        showToast(R.string.bed_hello);
        asleep = false;
        refresh();
    }

    @Override
    public void stroke() {
        flyAway(R.drawable.big_watermelon);
        mWindow.startAnimation(AnimationUtils.loadAnimation(mCtx, R.anim.shake));
        mVibrator.vibrate(STROKE_VIBRATIONS, -1);
    }
}