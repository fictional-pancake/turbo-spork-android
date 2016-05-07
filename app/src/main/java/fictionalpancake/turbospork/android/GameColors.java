package fictionalpancake.turbospork.android;

import android.graphics.Color;

import java.util.Random;

public class GameColors {
    public static int[] COLORS = {
            Color.GREEN,
            Color.BLUE,
            Color.RED,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            0xFFFFC800
    };

    public static int getColorForOwner(int owner) {
        if(owner < 0) {
            return Color.LTGRAY;
        }
        else if(owner < COLORS.length) {
            return COLORS[owner];
        }
        else {
            return 0xFF000000+new Random(owner).nextInt(0xFFFFFF);
        }
    }

    public static int darken(int color) {
        return Color.argb(Color.alpha(color), ((int) (Color.red(color) * 0.7)), ((int) (Color.green(color) * 0.7)), ((int) (Color.blue(color) * 0.7)));
    }
}
