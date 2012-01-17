package hey.hoop.animal;

import android.content.Context;

public interface Animal {
    boolean isAsleep();

    void wakeUp(Context ctx);

    void putToBed(Context ctx);

    public enum Meal {
        BREAKFAST, DINNER, SUPPER
    }

    public enum Drink {
        WATER, CARROT_JUICE
    }

    public interface Executable {
        public void execute();
    }

    public void resume(Context ctx);

    public void pause();

    public void feed(Meal meal);

    public void drink(Drink drink);

    public void stroke();
}
