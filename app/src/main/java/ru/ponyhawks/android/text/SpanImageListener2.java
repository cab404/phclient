package ru.ponyhawks.android.text;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 18:39 on 14/09/15
 *
 * @author cab404
 */
public class SpanImageListener2 implements RequestListener<Drawable> {
    private final ImageSpan span;
    private final TextView target;
    private final Spannable builder;

    public SpanImageListener2(TextView target, ImageSpan span, Spannable builder) {
        this.span = span;
        this.target = target;
        this.builder = builder;
    }

    boolean replace(ImageSpan by) {
        int start = builder.getSpanStart(span);
        int end = builder.getSpanEnd(span);

        if (start == -1)
            return true;

        builder.removeSpan(span);
        builder.setSpan(
                by,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        target.setText(builder);
        target.requestLayout();
        return true;
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        resource.setBounds(0, 0, resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
        return replace(new ImageSpan(resource));
    }
}
