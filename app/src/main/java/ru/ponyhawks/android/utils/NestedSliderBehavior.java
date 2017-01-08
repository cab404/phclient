package ru.ponyhawks.android.utils;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:36 on 08/01/17
 *
 * @author cab404
 */
public class NestedSliderBehavior extends BottomSheetBehavior {
    public NestedSliderBehavior() {
    }

    public NestedSliderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        if (event.getY() < child.getBottom())
            return super.onTouchEvent(parent, child, event);
        else
            return false;
    }

}
