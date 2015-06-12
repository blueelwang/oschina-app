package net.oschina.app.v2.activity.news.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.News;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.StringUtils;

public class NewsRecycleAdapter extends RecycleBaseAdapter {


    public NewsRecycleAdapter() {
    }

    public NewsRecycleAdapter(View headerView) {
        mHeaderView = headerView;
    }

    @Override
    public View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_news, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder vh, int position) {
        super.onBindItemViewHolder(vh, position);
        NewsRecycleAdapter.ViewHolder holder = (NewsRecycleAdapter.ViewHolder) vh;
        News news = (News) _data.get(position);
        holder.title.setText(news.getTitle());

        holder.title.setTextColor(holder.title.getResources()
                .getColor(AppContext.isReadedNews(news.getId()) ? R.color.main_gray : R.color.main_black));


        holder.body.setText(news.getBody());
        if (TextUtils.isEmpty(news.getBody()))
            holder.body.setVisibility(View.GONE);
        else
            holder.body.setVisibility(View.VISIBLE);
        holder.source.setText(news.getAuthor());
        holder.time.setText(DateUtil.getFormatTime(news.getPubDate()));
        //StringUtils.friendly_time(news.getPubDate())
        if (StringUtils.isToday(news.getPubDate())) {
            holder.tip.setVisibility(View.VISIBLE);
            holder.tip.setImageResource(R.drawable.ic_today);
        } else {
            holder.tip.setVisibility(View.GONE);
        }
        holder.commentCount.setText(holder.commentCount.getContext().getResources()
                .getString(R.string.comment_count, news.getCommentCount()));
    }

    public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        public TextView title, source, time, commentCount, body;
        public ImageView tip;

        public ViewHolder(int viewType, View view) {
            super(viewType, view);
            title = (TextView) view.findViewById(R.id.tv_title);
            body = (TextView) view.findViewById(R.id.tv_body);
            source = (TextView) view.findViewById(R.id.tv_source);
            time = (TextView) view.findViewById(R.id.tv_time);
            commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
            tip = (ImageView) view.findViewById(R.id.iv_tip);
        }
    }
}
