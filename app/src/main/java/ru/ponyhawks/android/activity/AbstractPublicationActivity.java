package ru.ponyhawks.android.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import java.util.Map;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.AbstractCommentEditFragment;
import ru.ponyhawks.android.fragments.PublicationFragment;
import ru.ponyhawks.android.utils.Meow;

public abstract class AbstractPublicationActivity extends LoginDependentActivity {

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";

    AbstractCommentEditFragment ced;
    PublicationFragment topic;
    protected int id;

    public int getId() {
        return id;
    }

    void backToMain() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setTitle(getIntent().getStringExtra(KEY_TITLE));
        id = -1;

        if (getIntent().getData() != null)
            System.out.println(getIntent().getData().getLastPathSegment());

        final Uri uri = getIntent().getData();
        final Map.Entry<Integer, Integer> path = Meow.resolvePostUrl(uri);
        if (uri != null) {
            if (path == null) {
                finish();
                return;
            }
            id = path.getKey();
            getIntent().putExtra(KEY_ID, id);
        } else if (Build.VERSION.SDK_INT >= 21)
            setTaskDescription(
                    new ActivityManager.TaskDescription(getIntent().getStringExtra("title"))
            );

        id = getIntent().getIntExtra(KEY_ID, id);

        boolean useMultitasking =
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("multitasking", false);

        if (useMultitasking && Build.VERSION.SDK_INT >= 21) {
            for (ActivityManager.AppTask task : Meow.getTaskList(this, getClass())) {
                final Intent running = task.getTaskInfo().baseIntent;
                final Map.Entry<Integer, Integer> resolveUrl = Meow.resolvePostUrl(running.getData());
                if (running.getIntExtra(KEY_ID, -1) == id || (resolveUrl != null && resolveUrl.getKey() == id)) {
                    if (task.getTaskInfo().id == getTaskId()) continue;
                    task.moveToFront();
                    finish();
                    return;
                }
            }

        }

        setContentView(R.layout.activity_topic);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ced = new EditorFragment();
        topic = getContentFragment();
        getSupportFragmentManager().beginTransaction()
                .add(
                        R.id.content_frame, topic
                )
                .add(
                        R.id.comment_frame, ced
                )
                .commit();

        topic.setCommentFragment(ced);
        ced.collapse();

    }

    public abstract PublicationFragment getContentFragment();

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onBackPressed() {
        if (ced.isExpanded())
            ced.collapse();
        else
            backToMain();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                backToMain();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void moveToComment(int value) {
        topic.moveToComment(value);
    }
}
