package fictionalpancake.turbospork.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import fictionalpancake.turbospork.GameConstants;
import fictionalpancake.turbospork.GameHandler;
import fictionalpancake.turbospork.Node;

public class GameMainView extends SurfaceView implements SurfaceHolder.Callback {
    private GameHandler gameHandler;

    public GameMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        new Thread(new Painter()).start();
        gameHandler = LoginActivity.lastGameHandler;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private class Painter implements Runnable {
        @Override
        public void run() {
            while (true) {
                Canvas cnvs = getHolder().lockCanvas();
                if (cnvs != null) {
                    cnvs.drawColor(Color.WHITE);
                    if (gameHandler.hasGameData()) {
                        List<Node> nodes = gameHandler.getNodes();
                        for (Node node : nodes) {
                            Paint paint = new Paint();
                            paint.setColor(GameColors.getColorForOwner(node.getOwner()));
                            cnvs.drawCircle(convertX(node.getX()), convertY(node.getY()), convertSize(GameConstants.NODE_RADIUS), paint);
                        }
                    }
                    getHolder().unlockCanvasAndPost(cnvs);
                }
            }
        }
    }

    private float sc, xoff, yoff;

    private float convertSize(float i) {
        updateScale();
        return i*sc;
    }

    private float convertX(float x) {
        return convertSize(x) + xoff;
    }

    private float convertY(float y) {
        return convertSize(y) + yoff;
    }

    private void updateScale() {
        Rect frame = getHolder().getSurfaceFrame();
        int width = frame.width();
        int height = frame.height();
        if (width > height) {
            xoff = (width - height) / 2;
            yoff = 0;
            sc = ((float) height) / GameConstants.FIELD_SIZE;
        } else {
            xoff = 0;
            yoff = (height - width) / 2;
            sc = ((float) width) / GameConstants.FIELD_SIZE;
        }
    }
}
