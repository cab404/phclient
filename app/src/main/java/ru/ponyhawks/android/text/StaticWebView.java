package ru.ponyhawks.android.text;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * View wrapper arounf HtmlRipper
 * Created at 16:07 on 14/09/15
 *
 * @author cab404
 */
public class StaticWebView extends LinearLayout {
    HtmlRipper boundRipper = null;


    public StaticWebView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public StaticWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        final String value = attrs.getAttributeValue(null, "text");
        if (value != null)
            setText(value);
    }

    public void parametrize() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boundRipper.loadImages = sp.getBoolean("loadImages", true);
        boundRipper.loadOnCellular = sp.getBoolean("loadOnCellular", true);
        boundRipper.loadVideos = sp.getBoolean("loadVideos", true);
        boundRipper.textIsSelectable = sp.getBoolean("textSelectable", false);
        boundRipper.displayGifs = sp.getBoolean("displayGifs", true);
        boundRipper.supportSmilepack = sp.getBoolean("supportSmilepack", true);
    }


    public void setRipper(HtmlRipper ripper) {
        this.boundRipper = ripper;
        parametrize();
        ripper.changeLayout(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (boundRipper != null)
            boundRipper.layout();
    }

    public HtmlRipper setText(String text) {
        boundRipper = new HtmlRipper(this);
        parametrize();
        try {
            boundRipper.escape(text);
        } catch (Exception e) {
            Log.e("HtmlRipper", "Had failed to escape " + text, e);
        }
        return boundRipper;
    }

    @Override
    protected void finalize() throws Throwable {
        boundRipper.destroy();
        super.finalize();
    }

}
