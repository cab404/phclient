package ru.ponyhawks.android.parts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;
import com.cab404.libph.data.Comment;
import com.cab404.libph.modules.CommentTreeModule;
import com.cab404.moonlight.parser.HTMLTree;
import com.cab404.moonlight.parser.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.text.DateUtils;
import ru.ponyhawks.android.text.HtmlRipper;
import ru.ponyhawks.android.text.StaticWebView;
import ru.ponyhawks.android.utils.DoubleClickListener;
import ru.ponyhawks.android.utils.GlideApp;
import ru.ponyhawks.android.utils.MidnightSync;

/**
 * Rated Insane.
 * <p/>
 * <p/>
 * Created at 00:26 on 14/09/15
 *
 * @author cab404
 */
public class CommentPart extends MoonlitPart<Comment> implements MidnightSync.InsertionRule<Comment> {
    Map<Integer, Comment> data = new HashMap<>();
    Map<Integer, Integer> ids = new HashMap<>();
    Map<Integer, String> reposts = new HashMap<>();

    public boolean blockUsers = false;
    public boolean saveState = true;

    Map<Integer, HtmlRipper> savedStates = new HashMap<>();
    private CommentPartCallback callback;

//    public static final DisplayImageOptions AVATARS_CFG = new DisplayImageOptions.Builder().cacheInMemory(true).build();
    private int selectedId;

    public synchronized void register(Comment comment) {

        if ("ph".equals(comment.author.login)) {
            try {
                HTMLTree html = new HTMLTree(comment.text);
                List<Tag> tags = html.xPath("a&target=_blank");
                if (tags.size() >= 2 && "[t]".equals(html.getContents(tags.get(0)))) {
                    comment.author.login = html.getContents(tags.get(1));
                    comment.author.small_icon = html.xPathFirstTag("img&align=left").get("src");
                    System.out.println(comment.author.small_icon);
                    System.out.println(comment.text);
                    comment.text = "" + comment.text.substring(html.get(html.getClosingTag(html.xPathFirstTag("strong"))).end + 2);
                    System.out.println(comment.text);
                    comment.author.is_system = false;
                    reposts.put(comment.id, tags.get(0).get("href"));
                }
            } catch (Exception e) {
                // :\
            }
        }

        data.put(comment.id, comment);
    }

    public void updateFrom(CommentTreeModule data) {
        for (Integer id : data.parents.keySet()) {
            this.data.get(id).parent = data.parents.get(id);
        }
    }

    private int levelOf(int id, int cl) {
        if (!data.containsKey(id)) return 0;
        int parent = data.get(id).parent;
        if (parent != 0)
            return levelOf(parent, cl + 1);
        else
            return cl;
    }

    protected int levelOf(int id) {
        return levelOf(id, 0);
    }

    int savedOffset = 0;

    public Collection<Comment> getComments() {
        return data.values();
    }
    public Comment getComment(int id) {
        return data.get(id);
    }

    public void offset(AbsListView parent, int offset) {
        savedOffset = offset;
        final int position = parent.getFirstVisiblePosition();
        ChumrollAdapter adapter = (ChumrollAdapter) parent.getAdapter();
        int type = adapter.typeIdOf(this);
        for (int i = position; i < position + parent.getChildCount(); i++) {
            if (adapter.getItemViewType(i) == type) {
                View view = parent.getChildAt(i - position);
                Comment data = (Comment) adapter.getData(i);
                resetOffset(view, data);
            }
        }
    }

    public void offsetToId(AbsListView parent, int id) {
        final int lv = (int) (parent.getContext().getResources().getDisplayMetrics().density * 16);
        final int level = levelOf(id) * lv;
        offset(parent, level);
    }

    public int getLastCommentId() {
        int max = 0;
        for (Integer id : data.keySet()) max = Math.max(id, max);
        return max;
    }

    void resetOffset(View view, Comment data) {
        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        int level = levelOf(data.id);
        int padding = -lv * level + savedOffset;
        view.scrollTo(padding, view.getScrollY());
    }

    public void updateIndexes(ChumrollAdapter adapter) {
        int sid = adapter.typeIdOf(this);
        for (int i = 0; i < adapter.getCount(); i++)
            if (sid == adapter.getItemViewType(i)) {
                final Comment data = (Comment) adapter.getData(i);
                ids.put(data.id, adapter.idOf(i));
            }
    }

    @BindView(R.id.text)
    StaticWebView text;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.repost)
    ImageView repost;
    @BindView(R.id.userspace)
    View userspace;
    @BindView(R.id.delimiter)
    View delimeter;
    @BindView(R.id.root)
    View root;

    @Override
    public void convert(View view, final Comment cm, int index, final ViewGroup parent, ChumrollAdapter adapter) {
        register(cm);
        super.convert(view, cm, index, parent, adapter);

        ButterKnife.bind(this, view);

        if (reposts.containsKey(cm.id)) {
            repost.setVisibility(View.VISIBLE);
            repost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(reposts.get(cm.id))
                            )
                    );
                }
            });
        } else
            repost.setVisibility(View.GONE);

        view.setBackgroundColor(selectedId == cm.id ? 0x80000000 : cm.is_new ? 0x40000000 : 0);

        boolean showDelimeters = PreferenceManager
                .getDefaultSharedPreferences(view.getContext())
                .getBoolean("showDelimiters", false);
        delimeter.setVisibility(showDelimeters ? View.VISIBLE : View.GONE);

        final int lv = (int) (view.getContext().getResources().getDisplayMetrics().density * 16);
        view.setOnClickListener(new DoubleClickListener() {
            @Override
            public void act(View v) {
                int offset = levelOf(cm.id) * lv;
                if (savedOffset == offset) offset = 0;
                offset((AbsListView) parent, offset);
            }
        });

        final View.OnClickListener showControls = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showActionDialog(cm, v.getContext());
            }
        };
        userspace.setOnClickListener(showControls);

        resetOffset(view, cm);

        if (saveState)
            if (savedStates.containsKey(cm.id)) {
                HtmlRipper ripper = savedStates.get(cm.id);
                text.setRipper(ripper);
                ripper.layout();
            }
            else
                savedStates.put(cm.id, text.setText(cm.text));
        else
            text.setText(cm.text);


        avatar.setVisibility(cm.author.is_system ? View.GONE : View.VISIBLE);

        author.setText(cm.author.login);
        avatar.setImageDrawable(null);

        date.setText(DateUtils.formPreciseDate(cm.date));
        root.setBackgroundColor(cm.deleted ? 0x40000000 : 0);

        if (!cm.author.is_system) {
            GlideApp.with(avatar).load(cm.author.small_icon).into(avatar);
        }
    }

    public void setCallback(CommentPartCallback callback) {
        this.callback = callback;
    }

    public int getIndex(int cid, ChumrollAdapter adapter) {
        if (!data.containsKey(cid))
            return MidnightSync.INDEX_REMOVE;
        return adapter.indexOf(data.get(cid));
    }

    public void setSelectedId(int selectedId) {
        this.selectedId = selectedId;
    }

    public void invalidateCommentText(int id) {
        if (saveState) {
            final HtmlRipper state = savedStates.remove(id);
            if (state != null)
                state.destroy();
        }
    }

    public boolean hidden(Comment newC) {
        return (newC == null) || (newC.deleted && blockUsers);
    }

    public interface CommentPartCallback {
        enum Action {
            EDIT, FAV, REPLY, SHARE
        }

        void onCommentActionInvoked(Action act, Comment cm, Context context);

    }

    @Override
    public int getLayoutId() {
        return R.layout.part_comment;
    }

    public void destroy() {
        for (HtmlRipper ripper : savedStates.values())
            ripper.destroy();
    }

    public final static CommentComparator CC_INST = new CommentComparator();

    public void clearNew() {
        for (Comment cm : data.values())
            cm.is_new = false;
    }

    private final static class CommentComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment lhs, Comment rhs) {
            return lhs.id - rhs.id;
        }
    }

    List<Comment> collectChildren(int parent, ChumrollAdapter adapter) {
        List<Comment> children = new ArrayList<>();
        for (Comment c : data.values())
            if (c.parent == parent)
                children.add(c);
        for (int i = 0; i < children.size(); )
            if (adapter.indexOf(children.get(i)) == -1)
                children.remove(i);
            else
                i++;
        return children;
    }

    /**
     * Finds last index in children's tree
     */
    int upfall(ChumrollAdapter adapter, Comment parent) {
        final List<Comment> parentsNeighbours = collectChildren(parent.id, adapter);
        Collections.sort(parentsNeighbours, CC_INST);

        if (parentsNeighbours.size() == 0)
            return adapter.indexOfId(ids.get(parent.id));
        else
            return upfall(adapter, parentsNeighbours.get(parentsNeighbours.size() - 1));
    }


    /**
     * Fuck yes.
     */
    @Override
    public int indexFor(Comment newC, ViewConverter<Comment> converter, ChumrollAdapter adapter) {
        updateIndexes(adapter);

        if (hidden(newC))
            return MidnightSync.INDEX_REMOVE;
        if (newC.parent != 0 && ids.get(newC.parent) == null) {
            Log.e("CM ADD ERROR", "ERROR WHILE SEARCHING PARENT WITH ID " + newC.parent);
            Log.e("CM ADD ERROR", "TREE DATA: " + ids);

            return MidnightSync.INDEX_REMOVE;
        }

        updateIndexes(adapter);

        List<Comment> nbrs = collectChildren(newC.parent, adapter);

        for (Comment cm : nbrs)
            if (cm.id == newC.id)
                return MidnightSync.INDEX_REMOVE;

        if (nbrs.size() == 0)
            return newC.parent == 0 ? MidnightSync.INDEX_END : (adapter.indexOfId(ids.get(newC.parent)) + 1);

        nbrs.add(newC);
        Collections.sort(nbrs, CC_INST);
        int index = nbrs.indexOf(newC);

        if (index == nbrs.size() - 1)
            return upfall(adapter, nbrs.get(nbrs.size() - 2)) + 1;
        else
            return adapter.indexOfId(ids.get(nbrs.get(index + 1).id));

    }

    void showActionDialog(final Comment cm, Context ctx) {
        final int theme = ctx
                .getTheme()
                .obtainStyledAttributes(new int[]{R.attr.alert_dialog_nobg_theme})
                .getResourceId(0, 0);

        @SuppressLint("InflateParams") final
        View controls = LayoutInflater.from(ctx)
                .inflate(R.layout.alert_comment_controls, null, false);

        final AlertDialog dialog = new AlertDialog
                .Builder(ctx, theme)
                .setView(controls)
                .show();

        final ImageView fav = (ImageView) controls.findViewById(R.id.fav);
        final ImageView edit = (ImageView) controls.findViewById(R.id.edit);
        final ImageView reply = (ImageView) controls.findViewById(R.id.reply);
        final ImageView share = (ImageView) controls.findViewById(R.id.copy_link);

        fav.setImageResource(
                cm.in_favs ?
                        R.drawable.ic_star :
                        R.drawable.ic_star_outline
        );

        final View.OnClickListener acl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentPartCallback.Action act;
                switch (v.getId()) {
                    case R.id.fav:
                        act = CommentPartCallback.Action.FAV;
                        break;
                    case R.id.edit:
                        act = CommentPartCallback.Action.EDIT;
                        break;
                    case R.id.reply:
                        act = CommentPartCallback.Action.REPLY;
                        break;
                    case R.id.copy_link:
                        act = CommentPartCallback.Action.SHARE;
                        break;
                    default:
                        act = null;
                }

                if (callback != null)
                    callback.onCommentActionInvoked(act, cm, v.getContext());
                dialog.dismiss();
            }
        };
        fav.setOnClickListener(acl);
        edit.setOnClickListener(acl);
        share.setOnClickListener(acl);
        reply.setOnClickListener(acl);
    }

}
