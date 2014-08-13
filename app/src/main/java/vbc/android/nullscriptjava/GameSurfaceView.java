package vbc.android.nullscriptjava;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * TODO: document your custom view class.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private CanvasDrawerThread drawerThread;
    private final Context mContext;

    private class CanvasDrawerThread extends Thread {

        private final SurfaceHolder holder;
        private boolean stopSignal;
        private final Object signalLock;

        private int width, height;

        public CanvasDrawerThread(SurfaceHolder holder) {
            this.holder = holder;
            this.stopSignal = false;
            width = height = 1;
            signalLock = new Object();
        }

        @Override
        public void run() {
            while (!stopSignal) {
                synchronized (holder) {
                    Canvas canvas = holder.lockCanvas();
                    canvas.drawColor(getSolidColor());
                    Paint p = new Paint();
                    p.setARGB(255, 128, 128, 128);
                    canvas.drawText("Drawing Test", 0, 0, p);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            stopSignal = false;
        }

        public void signalStop() {
            synchronized (signalLock) {
                stopSignal = true;
            }
        }

        public void setSurfaceSize(int format, int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public GameSurfaceView(Context context) {
        super(context);
        mContext = context;
        initializeView();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initializeView();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initializeView();
    }

    private void initializeView() {
        holder = this.getHolder();
        drawerThread = new CanvasDrawerThread(holder);
        holder.addCallback(this);

        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawerThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawerThread.setSurfaceSize(format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawerThread.signalStop();
        boolean retry = true;
        while (retry) try {
            drawerThread.join();
            retry = false;
        } catch (InterruptedException ignore) {
        }
    }
}
