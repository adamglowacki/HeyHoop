package hey.hoop.faller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class FallerSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private Context mContext;
    private FallerDrawer mDrawer;

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

    public void pause() {
        mDrawer.pause();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mDrawer.unpause();
        return true;
    }
}
