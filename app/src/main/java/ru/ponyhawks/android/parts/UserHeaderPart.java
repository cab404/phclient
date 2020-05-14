package ru.ponyhawks.android.parts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cab404.chumroll.ChumrollAdapter;
import com.cab404.libph.data.CommonInfo;
import com.cab404.libph.data.Profile;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.ponyhawks.android.R;
import ru.ponyhawks.android.utils.GlideApp;
import ru.ponyhawks.android.utils.Meow;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 11:00 on 15/09/15
 *
 * @author cab404
 */
public class UserHeaderPart extends MoonlitPart<CommonInfo> {

    @BindView(R.id.login)
    TextView login;

    @BindView(R.id.avatar)
    ImageView avatar;


    @Override
    public void convert(View view, CommonInfo data, int index, ViewGroup parent, ChumrollAdapter adapter) {
        super.convert(view, data, index, parent, adapter);
        ButterKnife.bind(this, view);

        login.setText(data.username);
        Profile profile = new Profile();
        profile.mid_icon = data.avatar;
        profile.fillImages();

        GlideApp.with(avatar).load(Meow.getUrl(profile.big_icon)).into(avatar);
    }

    @Override
    public int getLayoutId() {
        return R.layout.part_common_info;
    }
}
