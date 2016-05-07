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

public class GameMainView extends SurfaceView {
    private GameHandler gameHandler;
    private long lastPaint;

    public GameMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        new Thread(new Painter()).start();
        gameHandler = LoginActivity.lastGameHandler;
    }

    private class Painter implements Runnable {
        @Override
        public void run() {
            while (true) {
                Canvas cnvs = getHolder().lockCanvas();
                if (cnvs != null) {
                    cnvs.drawColor(Color.WHITE);
                    if (gameHandler.hasGameData()) {
                        Node[] nodes = gameHandler.getNodes().toArray(new Node[0]);
                        UnitGroup[] groups = gameHandler.getUnitGroups().toArray(new UnitGroup[0]);
                        for (Node node : nodes) {
                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setColor(GameColors.getColorForOwner(node.getOwner()));
                            cnvs.drawCircle(convertX(node.getX()), convertY(node.getY()), convertSize(GameConstants.NODE_RADIUS), paint);
                            drawNodeUnits(cnvs, node);
                        }
                        if (selectedNode != null) {
                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(convertSize(GameConstants.OUTLINE_SIZE));
                            paint.setColor(0xFFFFAFAF);
                            cnvs.drawCircle(convertX(selectedNode.getX()), convertY(selectedNode.getY()), convertSize(GameConstants.NODE_RADIUS), paint);
                        }
                        for (UnitGroup group : groups) {
                            int owner = group.getOwner();
                            drawUnitGroup(cnvs, group.getX(), group.getY(), owner, group.getUnits(), getGroupSeed(group.getSource(), owner), getGroupSeed(group.getDest(), owner), group.getProgress());
                        }
                        if(!gameHandler.isInProgress()) {
                            Paint paint = new Paint();
                            paint.setTextAlign(Paint.Align.CENTER);
                            paint.setTextSize(convertSize(GameConstants.GAMEREADY_TEXT_SIZE));
                            paint.setColor(Color.RED);
                            cnvs.drawText("Game starting soon", convertX(GameConstants.FIELD_SIZE/2), convertY(GameConstants.FIELD_SIZE/2), paint);
                        }
                    }
                    else {
                        selectedNode = null;
                        String winner = gameHandler.getLastWinner();
                        if(winner != null) {
                            Paint paint = new Paint();
                            paint.setColor(Color.GRAY);
                            paint.setTextSize(convertSize(GameConstants.WIN_TEXT_SIZE));
                            paint.setTextAlign(Paint.Align.CENTER);
                            cnvs.drawText(winner+" wins!", convertX(GameConstants.FIELD_SIZE/2), convertY(GameConstants.FIELD_SIZE/2), paint);
                        }
                    }
                    long time = System.currentTimeMillis();
                    int fps = ((int) (1000.0 / (time - lastPaint)));
                    Paint paint = new Paint();
                    paint.setTextSize(convertSize(GameConstants.SMALL_TEXT_SIZE));
                    cnvs.drawText(fps+" fps", convertX(0), convertY(GameConstants.SMALL_TEXT_SIZE), paint);
                    lastPaint = time;
                    getHolder().unlockCanvasAndPost(cnvs);
                }
            }
        }
    }

    private float sc;
    private int xoff, yoff;
    private Node selectedNode;

    private int convertSize(double i) {
        updateScale();
        return (int) (i * sc);
    }

    private int convertX(double x) {
        return convertSize(x) + xoff;
    }

    private int convertY(double y) {
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

    private void drawNodeUnits(Canvas cnvs, Node node) {
        List<Integer> owners = node.getUnitOwners();
        for (int i = 0; i < owners.size(); i++) {
            int owner = owners.get(i);
            int seed = getGroupSeed(node, owner);
            drawUnitGroup(cnvs, node.getX(), node.getY(), owner, node.getUnits(owner), seed, seed, 0);
        }
    }

    private void drawUnitGroup(Canvas cnvs, double x, double y, int owner, int units, int seed1, int seed2, double progress) {
        Random rand1 = new Random(seed1);
        Random rand2 = new Random(seed2);
        int radius = convertSize(GameConstants.UNIT_RADIUS);
        Paint paint = new Paint();
        paint.setColor(GameColors.darken(GameColors.getColorForOwner(owner)));
        Paint outline = new Paint();
        outline.setStyle(Paint.Style.STROKE);
        outline.setColor(Color.BLACK);
        for (int i = 0; i < units; i++) {
            Point coords1 = getRandomUnitCoords(rand1, x, y);
            Point coords2 = getRandomUnitCoords(rand2, x, y);
            int unitX = (int) (coords1.x + (coords2.x - coords1.x) * progress);
            int unitY = (int) (coords1.y + (coords2.y - coords1.y) * progress);
            cnvs.drawCircle(unitX, unitY, radius, outline);
            cnvs.drawCircle(unitX, unitY, radius, paint);
        }
    }

    private Point getRandomUnitCoords(Random rand, double x, double y) {
        double angle = rand.nextDouble() * 2 * Math.PI;
        double distance = Math.sqrt(rand.nextDouble()) * GameConstants.UNIT_MAX_DISTANCE * GameConstants.NODE_RADIUS;
        return new Point(convertX(distance * Math.cos(angle) + x), convertY(distance * Math.sin(angle) + y));
    }

    private int getGroupSeed(Node node, int owner) {
        List<Node> nodes = gameHandler.getNodes();
        if (nodes != null) {
            return nodes.indexOf(node) * (owner + 1);
        } else {
            return owner;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean tr = super.onTouchEvent(event);
        System.out.println(event.getAction());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Node node = getNodeUnder(event.getX(), event.getY());
            if (node == selectedNode) {
                selectedNode = null;
            } else if (selectedNode == null) {
                if (node.getUnits(gameHandler.getPosition()) > 0) {
                    selectedNode = node;
                }
            }
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (selectedNode != null) {
                Node node = getNodeUnder(event.getX(), event.getY());
                if (selectedNode != node) {
                    gameHandler.attack(node, selectedNode);
                    selectedNode = null;
                }
            }
        }
        return tr;
    }

    private Node getNodeUnder(float x, float y) {
        List<Node> nodes = gameHandler.getNodes();
        Node tr = null;
        if (nodes != null) {
            for (Node node : nodes) {
                if (MathHelper.distance((int) x, (int) y, convertX(node.getX()), convertY(node.getY())) <= convertSize(GameConstants.NODE_RADIUS)) {
                    tr = node;
                }
            }
        }
        return tr;
    }
}
