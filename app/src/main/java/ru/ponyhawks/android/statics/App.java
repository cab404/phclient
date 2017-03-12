package ru.ponyhawks.android.statics;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 02:46 on 14/09/15
 *
 * @author cab404
 */
public class App extends Application {

    public String appv = "unknown";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Providers.Preferences.getInstance().init(this);
        Providers.Profile.getInstance().init(this);

        try {
            appv = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("", e);
        }

        final ImageLoaderConfiguration config =
                new ImageLoaderConfiguration.Builder(this)
                        .build();
        ImageLoader.getInstance().init(config);

        Providers.ImgurGateway.init(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("forceRussian", false)) {
            getResources().getConfiguration().locale = new Locale("ru");
            getResources().updateConfiguration(
                    getResources().getConfiguration(),
                    getResources().getDisplayMetrics()
            );
        }

//        final Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
//        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//
//                File logfolder = new File(getFilesDir(), "logs");
//                if (!logfolder.exists()) logfolder.mkdir();
//
//                int index = 0;
//                File errsave;
//                do errsave = new File(logfolder, "pherrlog-" + index++ + ".txt");
//                while (errsave.exists());
//
//                try {
//                    final PrintWriter writer = new PrintWriter(new FileOutputStream(errsave));
//                    ex.printStackTrace(writer);
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                }
//
//                Intent email = new Intent(Intent.ACTION_SEND);
//                email.setType("text/plain");
//                email.putExtra(Intent.EXTRA_SUBJECT,
//                        "phclient v" + appv + " crash on "
//                                + Build.PRODUCT +
//                                ", API " + Build.VERSION.SDK_INT);
//                email.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(App.this, "ru.ponyhawks", errsave));
//                email.putExtra(Intent.EXTRA_EMAIL, "me@cab404.ru");
//
//                final Intent intent = Intent.createChooser(email, "Отправить логи");
//
//                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//                am.set(
//                        AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
//                        PendingIntent.getActivity(
//                                App.this, 4567874, intent, PendingIntent.FLAG_ONE_SHOT
//                        )
//                );
//
//                thread.getThreadGroup().uncaughtException(thread, ex);
//            }
//
//        });

    }

}
