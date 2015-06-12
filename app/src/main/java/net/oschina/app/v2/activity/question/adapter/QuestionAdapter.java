package net.oschina.app.v2.activity.question.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Post;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.utils.DateUtil;

public class QuestionAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_post,null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        final Post item = (Post) _data.get(position);

        vh.title.setText(item.getTitle());

        vh.title.setTextColor(vh.title.getResources()
                .getColor(AppContext.isReadedQuestion(item.getId()) ? R.color.main_gray : R.color.main_black));

        vh.source.setText(item.getAuthor());
        vh.avCount.setText(item.getAnswerCount()+"/"+item.getViewCount());
        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));

        vh.avatar.setUserInfo(item.getAuthorId(), item.getAuthor());
        vh.avatar.setAvatarUrl(item.getFace());

        vh.body.setText(item.getBody());
        if(TextUtils.isEmpty(item.getBody()))
            vh.body.setVisibility(View.GONE);
        else
            vh.body.setVisibility(View.VISIBLE);
    }

    static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
		public TextView title, source,avCount, time,body;
		public AvatarView avatar;
		public ViewHolder(int viewType,View view) {
            super(viewType,view);
            body = (TextView) view.findViewById(R.id.tv_body);
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			avCount = (TextView) view.findViewById(R.id.tv_answer_view_count);
			time = (TextView) view.findViewById(R.id.tv_time);
			avatar = (AvatarView)view.findViewById(R.id.iv_avatar);
		}
	}
}
