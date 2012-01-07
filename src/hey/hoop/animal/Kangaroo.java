package hey.hoop.animal;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.R;

import java.util.Date;
import java.util.Random;

public class Kangaroo implements Animal, View.OnTouchListener {
    private Context ctx;
    private ImageView window;
    private ImageView artifact1;
    private Random random;
    private Executable onStateChange;

    @Override
    public boolean isAsleep() {
        return asleep;
    }

    private boolean asleep;

    public Kangaroo(Context ctx, ImageView window, ImageView artifact1, Executable onStateChange) {
        this.ctx = ctx;
        this.window = window;
        this.artifact1 = artifact1;
        this.onStateChange = onStateChange;
        random = new Random(new Date().getTime());
        asleep = false;
    }

    @Override
    public void resume() {
        refresh();
        window.setOnTouchListener(this);
    }

    private void refresh() {
        if (asleep)
            window.setImageResource(R.drawable.kangaroo_zzz);
        else
            window.setImageResource(R.drawable.kangaroo_normal);
        onStateChange.execute();
    }

    @Override
    public void pause() {
    }

    private void showToast(int msgId) {
        Toast.makeText(ctx, msgId, Toast.LENGTH_LONG).show();
    }

    private void slideIn(int imgId) {
//        TranslateAnimation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 50.0f,
//                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        artifact1.setImageResource(imgId);
        artifact1.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide));
    }

    private void slideRandom(int... imgIds) {
        int selected = random.nextInt(imgIds.length);
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
        Toast.makeText(ctx, R.string.bed_thanks, Toast.LENGTH_LONG).show();
        asleep = true;
        refresh();
    }

    @Override
    public void wakeUp() {
        Toast.makeText(ctx, R.string.bed_hello, Toast.LENGTH_LONG).show();
        asleep = false;
        refresh();
    }

    @Override
    public void stroke() {
        artifact1.setImageResource(R.drawable.drink);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            stroke();
            return true;
        }
        return false;
    }
}
