package hey.hoop.animal;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.R;

public class Kangaroo implements Animal, View.OnTouchListener {
    private Context ctx;
    private ImageView window;
    private static final long STROKE_MINIMUM_TIME = 1000L;

    public Kangaroo(Context ctx, ImageView window) {
        this.ctx = ctx;
        this.window = window;
    }

    @Override
    public void resume() {
        revertNormal();
        window.setOnTouchListener(this);
    }

    private void revertNormal() {
        window.setImageResource(R.drawable.kangaroo_normal);
    }

    @Override
    public void pause() {
    }

    @Override
    public void feed(Meal meal) {
        Toast toast;
        switch (meal) {
            case BREAKFAST:
                toast = Toast.makeText(ctx, R.string.breakfast_thanks, Toast.LENGTH_LONG);
                break;
            case DINNER:
                toast = Toast.makeText(ctx, R.string.dinner_thanks, Toast.LENGTH_LONG);
                break;
            case SUPPER:
                toast = Toast.makeText(ctx, R.string.supper_thanks, Toast.LENGTH_LONG);
                break;
            default:
                toast = Toast.makeText(ctx, R.string.unknown_food_thanks, Toast.LENGTH_LONG);
        }
        toast.show();
    }

    @Override
    public void putToBed() {
        Toast.makeText(ctx, R.string.bed_thanks, Toast.LENGTH_LONG).show();
    }

    private void changeIfNotAnimation(AnimationDrawable newAnimation) {
        Drawable oldDrawable = window.getDrawable();
        if (oldDrawable == null || !(oldDrawable instanceof AnimationDrawable)
                || !((AnimationDrawable) oldDrawable).isRunning()) {
            window.setImageDrawable(newAnimation);
            newAnimation.start();
        }
    }

    @Override
    public void stroke() {
//        changeIfNotAnimation((AnimationDrawable) ctx.getResources().getDrawable(R.drawable.kangaroo_stroked));
        Drawable oldDrawable = window.getDrawable();
        if (oldDrawable instanceof AnimationDrawable)
            ((AnimationDrawable) oldDrawable).stop();
        AnimationDrawable newAnimation = (AnimationDrawable) ctx.getResources().getDrawable(R.drawable.kangaroo_stroked);
        window.setImageDrawable(newAnimation);
        newAnimation.start();
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
