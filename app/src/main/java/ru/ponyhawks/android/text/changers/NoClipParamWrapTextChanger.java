package ru.ponyhawks.android.text.changers;

import android.widget.EditText;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 12:20 on 20/10/15
 *
 * @author cab404
 */
public class NoClipParamWrapTextChanger extends ParamWrapTextChanger {
    public NoClipParamWrapTextChanger(String start, int icon_id, int hint_id) {
        super(start, icon_id, hint_id);
    }

    @Override
    public CharSequence getInitialText() {
        return "";
    }
}
