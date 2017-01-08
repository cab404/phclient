package ru.ponyhawks.android.activity;

import ru.ponyhawks.android.fragments.LetterFragment;
import ru.ponyhawks.android.fragments.PublicationFragment;

public class LetterActivity extends AbstractPublicationActivity {
    @Override
    public PublicationFragment getContentFragment() {
        return LetterFragment.getInstance(id);
    }
}
