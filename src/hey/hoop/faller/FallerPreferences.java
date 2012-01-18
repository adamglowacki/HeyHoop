package hey.hoop.faller;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import hey.hoop.R;

public class FallerPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.faller_preferences);
    }
}