package ru.ponyhawks.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 13:50 on 08/01/17
 *
 * @author cab404
 */
public class LineInputDialog extends Dialog {

    @Bind(R.id.confirm)
    View ok;
    @Bind(R.id.text)
    EditText text;

    public interface OnConfirmListener {
        boolean onConfirm(Editable text);
    }

    public LineInputDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_text);
        ButterKnife.bind(this);
    }

    public void setOnClick(final OnConfirmListener confirm) {
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirm.onConfirm(text.getText()))
                    dismiss();
            }
        });
    }
}
