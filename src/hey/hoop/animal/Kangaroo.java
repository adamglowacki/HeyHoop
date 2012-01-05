package hey.hoop.animal;

import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;
import hey.hoop.R;

public class Kangaroo implements Animal {
    private Context ctx;
    private ImageView window;

    public Kangaroo(Context ctx, ImageView window) {
        this.ctx = ctx;
        this.window = window;
    }

    @Override
    public void resume() {
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
}
