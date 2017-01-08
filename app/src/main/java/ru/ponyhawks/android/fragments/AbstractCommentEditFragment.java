package ru.ponyhawks.android.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.changers.TextChanger;
import ru.ponyhawks.android.text.changers.TextPrism;
import ru.ponyhawks.android.utils.HideablePartBehavior;
import ru.ponyhawks.android.utils.IgnorantCoordinatorLayout;
import ru.ponyhawks.android.utils.Meow;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class AbstractCommentEditFragment extends Fragment {

    protected SendCallback sendCallback;

    public abstract Editable getText();

    public abstract void setTarget(CharSequence target);

    public abstract void clear();

    public abstract void setText(CharSequence text);

    public abstract boolean isExpanded();

    public interface SendCallback {
        void onSend(Editable text);
        void onRefresh(boolean force);
    }

    public void setSendCallback(SendCallback callback) {
        sendCallback = callback;
    }

    public abstract void hide();

    public abstract void collapse();

    public abstract void expand();

    public abstract void setCommentCount(int count);
    public abstract void setUpdating(boolean updating);

}
