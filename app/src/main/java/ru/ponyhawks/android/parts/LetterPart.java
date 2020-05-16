package ru.ponyhawks.android.parts;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Letter;
import com.cab404.libph.data.Topic;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.StaticWebView;
import ru.ponyhawks.android.utils.GlideApp;
import ru.ponyhawks.android.utils.Meow;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:58 on 14/09/15
 *
 * @author cab404
 */
public class LetterPart extends MoonlitPart<Letter> {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    StaticWebView text;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.avatar)
    ImageView avatar;
    private TopicPartCallback callback;

    @Override
    public void convert(View view, final Letter data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        super.convert(view, data, index, parent, adapter);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        text.setText(data.text);

//        view.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                showActionDialog(data, v.getContext());
//                return true;
//            }
//        });

        data.recipients.remove(data.starter.login);
        data.recipients.add(0, data.starter.login);
        author.setText(TextUtils.join(", ", data.recipients));
        avatar.setImageDrawable(null);
        GlideApp.with(avatar).load(Meow.getUrl(data.starter.small_icon)).into(avatar);

    }

    public interface TopicPartCallback {
        void onFavInvoked(Topic cm, Context context);

        void onShareInvoked(Topic cm, Context context);

        void onReplyInvoked(Topic cm, Context context);

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_letter;
    }

}
