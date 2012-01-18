package hey.hoop.faller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import hey.hoop.R;

public class FallerActivity extends Activity {
    private static final int REQUEST_CODE_PREFERENCES = 0xada5;
    FallerSurfaceView mSurfaceView;
    FallerDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faller);
        setTitle(R.string.faller_title);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSurfaceView = (FallerSurfaceView) findViewById(hey.hoop.R.id.faller_view);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mDrawer = mSurfaceView.getDrawer();
        mDrawer.configure(sharedPref, getResources());
        if (savedInstanceState != null)
            mDrawer.restoreState(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.pause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.faller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.faller_preferences_menu_item) {
            Intent launchPreferences = new Intent(this, FallerPreferences.class);
            startActivityForResult(launchPreferences, REQUEST_CODE_PREFERENCES);
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PREFERENCES && mDrawer != null)
            mDrawer.configure(PreferenceManager.getDefaultSharedPreferences(this), getResources());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDrawer.saveState(outState);
    }
}
