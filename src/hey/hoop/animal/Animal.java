package hey.hoop.animal;

public interface Animal {
	public enum Meal {
		BREAKFAST, DINNER, SUPPER
	};

	public void feed(Meal meal);

	public void putToBed();
}
