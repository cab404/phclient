package ru.ponyhawks.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.chumroll.ViewConverter;
import com.cab404.libph.data.Comment;
import com.cab404.libph.data.Type;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.requests.CommentAddRequest;
import com.cab404.libph.requests.CommentEditRequest;
import com.cab404.libph.requests.FavRequest;
import com.cab404.libph.requests.LSRequest;
import com.cab404.libph.requests.RefreshCommentsRequest;
import com.cab404.moonlight.framework.ModularBlockParser;
import com.cab404.moonlight.framework.Page;
import com.cab404.moonlight.util.SU;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.activity.BaseActivity;
import ru.ponyhawks.android.activity.RefreshRatePickerDialog;
import ru.ponyhawks.android.activity.ResolverActivity;
import ru.ponyhawks.android.parts.CommentNumPart;
import ru.ponyhawks.android.parts.CommentPart;
import ru.ponyhawks.android.parts.LoadingPart;
import ru.ponyhawks.android.parts.UpdateCommonInfoTask;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public abstract class PublicationFragment extends ListFragment implements
        AbstractCommentEditFragment.SendCallback,
        CommentPart.CommentPartCallback,
        RefreshRatePickerDialog.RefreshPickedListener {
    public static final String KEY_ID = "id";

    private ChumrollAdapter adapter;
    private MidnightSync sync;
    private CommentPart commentPart;

    private Comment replyingTo = null;
    private boolean editing = false;

    private AbstractCommentEditFragment commentFragment;
    private boolean atLeastSomethingIsHere;
    private boolean broken;

    public void setCommentFragment(AbstractCommentEditFragment commentFragment) {
        this.commentFragment = commentFragment;
        commentFragment.setSendCallback(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_topic;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;
    }

    abstract void prepareAdapter(ChumrollAdapter adapter);

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChumrollAdapter();

        commentPart = new CommentPart();
        commentPart.setCallback(this);
        commentPart.saveState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("saveCommentState", true);
        commentPart.blockUsers = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getBoolean("blockUsers", false);

        final CommentNumPart commentNumPart = new CommentNumPart();
        adapter.prepareFor(commentPart, new LoadingPart(), commentNumPart);
        prepareAdapter(adapter);
        setAdapter(adapter);

        sync = new MidnightSync(adapter);

        bindModules(sync);
        sync
                .bind(MainPage.BLOCK_COMMENT, commentPart, new MidnightSync.InsertionRule<Comment>() {
                    @Override
                    public int indexFor(Comment newC, ViewConverter<Comment> converter, ChumrollAdapter adapter) {
                        if (commentPart.hidden(newC))
                            return -1;
                        return -2;
                    }
                })
                .bind(MainPage.BLOCK_COMMENT_NUM, commentNumPart);
        fullReload();
        if (commentFragment != null)
            commentFragment.setTarget(getActivity().getString(R.string.replying_topic));

        isInitialized = true;
    }

    volatile boolean updating = false;

    List<Integer> newCommentsStack = new ArrayList<>();
    Comparator<Integer> levelIDs = new Comparator<Integer>() {
        @Override
        public int compare(Integer lhs, Integer rhs) {
            return commentPart.getIndex(lhs, adapter) - commentPart.getIndex(rhs, adapter);
        }
    };

    public void nextNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (newCommentsStack.isEmpty()) return;

                Collections.sort(newCommentsStack, levelIDs);

                final int next = newCommentsStack.remove(0);
                final int index = commentPart.getIndex(next, adapter);

                if (index == -1) {
                    Toast.makeText(
                            getContext(),
                            "Странно, но следующий комментарий не найден .-.",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                list.post(new Runnable() {
                    @Override
                    public void run() {
                        commentPart.offsetToId(list, next);
                        list.setSelection(index);
                    }
                });


                commentFragment.setCommentCount(newCommentsStack.size());
                commentPart.setSelectedId(next);
                adapter.notifyDataSetChanged();
            }
        });

    }

    public void clearNew() {
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                commentPart.clearNew();
                newCommentsStack.clear();
                commentFragment.setCommentCount(newCommentsStack.size());
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void update(final boolean clearNew, int selfCommentId) {
        if (updating) return;
        if (getActivity() == null) return; // occurs a lot of times, actually
        updating = true;
        commentFragment.setUpdating(true);
        commentPart.setSelectedId(0);
        list.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        if (broken) {
            fullReload();
            return;
        }

        final RefreshCommentsRequest request = getRefreshRequest(getCommentPart().getLastCommentId());
        request.setSelfIdComment(selfCommentId);

        RequestManager.fromActivity(getActivity())
                .manage(request)
                .setCallback(new RequestManager.SimpleRequestCallback<RefreshCommentsRequest>() {
                    @Override
                    public void onStart(RefreshCommentsRequest what) {
                        super.onStart(what);
                    }

                    @Override
                    public void onSuccess(final RefreshCommentsRequest what) {
                        list.post(new Runnable() {
                            @Override
                            public void run() {
                                if (clearNew) {
                                    clearNew();
                                }
                                for (Comment cm : what.comments) {
                                    sync.inject(cm, commentPart, commentPart);
                                    commentPart.register(cm);
                                    if (cm.is_new) {
                                        newCommentsStack.add(cm.id);
                                    }
                                    notifyNewComments(cm);

                                }
                                commentFragment.setCommentCount(newCommentsStack.size());
                            }
                        });
                    }

                    @Override
                    public void onError(RefreshCommentsRequest what, final Exception e) {
                        super.onError(what, e);
                        e.printStackTrace();
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFinish(RefreshCommentsRequest what) {
                        updating = false;
                        commentFragment.setUpdating(false);
                    }
                })
                .start();
    }

    private static final String TAG = "PublicationFragment";

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
        final boolean startAutoUpdate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("autoAutoUpdate", false);
        if (startAutoUpdate)
            onRefreshRatePicked(true, 30000);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        final boolean startAutoUpdate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("autoAutoUpdate", false);
        if (startAutoUpdate)
            onRefreshRatePicked(false, 30000);
    }

    private void notifyNewComments(Comment cm) {
        boolean show = false;
        final boolean showNotificationsReplies = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("showNotificationsReplies", false);
        if (showNotificationsReplies && cm.parent != 0) {
            final Comment parent = commentPart.getComment(cm.parent);
            if (parent != null && parent.author != null && parent.author.login != null) {
                final String username = Providers.UserInfo.getInstance().getInfo().username;
                if (parent.author.login.equals(username)) {
                    show = true;
                }
            }
        }

        if (show && getActivity() != null && !((BaseActivity) getActivity()).isVisible()) {
            final Notification header = new NotificationCompat.Builder(getContext())
                    .setGroupSummary(true)
                    .setGroup("newCommentsFrom" + getLink())
                    .setSmallIcon(R.drawable.ic_re)
                    .setAutoCancel(true)
                    .build();
            final PendingIntent intent = PendingIntent.getActivity(
                    getContext(), 42, new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getLink(cm)),
                            getContext(),
                            ResolverActivity.class
                    ), PendingIntent.FLAG_UPDATE_CURRENT
            );
            final Notification notification = new NotificationCompat.Builder(getContext())
                    .setContentTitle(cm.author.login)
                    .setContentText(SU.removeAllTags(cm.text))
                    .setSubText(getActivity().getTitle())
                    .setSmallIcon(R.drawable.ic_re)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{300, 100, 300})
                    .setGroup("newCommentsFrom" + getLink())
                    .setAutoCancel(true)
                    .setContentIntent(intent)
                    .build();

            NotificationManagerCompat mc = NotificationManagerCompat.from(getContext());

            mc.notify(getLink().hashCode(), header);
            mc.notify(cm.hashCode(), notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        commentPart.destroy();
        onRefreshRatePicked(false, 0);
    }

    public void fullReload() {
        if (commentFragment == null) {
            sync.post(new Runnable() {
                @Override
                public void run() {
                    fullReload();
                }
            });
            return;
        }
        updating = true;
        commentFragment.setUpdating(true);
        adapter.clear();
        final int loadingPartId = adapter.add(LoadingPart.class, null);
        Meow.inMain(new Runnable() {
            @Override
            public void run() {
                if (commentFragment != null)
                    commentFragment.collapse();
            }
        });
        RequestManager.fromActivity(getActivity())
                .manage(getPageRequest())
                .setHandlers(
                        sync,
                        new UpdateCommonInfoTask(),
                        new ModularBlockParser.ParsedObjectHandler() {
                            @Override
                            public void handle(final Object object, int key) {
                                handleInitialLoad(object, key);
                                atLeastSomethingIsHere = true;
                                switch (key) {
                                    case MainPage.BLOCK_COMMENT:
                                        final Comment cm = (Comment) object;
                                        commentPart.register(cm);
                                        if (cm.is_new)
                                            newCommentsStack.add(cm.id);
                                        break;
                                    case MainPage.BLOCK_COMMENTS_ENABLED:
                                        break;
                                }
                            }
                        })
                .setCallback(new RequestManager.SimpleRequestCallback<Page>() {

                    @Override
                    public void onError(Page what, final Exception e) {
                        super.onError(what, e);
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() == null) return;
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                broken = true;
                                if (!atLeastSomethingIsHere) {
                                    getActivity().finish();
                                }
                            }
                        };
                        Meow.inMain(runnable);
                        e.printStackTrace();
                    }

                    @Override
                    public void onFinish(Page what) {
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.removeById(loadingPartId);
                                commentFragment.setCommentCount(newCommentsStack.size());
                            }
                        });
                        updating = false;
                        commentFragment.setUpdating(false);
                    }

                    @Override
                    public void onSuccess(Page what) {
                        super.onSuccess(what);
                        broken = false;
                    }
                })
                .start();
    }

    public CommentPart getCommentPart() {
        return commentPart;
    }

    RefreshRatePickerDialog.SavedRefreshState refreshState = new RefreshRatePickerDialog.SavedRefreshState();

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.continuous_refresh:
                item.setChecked(!item.isChecked());
                onRefreshRatePicked(item.isChecked(), 30000);
//                final RefreshRatePickerDialog dialog = new RefreshRatePickerDialog(getActivity());
//                dialog.setListener(this);
//                dialog.setState(refreshState);
//                dialog.show();
                break;
            case R.id.copy_link:
                setClipboard(getLink());
                Toast.makeText(getActivity(), R.string.topic_link_copied, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reply:
                if (commentFragment.isExpanded()) {
                    commentFragment.collapse();
                } else
                    reply(null, getActivity());
                return true;
            case R.id.to_the_bottom:
                list.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setSelection(adapter.getCount() - 1);
                    }
                });
                return true;
            case R.id.search:
                final EditText search = new EditText(getActivity());
                search.setHint(android.R.string.search_go);
                new AlertDialog.Builder(getActivity())
                        .setView(search)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                newCommentsStack.clear();
                                for (Comment comment : commentPart.getComments())
                                    if (search.getText() != null && comment.text != null
                                            && comment.text.toLowerCase().contains(
                                            (search.getText() + "").toLowerCase())
                                            )
                                        newCommentsStack.add(comment.id);
                                nextNew();
                            }
                        }).show();
                return true;
            case R.id.reload:
                if (!updating)
                    fullReload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh(boolean force) {
        if (force) {
            update(true);
        } else {
            if (newCommentsStack.isEmpty())
                update(true);
            else
                nextNew();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.publication, menu);
    }

    public void fav(final Comment cm, final Context context) {
        final boolean target_state = !cm.in_favs;
        final FavRequest request = new FavRequest(Type.COMMENT, cm.id, target_state);
        RequestManager.fromActivity(getActivity())
                .manage(request)
                .setCallback(new RequestManager.SimpleRequestCallback<FavRequest>() {

                    @Override
                    public void onSuccess(FavRequest what) {
                        if (request.success())
                            cm.in_favs = target_state;
                        msg(request.msg);
                    }

                    @Override
                    public void onError(FavRequest what, Exception e) {
                        msg(e.getLocalizedMessage());
                    }

                    void msg(final String msg) {
                        Meow.inMain(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .start();

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected void setClipboard(String to) {
        final ClipboardManager cbman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cbman.setText(to);
    }

    public void reply(Comment cm, Context context) {
        replyingTo = cm;
        editing = false;

        if (cm == null)
            commentFragment.setTarget(context.getString(R.string.replying_topic));
        else
            commentFragment.setTarget(String.format(context.getString(R.string.replying_comment), cm.id, cm.author.login));
        commentFragment.expand();
    }

    @Override
    public void onSend(final Editable message) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getActivity().getString(R.string.sending_messsge));
        dialog.show();

        int reply = replyingTo == null ? 0 : replyingTo.id;

        LSRequest req;
        if (editing)
            req = getCommentEditRequest(message, reply);
        else
            req = getCommentAddRequest(message, reply);

        RequestManager
                .fromActivity(getActivity())
                .manage(req)
                .setCallback(new RequestManager.SimpleRequestCallback<LSRequest>() {
                    @Override
                    public void onSuccess(final LSRequest what) {
                        super.onSuccess(what);
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(what.msg))
                                    Toast.makeText(getActivity(), what.msg, Toast.LENGTH_SHORT).show();

                                if (what.success()) {
                                    if (editing) {
                                        replyingTo.text = message.toString();
                                        commentPart.invalidateCommentText(replyingTo.id);
                                    }

                                    if (what instanceof CommentAddRequest)
                                        update(false, ((CommentAddRequest) what).id);
                                    else
                                        update(false);

                                    commentFragment.hide();
                                    commentFragment.clear();

                                    if (TextUtils.isEmpty(what.msg))
                                        Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                                }

                                if (!what.success() && TextUtils.isEmpty(what.msg))
                                    Toast.makeText(getActivity(), R.string.undefined_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(LSRequest what, final Exception e) {
                        super.onError(what, e);
                        sync.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFinish(LSRequest what) {
                        super.onFinish(what);
                        dialog.dismiss();
                    }
                })
                .start();
    }

    private void update(boolean reset) {
        update(reset, 0);
    }

    @Override
    public void onCommentActionInvoked(Action act, Comment cm, Context context) {
        switch (act) {
            case REPLY:
                reply(cm, context);
                break;
            case SHARE:
                share(cm, context);
                break;
            case EDIT:
                edit(cm, context);
                break;
            case FAV:
                fav(cm, context);
                break;
        }
    }

    public void share(Comment cm, Context context) {
        final String clip = getLink(cm);
        setClipboard(clip);
        Toast.makeText(getActivity(), R.string.comment_link_copied, Toast.LENGTH_SHORT).show();
    }

    protected abstract String getLink(Comment cm);

    private void edit(Comment cm, Context context) {
        if (commentFragment.isExpanded()) {
            commentFragment.collapse();
            return;
        }
        commentFragment.setText(cm.text);
        replyingTo = cm;
        editing = true;

        commentFragment.setTarget(String.format(context.getString(R.string.editing_comment), cm.id, cm.author.login));
        commentFragment.expand();
    }

    public void moveToComment(int value) {
        commentPart.setSelectedId(value);
        adapter.notifyDataSetChanged();
        list.setSelection(commentPart.getIndex(value, adapter));
    }


    long refreshRateMs = 0;
    private boolean isInitialized = false;

    Runnable updateCycle = new Runnable() {
        @Override
        public void run() {
            if (isDetached() | !isInitialized) return;
            update(false);
            list.postDelayed(this, refreshRateMs);
        }
    };

    @Override
    public void onRefreshRatePicked(boolean enabled, long rate_ms) {
        list.removeCallbacks(updateCycle);
        if (enabled) list.postDelayed(updateCycle, refreshRateMs = rate_ms);
    }

    protected abstract Page getPageRequest();

    protected abstract void bindModules(MidnightSync sync);

    protected abstract void handleInitialLoad(final Object object, int key);

    protected abstract RefreshCommentsRequest getRefreshRequest(int lastCommentId);

    protected abstract String getLink();

    protected abstract CommentAddRequest getCommentAddRequest(Editable message, int reply);

    protected abstract CommentEditRequest getCommentEditRequest(Editable message, int reply);


}
