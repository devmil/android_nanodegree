package de.devmil.nanodegree_spotifystreamer.uicomponents;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class RectangleLayout extends LinearLayout {

    private boolean forceRectangle = true;

    public RectangleLayout(Context context) {
        super(context);
    }

    public RectangleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectangleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setForceRectangle(boolean forceRectangle) {
        this.forceRectangle = forceRectangle;
    }

    public boolean isForceRectangle() {
        return forceRectangle;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(forceRectangle) {
            int min = Math.min(getMeasuredWidth(), getMeasuredHeight());
            setMeasuredDimension(min, min);
        }
    }
}
