package hey.hoop.faller;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FallerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Context mContext;
    private FallerDrawer mDrawer;

    public FallerSurfaceView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        mDrawer = new FallerDrawer(ctx, surfaceHolder);
    }

    public FallerDrawer getDrawer() {
        return mDrawer;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mDrawer.setSurfaceReady(true);
        mDrawer.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mDrawer.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean running = true;
        mDrawer.setSurfaceReady(false);
        while (running) {
            try {
                mDrawer.join();
                running = false;
            } catch (InterruptedException e) { /* ignore */ }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        /* ... */
    }

    public void pause() {
        mDrawer.pause();
    }

    public void resume() {
        mDrawer.resume();
    }
}
