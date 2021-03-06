package org.cryse.novelreader.util;

import android.content.Context;
import android.text.TextUtils;

public class PreferenceConverter {
    public static final int SCROLL_MODE_FLIP_VERTICAL = 0;
    public static final int SCROLL_MODE_FLIP_HORIZONTAL = 1;
    public static final int SCROLL_MODE_VIEWPAGER_HORIZONTAL = 2;
    public static float getFontSize(Context context, String fontSize) {
        int sizeItem = Integer.parseInt(fontSize);
        return sp2px(context, sizeItem);
    }

    public static float getLineSpacing(String lineSpacing) {
        if (TextUtils.isEmpty(lineSpacing))
            return 1f;
        else {
            String value = lineSpacing;
            if (lineSpacing.endsWith("%"))
                value = value.substring(0, lineSpacing.length() - 1);

            return Float.parseFloat(value) / 100f;
        }
    }

    public static float px2sp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static float sp2px(Context context, float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }

    public static int getScrollMode(String scrollMode) {
        return Integer.parseInt(scrollMode);
    }
}
