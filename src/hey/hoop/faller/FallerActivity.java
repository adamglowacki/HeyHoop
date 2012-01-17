package hey.hoop.faller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import hey.hoop.R;

public class FallerActivity extends Activity {
    FallerSurfaceView mSurfaceView;
    FallerDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faller);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSurfaceView = (FallerSurfaceView) findViewById(hey.hoop.R.id.faller_view);
        mDrawer = mSurfaceView.getDrawer();
        if (savedInstanceState != null)
            mDrawer.restoreState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.resume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDrawer.saveState(outState);
    }
}
