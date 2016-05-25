package fictionalpancake.turbospork.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;
import java.util.Random;

import fictionalpancake.turbospork.GameConstants;
import fictionalpancake.turbospork.GameHandler;
import fictionalpancake.turbospork.MathHelper;
import fictionalpancake.turbospork.Node;
import fictionalpancake.turbospork.UnitGroup;
import fictionalpancake.turbospork.paint.GraphicsHandler;
import fictionalpancake.turbospork.paint.IPainter;
import fictionalpancake.turbospork.paint.PaintStyle;

public class GameMainView extends SurfaceView {
    private GameHandler gameHandler;
    private GraphicsHandler graphicsHandler;
    private int statusBarHeight;

    public GameMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gameHandler = LoginActivity.lastGameHandler;
        graphicsHandler = new GraphicsHandler(gameHandler);
        int barId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(barId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(barId);
        }
        new Thread(new Painter(graphicsHandler)).start();
    }

    private class Painter implements Runnable, IPainter {
        private Canvas cnvs;
        private GraphicsHandler graphicsHandler;

        public Painter(GraphicsHandler graphicsHandler) {
            this.graphicsHandler = graphicsHandler;
        }

        @Override
        public void run() {
            while (true) {
                cnvs = getHolder().lockCanvas();
                if (cnvs != null) {
                    cnvs.drawColor(Color.WHITE);
                    graphicsHandler.paint(this);
                    getHolder().unlockCanvasAndPost(cnvs);
                }
            }
        }

        @Override
        public int getWidth() {
            return cnvs.getWidth();
        }

        @Override
        public int getHeight() {
            return cnvs.getHeight();
        }

        @Override
        public void drawCircle(PaintStyle paintStyle, int x, int y, int radius) {
            cnvs.drawCircle(x, y, radius, convertPaint(paintStyle));
        }

        @Override
        public void drawText(PaintStyle paintStyle, String s, int x, int y) {
            Paint paint = convertPaint(paintStyle);
            int ay = y;
            if(paintStyle.alignY == PaintStyle.Align.TOP) {
                ay += paintStyle.textSize;
                //ay += statusBarHeight;
            }
            else if(paintStyle.alignY == PaintStyle.Align.BOTTOM) {
                ay -= paint.descent();
            }
            cnvs.drawText(s, x, ay, paint);
        }

        @Override
        public fictionalpancake.turbospork.paint.Point getMousePos() {
            return null;
        }

        private Paint convertPaint(PaintStyle paintStyle) {
            Paint tr = new Paint(Paint.ANTI_ALIAS_FLAG);
            tr.setColor(convertColor(paintStyle.color));
            if (paintStyle.alignX == PaintStyle.Align.CENTER) {
                tr.setTextAlign(Paint.Align.CENTER);
            } else if (paintStyle.alignX == PaintStyle.Align.RIGHT) {
                tr.setTextAlign(Paint.Align.RIGHT);
            }
            tr.setStyle(paintStyle.fill ? Paint.Style.FILL : Paint.Style.STROKE);
            tr.setStrokeWidth(paintStyle.strokeWidth/1.5f);
            tr.setTextSize(paintStyle.textSize);
            return tr;
        }

        private int convertColor(fictionalpancake.turbospork.paint.Color color) {
            return 0xFF000000 + color.getRed() * 0x10000 + color.getGreen() * 0x100 + color.getBlue();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean tr = super.onTouchEvent(event);
        System.out.println(event.getAction());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Node node = getNodeUnder(event.getX(), event.getY());
            graphicsHandler.select(node);
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            Node node = getNodeUnder(event.getX(), event.getY());
            graphicsHandler.attack(node);
        }
        return tr;
    }

    private Node getNodeUnder(float x, float y) {
        fictionalpancake.turbospork.paint.Point point = new fictionalpancake.turbospork.paint.Point(((int) x), ((int) y));
        List<Node> nodes = gameHandler.getNodes();
        Node tr = null;
        if (nodes != null) {
            for (Node node : nodes) {
                if (graphicsHandler.isMouseOverNode(node, point)) {
                    tr = node;
                }
            }
        }
        return tr;
    }
}
