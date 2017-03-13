package ru.ponyhawks.android.parts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.Blog;

import ru.ponyhawks.android.R;

/**
 * Created at 01:07 on 14/03/17
 *
 * @author cab404
 */
public class BlogItem extends MoonlitPart<Blog> {
    @Override
    public int getLayoutId() {
        return R.layout.part_blog;
    }

    @Override
    public void convert(View view, Blog data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        super.convert(view, data, index, parent, adapter);
//        ImageLoader.getInstance().displayImage(data.icon, new ImageViewAware(((ImageView) view.findViewById(R.id.icon))));
        ((TextView) view.findViewById(R.id.title)).setText(data.name);
    }

    @Override
    public boolean enabled(Blog data, int index, ChumrollAdapter adapter) {
        return true;
    }
}
