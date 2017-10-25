package xyz.jienan.checked;

import android.graphics.Color;

import java.security.InvalidParameterException;

/**
 * Created by Jienan on 2017/10/23.
 */

public class ColorUtils {

    public static int getStatusColor(String status) {
        if (Status.Ongoing.toString().equalsIgnoreCase(status)) {
            return Color.GREEN;
        } else if (Status.Pending.toString().equalsIgnoreCase(status)) {
            return Color.YELLOW;
        } else if (Status.Completed.toString().equalsIgnoreCase(status)) {
            return Color.GRAY;
        }
        return Color.BLACK;
    }

    public static int blendColor(int[] colors, float progress, float... positionRatios) {
        int len = colors.length;
        int fromColor = colors[0];
        int toColor = colors[1];

        if (positionRatios != null && positionRatios.length > 0) {
            if (len != positionRatios.length) {
                throw new InvalidParameterException("Should pass in n ratios, n is the length of colors");
            }
            int currentIndex = 0;
            for (int i = 0; i < positionRatios.length; i++) {
                if (positionRatios[i] > progress) {
                    currentIndex = i;
                    break;
                }
                currentIndex = i;
            }
            fromColor = colors[currentIndex - 1];
            toColor = colors[currentIndex];
            float progressConverted =  (progress - positionRatios[currentIndex - 1]) /
                    (positionRatios[currentIndex] - positionRatios[currentIndex - 1]);
            int[] fromColorA = generateArgbArray(fromColor);
            int[] toColorA = generateArgbArray(toColor);

            int a = (int)(fromColorA[0] - (fromColorA[0] - toColorA[0]) * converterA(progressConverted));
            int r = (int)(fromColorA[1] - (fromColorA[1] - toColorA[1]) * converterA(progressConverted));
            int g = (int)(fromColorA[2] - (fromColorA[2] - toColorA[2]) * converterA(progressConverted));
            int b = (int)(fromColorA[3] - (fromColorA[3] - toColorA[3]) * converterA(progressConverted));

            return Color.argb(a,r,g,b);
        }

        return 0xFFFFFFFF;
    }

    /**
     * apply a converter to make the transition have longer retention on both of the endpoints.
     * the curve is in the shape of "∫" between 0 and 1
     * @param x
     * @return
     */
    public static double converterA(float x) {
        return 1 / (1 + Math.pow(10000,(-x+0.5)));
    }

    /**
     * apply a converter to make the transition quickly raise on the left endpoint.
     * the curve is in the shape of "/¯¯" between 0 and 1
     * @param x
     * @return
     */
    public static double converterB(float x) {
        return Math.min(5 * x, 1);
    }


    private static int[] generateArgbArray(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color & 0x00FF0000) >> 16;
        int g = (color & 0x0000FF00) >> 8;
        int b = (color & 0x000000FF);
        return new int[]{a,r,g,b};
    }
}
