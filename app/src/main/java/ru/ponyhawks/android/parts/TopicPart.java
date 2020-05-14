package ru.ponyhawks.android.parts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.KV;
import com.cab404.libph.data.Topic;
import com.cab404.libph.requests.SubmitPollRequest;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.text.DateUtils;
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
public class TopicPart extends MoonlitPart<Topic> {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text)
    StaticWebView text;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.comment_num)
    TextView comments;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.delimiter)
    View delimeter;

    private TopicPartCallback callback;

    @Override
    public void convert(View view, final Topic data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        super.convert(view, data, index, parent, adapter);
        ButterKnife.bind(this, view);
        title.setText(data.title);
        text.setText(data.text);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showActionDialog(data, v.getContext());
                return true;
            }
        });

        boolean showDelimeters = PreferenceManager
                .getDefaultSharedPreferences(view.getContext())
                .getBoolean("showDelimiters", false);
        delimeter.setVisibility(showDelimeters ? View.VISIBLE : View.GONE);

        String cc = data.comments > 0 ? data.comments + " " : "";
        cc += data.comments_new > 0 ? "+" + data.comments_new + " " : "";

        comments.setVisibility(cc.isEmpty() ? View.GONE : View.VISIBLE);

        cc += view.getResources().getQuantityString(
                R.plurals.comment_num,
                data.comments_new > 0 ? data.comments_new : data.comments
        );

        comments.setText(cc);

        date.setText(DateUtils.formPreciseDate(data.date));

        author.setText(data.author.login);
        avatar.setImageDrawable(null);
        if (!data.author.is_system) {
            GlideApp.with(avatar).load(Meow.getUrl(data.author.small_icon)).into(avatar);
        }

        /* polls */
        final LinearLayout vPollsList = view.findViewById(R.id.vPollsList);
        vPollsList.removeAllViews();

        if (data.is_poll) {
            float sum = 0f;
            for (KV<String, Integer> var : data.pollData) {
                sum += var.v;
            }

            LayoutInflater inflater = LayoutInflater.from(vPollsList.getContext());
            for (final KV<String, Integer> var : data.pollData) {
                View pollsLine = inflater.inflate(R.layout.include_polls_line, vPollsList, false);
                final ImageView vAdd = pollsLine.findViewById(R.id.vAdd);
                final TextView vTitle = pollsLine.findViewById(R.id.vTitle);
                final TextView vCount = pollsLine.findViewById(R.id.vCount);
                final ProgressBar vVotes = pollsLine.findViewById(R.id.vVotes);

                vTitle.setText(var.k);

                if (data.is_pollFinished) {
                    vAdd.setVisibility(View.GONE);
                    vVotes.setVisibility(View.VISIBLE);

                    StringBuilder count = new StringBuilder();
                    count.append(var.v);
                    if (sum > 0) {
                        float amount = var.v / sum;
                        vVotes.setProgress((int) (vVotes.getMax() * amount));
                        count.append(String.format(Locale.US, " (%.0f%%)", amount * 100));
                    } else {
                        vVotes.setProgress(0);
                    }
                    vCount.setText(count.toString());
                } else {
                    vAdd.setVisibility(View.VISIBLE);
                    vVotes.setVisibility(View.GONE);
                    vCount.setText(null);
                    vAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callback != null) {
                                callback.onPollSubmitInvoked(data, data.pollData.indexOf(var));
                            }
                        }
                    });
                }

                vPollsList.addView(pollsLine);

            }
        }

    }

    public void setCallback(TopicPartCallback callback) {
        this.callback = callback;
    }

    public interface TopicPartCallback {
        void onFavInvoked(Topic cm, Context context);

        void onShareInvoked(Topic cm, Context context);

        void onPollSubmitInvoked(Topic cm, int answer);

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_topic;
    }

    void showActionDialog(final Topic topic, Context ctx) {
        final int theme = ctx
                .getTheme()
                .obtainStyledAttributes(new int[]{R.attr.alert_dialog_nobg_theme})
                .getResourceId(0, 0);

        @SuppressLint("InflateParams") final View controls = LayoutInflater.from(ctx)
                .inflate(R.layout.alert_topic_controls, null, false);

        final AlertDialog dialog = new AlertDialog
                .Builder(ctx, theme)
                .setView(controls)
                .show();

        final ImageView fav = controls.findViewById(R.id.fav);
        final ImageView share = controls.findViewById(R.id.copy_link);

        fav.setImageResource(
                topic.in_favourites ?
                        R.drawable.ic_star :
                        R.drawable.ic_star_outline
        );

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onFavInvoked(topic, v.getContext());
                dialog.dismiss();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onShareInvoked(topic, v.getContext());
                dialog.dismiss();
            }
        });
    }


}
