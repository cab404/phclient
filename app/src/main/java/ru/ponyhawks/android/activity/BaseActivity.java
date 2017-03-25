package ru.ponyhawks.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Base themed activity
 *
 * @author cab404
 */
public class BaseActivity extends AppCompatActivity {

    static final LinkedList<BaseActivity> running = new LinkedList<>();
    RequestManager manager = new RequestManager(Providers.Profile.get());

    private boolean isVisible;

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    private void setupTheme() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String themeName = sp.getString("theme", "AppThemeDark");

        int id = getResources().getIdentifier(themeName, "style", getPackageName());
        if (id == 0) id = R.style.AppThemeDark;

        setTheme(id);

    }

    private void setupLang() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("forceRussian", false)) {
            getResources().getConfiguration().locale = new Locale("ru");
            getResources().updateConfiguration(
                    getResources().getConfiguration(),
                    getResources().getDisplayMetrics()
            );
        }
    }

    public RequestManager getRequestManager() {
        return manager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running.remove(this);
        manager.cancelAll();
    }

    @SuppressWarnings("unchecked")
    public static <A extends BaseActivity> List<A> getRunning(Class<A> type) {
        List<A> running_activities = new LinkedList<>();
        for (BaseActivity activity : running)
            if (activity.getClass().isAssignableFrom(type))
                running_activities.add((A) activity);
        return running_activities;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        running.add(this);
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("removeShadow", false))
            if (getSupportActionBar() != null)
                getSupportActionBar().setElevation(0);
    }

    @Override
    public void setContentView(View view) {
        setupLang();
        super.setContentView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        setupLang();
        super.setContentView(layoutResID);
    }

}
