package ru.ponyhawks.android.statics;

import android.app.Application;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.crashlytics.android.Crashlytics;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import ru.ponyhawks.android.BuildConfig;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:46 on 14/09/15
 *
 * @author cab404
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        Providers.Preferences.getInstance().init(this);
        Providers.Profile.getInstance().init(this);
        Providers.ImgurGateway.init(this);

        Glide.init(this, new GlideBuilder().setLogLevel(Log.ERROR));

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("forceRussian", false)) {
            getResources().getConfiguration().locale = new Locale("ru");
            getResources().updateConfiguration(
                    getResources().getConfiguration(),
                    getResources().getDisplayMetrics()
            );
        }

    }

}
