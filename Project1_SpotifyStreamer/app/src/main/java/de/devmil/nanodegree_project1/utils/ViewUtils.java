package de.devmil.nanodegree_project1.utils;

import android.content.Context;
import android.util.TypedValue;

public final class ViewUtils {
    private ViewUtils() {

    }

    public static float getPxFromDip(Context context, float dip)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }
}
