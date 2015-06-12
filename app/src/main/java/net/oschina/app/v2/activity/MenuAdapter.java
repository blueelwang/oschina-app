package net.oschina.app.v2.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.User;

/**
 * Created by Tonlin on 2015/3/5.
 */
@Deprecated
public class MenuAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.v2_list_cell_popup_menu_userinfo, null);
            TextView name = (TextView) convertView
                    .findViewById(R.id.tv_name);
            ImageView avatar = (ImageView) convertView
                    .findViewById(R.id.iv_avatar);
            AppContext.instance().initLoginInfo();
            if (AppContext.instance().isLogin()) {
                User user = AppContext.instance().getLoginInfo();
                name.setText(user.getName());
                ImageLoader.getInstance().displayImage(user.getFace(),
                        avatar);
            } else {
                name.setText(R.string.unlogin);
                avatar.setImageBitmap(null);
            }
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.v2_list_cell_popup_menu, null);
            TextView name = (TextView) convertView
                    .findViewById(R.id.tv_name);
            int iconResId = 0;

            if (position == 1) {
                name.setText(R.string.main_menu_software);
                iconResId = R.drawable.actionbar_menu_icn_software;
            } else if (position == 2) {
                name.setText(R.string.main_menu_setting);
                iconResId = R.drawable.actionbar_menu_icn_set;
            } else if (position == 3) {
                name.setText(R.string.main_menu_exit);
                iconResId = R.drawable.actionbar_menu_icn_exit;
            }
            Drawable drawable = AppContext.resources().getDrawable(
                    iconResId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                    drawable.getMinimumHeight());
            name.setCompoundDrawables(drawable, null, null, null);
        }
        return convertView;
    }
}