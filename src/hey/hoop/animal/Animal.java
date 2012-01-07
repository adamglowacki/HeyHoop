package hey.hoop.animal;

public interface Animal {
    void wakeUp();

    boolean isAsleep();

    public enum Meal {
        BREAKFAST, DINNER, SUPPER
    }
    public enum Drink {
        WATER, CARROT_JUICE
    }

    public interface Executable {
        public void execute();
    }

    public void resume();

    public void pause();

    public void feed(Meal meal);

    public void drink(Drink drink);

    public void putToBed();

    public void stroke();
}
