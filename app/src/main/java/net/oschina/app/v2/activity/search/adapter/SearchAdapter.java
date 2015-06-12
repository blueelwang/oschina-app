package net.oschina.app.v2.activity.search.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.SearchList.Result;
import net.oschina.app.v2.utils.DateUtil;

public class SearchAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_news, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        Result item = (Result) _data.get(position);

        vh.title.setText(item.getTitle());
        vh.source.setText(item.getAuthor());
        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));
        vh.tip.setVisibility(View.GONE);
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		public TextView title, source, time;
		public ImageView tip;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			time = (TextView) view.findViewById(R.id.tv_time);
			tip = (ImageView) view.findViewById(R.id.iv_tip);
		}
	}
}
