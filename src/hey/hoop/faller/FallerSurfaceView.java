package hey.hoop.faller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class FallerSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private FallerDrawer mDrawer;
    private Thread mDrawerThread;

    public FallerSurfaceView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        setOnTouchListener(this);
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
        mDrawerThread = new Thread(mDrawer);
        mDrawerThread.start();
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
                mDrawerThread.join();
                running = false;
            } catch (InterruptedException e) { /* ignore */ }
        }
    }

    public void pause() {
        mDrawer.pause();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            mDrawer.switchPause();
            return true;
        }
        return false;
    }
}
