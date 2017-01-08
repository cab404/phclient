package ru.ponyhawks.android.activity;

import ru.ponyhawks.android.fragments.PublicationFragment;
import ru.ponyhawks.android.fragments.TopicFragment;

public class TopicActivity extends AbstractPublicationActivity {
    @Override
    public PublicationFragment getContentFragment() {
        return TopicFragment.getInstance(id);
    }
}
