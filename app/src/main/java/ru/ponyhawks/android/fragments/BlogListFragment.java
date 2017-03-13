package ru.ponyhawks.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Blog;
import com.cab404.libph.pages.BasePage;
import com.cab404.libph.pages.BlogListPage;

import ru.ponyhawks.android.R;
import ru.ponyhawks.android.parts.BlogItem;
import ru.ponyhawks.android.parts.MoonlitPart;
import ru.ponyhawks.android.utils.MidnightSync;
import ru.ponyhawks.android.utils.RequestManager;

/**
 * Created at 01:01 on 14/03/17
 *
 * @author cab404
 */
public class BlogListFragment extends ListFragment implements MoonlitPart.OnDataClickListener<Blog> {

    ChumrollAdapter adapter = new ChumrollAdapter();

    {
        final BlogItem item = new BlogItem();
        item.setOnDataClickListener(this);
        adapter.prepareFor(item);
    }

    MidnightSync sync = new MidnightSync(adapter);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(adapter);
        if (!adapter.isEmpty()) return;
        final BlogListPage page = new BlogListPage();
        RequestManager.fromActivity(getActivity())
                .manage(page)
                .setHandlers(sync.bind(BasePage.BLOCK_BLOG_INFO, BlogItem.class))
                .start();
    }

    @Override
    public void onClick(Blog data, View view) {
        final PublicationsListFragment fragment = PublicationsListFragment.getInstance("/blog/" + data.url_name);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("openedBlog")
                .commit();
    }
}
