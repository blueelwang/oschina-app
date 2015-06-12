package net.oschina.app.v2.activity.friend.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.FriendList.Friend;
import net.oschina.app.v2.utils.UIHelper;

public class FriendAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_friend, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        final Friend item = (Friend) _data.get(position);

        vh.name.setText(item.getName());
        vh.desc.setText(item.getExpertise());
        vh.gender.setImageResource(item.getGender() == 1 ? R.drawable.list_male
                : R.drawable.list_female);

        ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
        final Context context = vh.avatar.getContext();
        vh.avatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UIHelper.showUserCenter(context, item.getUserid(),
                        item.getName());
            }
        });
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
		public TextView name, desc;
		public ImageView gender;
		public ImageView avatar;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			name = (TextView) view.findViewById(R.id.tv_name);
			desc = (TextView) view.findViewById(R.id.tv_desc);
			gender = (ImageView) view.findViewById(R.id.iv_gender);
			avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		}
	}
}
