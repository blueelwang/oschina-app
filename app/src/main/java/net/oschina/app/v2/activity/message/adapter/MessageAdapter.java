package net.oschina.app.v2.activity.message.adapter;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Message;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.UIHelper;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

public class MessageAdapter extends RecycleBaseAdapter {

	@Override
	protected boolean loadMoreHasBg() {
		return false;
	}

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_message, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        final Message item = (Message) _data.get(position);

        if (AppContext.instance().getLoginUid() == item.getSenderId()) {
            vh.sender.setVisibility(View.VISIBLE);
        } else {
            vh.sender.setVisibility(View.GONE);
        }

        vh.name.setText(item.getFriendName());

        vh.content.setMovementMethod(MyLinkMovementMethod.a());
        vh.content.setFocusable(false);
        vh.content.setDispatchToParent(true);
        vh.content.setLongClickable(false);
        Spanned span = Html.fromHtml(item.getContent());
        vh.content.setText(span);
        MyURLSpan.parseLinkText(vh.content, span);

        final Context context = vh.avatar.getContext();

        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));
        vh.count.setText(context.getResources().getString(
                R.string.message_count,item.getMessageCount()));

        ImageLoader.getInstance().displayImage(item.getFace(), vh.avatar);
        vh.avatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UIHelper.showUserCenter(context,
                        item.getFriendId(), item.getFriendName());
            }
        });
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		ImageView avatar;
		TextView name, sender, time,count;
		TweetTextView content;

		ViewHolder(int viewType,View view) {
            super(viewType,view);
			avatar = (ImageView) view.findViewById(R.id.iv_avatar);
			sender = (TextView) view.findViewById(R.id.tv_sender);
			name = (TextView) view.findViewById(R.id.tv_name);
			time = (TextView) view.findViewById(R.id.tv_time);
			count = (TextView) view.findViewById(R.id.tv_count);
			content = (TweetTextView) view.findViewById(R.id.tv_content);
		}
	}
}
