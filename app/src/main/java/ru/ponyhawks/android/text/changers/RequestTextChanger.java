package ru.ponyhawks.android.text.changers;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.net.URL;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.utils.LineInputDialog;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:12 on 20/10/15
 *
 * @author cab404
 */
public abstract class RequestTextChanger implements TextChanger {

    private Fragment ctx;
    int ss;
    int se;

    public static void insert(EditText ed, String wrapping, int ss, int se, String param) {
        if (ss < 0) ss = -1;
        if (se < 0) se = ss;

        final Editable text = ed.getText();


        String ready = wrapping
                .replace("%", param);

        int insi = ready.indexOf("$");
        int seli = ed.getSelectionStart();
        if (seli == -1) seli = 0;

        ready = ready
                .replace("$", text.subSequence(ss, se));

        text.replace(ss, se, ready);

        if (insi == -1)
            if (ss != se)
                ed.setSelection(seli + ss, seli + ss + ready.length());
            else
                ed.setSelection(seli + ss);
        else
            if (ss != se)
                ed.setSelection(seli + insi, seli + insi + se - ss);
            else
                ed.setSelection(seli + insi);
    }

    @SuppressWarnings("ResourceType")
    @Override
    @SuppressLint("NewApi")
    public void onSelect(Fragment ctx, final EditText editorText) {
        this.ctx = ctx;
        ss = editorText.getSelectionStart();
        se = editorText.getSelectionEnd();



        LineInputDialog dialog = new LineInputDialog(ctx.getActivity());
        dialog.setText(getInitialText() + "");
        dialog.setHint(ctx.getString(getHint()));

        dialog.setOnClick(new LineInputDialog.OnConfirmListener() {
            @Override
            public boolean onConfirm(EditText text) {
                handleText(editorText, ss, se, text.getText().toString());
                System.out.println(text.getText());
                return true;
            }
        });
        dialog.show();
    }

    public abstract void handleText(EditText text, int ss, int se, String s);

    @DrawableRes
    public abstract int getHint();

    @Override
    public void onConfigure(Context ctx) {
    }

    @Override
    public boolean configurable() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @SuppressWarnings("deprecation")
    public CharSequence getInitialText(){
        CharSequence clipboard = null;
        if (Build.VERSION.SDK_INT >= 11) {
            final ClipboardManager cbm =
                    (ClipboardManager)
                            ctx.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard = cbm.getText();
        }

        if (clipboard != null) {
            try {
                new URL(clipboard.toString());
            } catch (MalformedURLException e) {
                clipboard = "";
            }
        }

        return clipboard;
    }
}
