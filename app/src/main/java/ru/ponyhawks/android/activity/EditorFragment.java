package ru.ponyhawks.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.AbstractCommentEditFragment;
import ru.ponyhawks.android.text.changers.ImportImageTextChanger;
import ru.ponyhawks.android.text.changers.ShrunkFormattingPrism;
import ru.ponyhawks.android.text.changers.SimpleChangers;
import ru.ponyhawks.android.text.changers.TextChanger;
import ru.ponyhawks.android.text.changers.TextPrism;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.UpdateDrawable;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 06:15 on 30/12/16
 *
 * @author cab404
 */
public class EditorFragment extends AbstractCommentEditFragment {

    @Bind(R.id.instruments)
    public LinearLayout instrumentsLayout;
    @Bind(R.id.text)
    public EditText text;
    @Bind(R.id.send)
    public ImageView send;

    @Bind(R.id.instruments_scroll)
    public View instrumentsScroll;

    @Bind(R.id.editor_root)
    public View editorRoot;
    @Bind(R.id.editor_window)
    public View editorWindow;

    float dp42;
    float dp72;

    private List<TextChanger> instruments;
    private UpdateDrawable spinningWheel;

    {
        instruments = new ArrayList<>();
        instruments.add(SimpleChangers.RE);
        instruments.add(SimpleChangers.SPOILER);
        instruments.add(SimpleChangers.LINK);
        instruments.add(SimpleChangers.QUOTE);
        instruments.add(SimpleChangers.LITESPOILER);
        instruments.add(new ImportImageTextChanger());
        instruments.add(SimpleChangers.VIDEO);
        instruments.add(SimpleChangers.BOLD);
        instruments.add(SimpleChangers.ITALIC);
        instruments.add(SimpleChangers.UNDERLINE);
        instruments.add(SimpleChangers.STRIKETHROUGH);
        instruments.add(SimpleChangers.SPAN_LEFT);
        instruments.add(SimpleChangers.SPAN_CENTER);
        instruments.add(SimpleChangers.SPAN_RIGHT);
    }

    private List<TextPrism> postprocessors;

    {
        postprocessors = new ArrayList<>();
        postprocessors.add(new ShrunkFormattingPrism());
    }

    private int selectedInstrument = 0;

    BottomSheetBehavior bs;
    private BottomSheetBehavior.BottomSheetCallback bsCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (getView() == null) return;
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    hideKeyboard();
                    changeLayout(0);
                    text.clearFocus();
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    changeLayout(1);
                    text.requestFocus();
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    hideKeyboard();
                    changeLayout(0);
                    text.clearFocus();
                    break;
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (getView() == null) return;
            System.out.println(slideOffset);
            changeLayout(slideOffset > 0 ? slideOffset : 0);
        }
    };

    private void hideKeyboard() {
        text.clearFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    private void changeLayout(float slideOffset) {
        ((RelativeLayout.LayoutParams) send.getLayoutParams()).topMargin = 0;//(int) (dp42 * (1 - slideOffset));
        ((RelativeLayout.LayoutParams) instrumentsScroll.getLayoutParams()).rightMargin = (int) (dp42 * slideOffset);
        ((RelativeLayout.LayoutParams) text.getLayoutParams()).rightMargin = (int) (dp42 * (1 - slideOffset));
        ((RelativeLayout.LayoutParams) text.getLayoutParams()).topMargin = (int) (dp42 * (slideOffset));
        ((CoordinatorLayout.LayoutParams) editorRoot.getLayoutParams()).rightMargin = (int) (dp72 * (1 - slideOffset));
        ViewCompat.setAlpha(instrumentsScroll, slideOffset);
        editorWindow.requestLayout();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        spinningWheel = new UpdateDrawable(getActivity());
        return inflater.inflate(R.layout.fragment_comment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        dp42 = getResources().getDisplayMetrics().density * 42;
        dp72 = getResources().getDisplayMetrics().density * 72;
        bs = BottomSheetBehavior.from(editorRoot);

        bs.setBottomSheetCallback(bsCallback);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                if (getView() == null) return;
                getView().getWindowVisibleDisplayFrame(rect);
                getView().getGlobalVisibleRect(rect);
                editorRoot.getLayoutParams().height = (getView().getHeight() + editorWindow.getHeight()) / 2;
                editorRoot.requestLayout();
            }
        });

        updateButton.setImageDrawable(spinningWheel);

        instrumentsLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        for (final TextChanger changer : instruments) {
            View button = inflater.inflate(R.layout.include_instrument_button, instrumentsLayout, false);
            ((ImageView) button.findViewById(R.id.icon)).setImageResource(changer.getImageResource());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changer.onSelect(EditorFragment.this, text);
                    selectedInstrument = instruments.indexOf(changer);
                }
            });
            instrumentsLayout.addView(button);
        }
    }

    private Editable chainAffect(Editable in) {
        for (TextPrism prism : postprocessors)
            in = prism.affect(in);
        return in;
    }

    private Editable chainPurify(Editable in) {
        for (TextPrism prism : postprocessors)
            in = prism.purify(in);
        return in;
    }

    @Override
    public Editable getText() {
        if (text != null)
            return chainPurify(text.getText());
        return null;
    }

    @Override
    public void setTarget(CharSequence target) {
        if (text != null)
            text.setHint(target);
    }

    @Override
    public void clear() {
        if (text != null)
            text.setText("");
    }

    @Override
    public void setText(CharSequence text) {
        if (text != null)
            this.text.setText(text);
    }

    @OnClick(R.id.send)
    public void send() {
        if (sendCallback != null)
            sendCallback.onSend(getText());
    }

    @Override
    public boolean isExpanded() {
        if (bs != null)
            return bs.getState() == BottomSheetBehavior.STATE_EXPANDED;
        return false;
    }

    @Override
    public void hide() {
        if (bs != null)
            bs.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void collapse() {
        if (bs != null)
            bs.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void expand() {
        if (bs != null)
            bs.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void setCommentCount(int count) {
        if (spinningWheel != null)
            spinningWheel.setNum(count);
    }

    @Override
    public void setUpdating(boolean updating) {
        if (spinningWheel != null)
            spinningWheel.setSpinning(updating);
    }


    @OnTextChanged(R.id.text)
    void textChanged(CharSequence text){
        Meow.tintTags((Editable) text);
    }

    @OnFocusChange(R.id.text)
    void onTextFocusChange(boolean hasFocus) {
        if (hasFocus)
            expand();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        instruments.get(selectedInstrument).onActivityResult(requestCode, resultCode, data);
    }

    @Bind(R.id.colorful_button)
    FloatingActionButton updateButton;

    @OnClick(R.id.colorful_button)
    void onRefreshClicked() {
        sendCallback.onRefresh(false);
    }

    @OnLongClick(R.id.colorful_button)
    boolean onRefreshForced() {
        sendCallback.onRefresh(true);
        return true;
    }

}
