package ru.ponyhawks.android.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 16:06 on 05/10/15
 *
 * @author cab404
 */
public class ProfileActivity extends LoginDependentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String username = getIntent().getData().getQueryParameter("username");

    }
}
