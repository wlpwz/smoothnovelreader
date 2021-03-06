package org.cryse.novelreader.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.text.TextUtils;

import org.cryse.novelreader.R;

import java.util.Date;
import java.util.Random;

public class ColorUtils {
    public static final int[] IMAGEVIEW_BG_COLORS = new int[]{
            R.color.colorful_bg_1,
            R.color.colorful_bg_2,
            R.color.colorful_bg_3,
            R.color.colorful_bg_4,
            R.color.colorful_bg_5,
            R.color.colorful_bg_6,
            R.color.colorful_bg_7,
            R.color.colorful_bg_8,
            R.color.colorful_bg_9,
            R.color.colorful_bg_10,
            R.color.colorful_bg_11,
            R.color.colorful_bg_12,
            R.color.colorful_bg_13,
            R.color.colorful_bg_14,
            R.color.colorful_bg_15,
            R.color.colorful_bg_16,
            R.color.colorful_bg_17,
            R.color.colorful_bg_18,
            R.color.colorful_bg_19,
            R.color.colorful_bg_20,
            R.color.colorful_bg_21,
            R.color.colorful_bg_22,
            R.color.colorful_bg_23,
            R.color.colorful_bg_24
    };
    public static final int[] TAG_COLOR_RES_IDS = new int[]{
            R.color.md_amber_700,
            R.color.md_red_700,
            R.color.md_green_700,
            R.color.md_blue_700,
            R.color.md_brown_700,
            R.color.md_cyan_700,
            R.color.md_indigo_700,
            R.color.md_purple_700,
            R.color.md_teal_700,
            R.color.md_deep_orange_700,
            R.color.md_blue_grey_700
    };

    public static int[] getRefreshProgressBarColors() {
        return new int[]{
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4};
    }

    public static int[] getPreDefinedBackgroundColors(Context context) {
        return getColorsFromResources(context, IMAGEVIEW_BG_COLORS);
    }

    public static int getPreDefinedBackgroundColor(Resources resources, int index) {
        if(index >= IMAGEVIEW_BG_COLORS.length)
            index = 0;
        return getColor(resources, IMAGEVIEW_BG_COLORS[index]);
    }

    public static int getPreDefinedColorFromId(Resources resources, String idString, int offset) {
        long colorBase;
        if (!TextUtils.isEmpty(idString) && TextUtils.isDigitsOnly(idString)) {
            colorBase = Long.parseLong(idString) + offset;
        } else if (!TextUtils.isEmpty(idString)) {
            colorBase = idString.hashCode();
        } else {
            Random random = new Random(System.currentTimeMillis());
            colorBase = random.nextLong();
        }
        if(colorBase < 0) colorBase = -colorBase;
        return getPreDefinedBackgroundColor(resources, (int) (colorBase % IMAGEVIEW_BG_COLORS.length));
    }

    public static int getRandomPreDefinedColor(Resources resources) {
        Random random = new Random((new Date()).getTime());
        return getPreDefinedBackgroundColor(resources, random.nextInt(IMAGEVIEW_BG_COLORS.length));
    }

    public static int getSortedPreDefinedColor(Resources resources, int position) {
        return getColor(resources, TAG_COLOR_RES_IDS[position % TAG_COLOR_RES_IDS.length]);
    }

    public static int getColorFromAttr(Context context, int attr) {
        int[] textSizeAttr = new int[] { attr };
        TypedArray a = context.obtainStyledAttributes(textSizeAttr);
        int color = a.getColor(0, Color.RED);
        a.recycle();
        return color;
    }

    public static int getColor(Resources resources, @ColorRes int resId) {
        return getColor(resources, resId, null);
    }

    public static int getColor(Resources resources, @ColorRes int resId, Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(resId, theme);
        } else {
            return resources.getColor(resId);
        }
    }

    private static int[] getColorsFromResources(Context context, int[] resIds) {
        Resources resources = context.getResources();
        int count = resIds.length;
        int[] colors = new int[count];
        for(int i = 0; i < count; i++) {
            colors[i] = resources.getColor(resIds[i]);
        }
        return colors;
    }

    public static String colorToHexString(int color) {
        return String.format("#%08X", color);
    }
}