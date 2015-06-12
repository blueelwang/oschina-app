package net.oschina.app.v2.activity.favorite.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.FavoriteList.Favorite;

public class FavoriteAdapter extends RecycleBaseAdapter {

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_simple_text, null);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        Favorite item = (Favorite) _data.get(position);
        vh.content.setText(item.title);
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		public TextView content;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			content = (TextView) view.findViewById(R.id.tv_content);
		}
	}
}
