package ru.ponyhawks.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Topic;
import com.cab404.libph.modules.TopicModule;
import com.cab404.libph.pages.MainPage;
import com.cab404.libph.util.PonyhawksProfile;
import com.cab404.moonlight.framework.ModularBlockParser;

import ru.ponyhawks.android.activity.TopicActivity;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.parts.SpacePart;
import ru.ponyhawks.android.parts.TopicPart;
import ru.ponyhawks.android.statics.ProfileStore;
import ru.ponyhawks.android.utils.BatchedInsertHandler;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 00:52 on 14/09/15
 *
 * @author cab404
 */
public class TopicListFragment extends ListFragment {
    public static final String KEY_URL = "url";
    ChumrollAdapter adapter;
    private SpacePart spacePart;

    public static TopicListFragment getInstance(String pageUrl) {
        final TopicListFragment fragment = new TopicListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_URL, pageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ChumrollAdapter();
        final TopicPart topicPart = new TopicPart();
        topicPart.setOnDataClickListener(new MoonlitPart.OnDataClickListener<Topic>() {
            @Override
            public void onClick(Topic data, View view) {
                switchToPage(data);
            }
        });
        adapter.prepareFor(topicPart);
        spacePart = new SpacePart();
        adapter.prepareFor(spacePart);
        setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
                PonyhawksProfile profile = ProfileStore.get();
                final MainPage page = new MainPage() {
                    @Override
                    protected void bindParsers(ModularBlockParser base) {
                        super.bindParsers(base);
                        base.bind(new TopicModule(TopicModule.Mode.LIST), BLOCK_TOPIC_HEADER);
                    }
                };
                final BatchedInsertHandler insertHandler = new BatchedInsertHandler(adapter);
                page.setHandler(insertHandler.bind(MainPage.BLOCK_TOPIC_HEADER, topicPart));
                page.fetch(profile);
            }
        }.start();
    }

    private void switchToPage(Topic data) {
        Intent startTopicActivity = new Intent(getActivity(), TopicActivity.class);
        startTopicActivity.putExtra(TopicActivity.KEY_TOPIC_ID, data.id);
        startActivity(startTopicActivity);
    }

//    @Override
//    public void handle(Object object, int key) {
//        Log.v("This", "Got object ");
//        switch (key) {
//            case MainPage.BLOCK_TOPIC_HEADER:
//                adapter.add(TopicPart.class, ((Topic) object));
//                System.out.println(((Topic) object).title);
//                break;
//        }
//    }
}
