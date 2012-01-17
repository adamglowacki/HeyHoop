package hey.hoop.faller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import hey.hoop.R;

public class FallerActivity extends Activity {
    FallerSurfaceView mSurfaceView;
    FallerDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faller);
        setTitle(R.string.faller_title);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSurfaceView = (FallerSurfaceView) findViewById(hey.hoop.R.id.faller_view);
        mDrawer = mSurfaceView.getDrawer();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDrawer.saveState(outState);
    }
}
