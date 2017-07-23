package ru.ponyhawks.android.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

import ru.ponyhawks.android.R;

/**
 * Created at 01:31 on 30/03/17
 *
 * @author cab404
 */
public class NotificationDrawerDrawable extends DrawerArrowDrawable {
    /**
     * @param context used to get the configuration for the drawable from
     */
    public NotificationDrawerDrawable(Context context) {
        super(context);
        padding *= context.getResources().getDisplayMetrics().density;
        radius *= context.getResources().getDisplayMetrics().density;

        TypedArray color = context.obtainStyledAttributes(new int[]{R.attr.notifColor});
        setCircleColor(color.getColor(0, -1));
        color.recycle();
    }

    private boolean showCircle = false;
    private int circleColor;
    private float padding = 12;
    private float radius = 4;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);


    public void setPadding(float padding) {
        this.padding = padding;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setShowCircle(boolean showCircle) {
        this.showCircle = showCircle;
        paint.setStyle(Paint.Style.FILL);
        invalidateSelf();
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        paint.setColor(0xff000000 | circleColor);
        if (showCircle)
            canvas.drawCircle(
                    canvas.getClipBounds().exactCenterX() + padding,
                    canvas.getClipBounds().exactCenterY() - padding,
                    radius,
                    paint
            );
    }
}
