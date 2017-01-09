package ru.ponyhawks.android.fragments;


import android.support.v4.app.Fragment;
import android.text.Editable;

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
