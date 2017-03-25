package ru.ponyhawks.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;

import com.cab404.libph.data.CommonInfo;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.fragments.BlogListFragment;
import ru.ponyhawks.android.fragments.DrawerContentFragment;
import ru.ponyhawks.android.fragments.FavouritesFragment;
import ru.ponyhawks.android.fragments.LetterListFragment;
import ru.ponyhawks.android.fragments.LoginFragment;
import ru.ponyhawks.android.fragments.PublicationsFragment;
import ru.ponyhawks.android.fragments.PublicationsListFragment;
import ru.ponyhawks.android.statics.Providers;
import ru.ponyhawks.android.utils.Meow;
import ru.ponyhawks.android.utils.NotificationDrawerDrawable;

public class MainActivity extends LoginDependentActivity implements DrawerContentFragment.DrawerClickCallback {

    private NotificationDrawerDrawable notifArrow;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            onNewIntent(getIntent());
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the drawer.
        notifArrow = new NotificationDrawerDrawable(this);
        notifArrow.setShowCircle(notifications);
        notifArrow.setColor(getTitleColor());
        notifArrow.setCircleColor(notifArrow.getColor());
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.inverse_action_bar_color, value, true);
        notifArrow.setColor(value.data);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerToggle.setDrawerArrowDrawable(notifArrow);
        drawerToggle.getToolbarNavigationClickListener();
        drawerLayout.setDrawerListener(drawerToggle);

        onDrawerItemSelected(DrawerContentFragment.ID_MAIN);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        drawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    boolean notifications = false;

    public void setNotificationCircle(boolean show) {
        notifications = show;
        if (notifArrow != null)
            notifArrow.setShowCircle(show);
    }

    int currentSection = -1;

    @Override
    public void onDrawerItemSelected(int id) {

        while (getSupportFragmentManager().popBackStackImmediate()) {
            Log.v("Stack", "Pop!");
        }

        Fragment use = null;
        if (currentSection == id)
            return;

        final CommonInfo info = Providers.UserInfo.getInstance().getInfo();
        if (info == null) {
            startActivity(new Intent(MainActivity.this, SplashActivity.class));
            finish();
            return;
        }
        String login = info.username;

        switch (id) {
            case DrawerContentFragment.ID_MAIN:
                use = PublicationsListFragment.getInstance("/blog/good");
                break;
            case DrawerContentFragment.ID_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case DrawerContentFragment.ID_BLOGS:
                use = new BlogListFragment();
                break;
            case DrawerContentFragment.ID_MESSAGES:
                use = LetterListFragment.getInstance();
                break;
            case DrawerContentFragment.ID_FAVOURITES:
                use = new FavouritesFragment();
                break;
            case DrawerContentFragment.ID_PUBLICATIONS:
                use = new PublicationsFragment();
                break;
            case DrawerContentFragment.ID_EXIT:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.logout_promt)
                        .setPositiveButton(Meow.getRandomOf(this, R.array.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }


        if (use != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, use, "content")
                    .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            currentSection = id;
        }
    }

    protected void logout() {
        Providers.Preferences.getInstance().get().edit()
                .remove(LoginFragment.KEY_USERNAME)
                .remove(LoginFragment.KEY_PASSWORD)
                .commit();
        Providers.Profile.getInstance().reset();

        final Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }


}
