package net.oschina.app.v2.activity.software.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.SoftwareList.Software;

public class SoftwareAdapter extends RecycleBaseAdapter {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_software, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType, view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;
        Software item = (Software) _data.get(position);
        vh.name.setText(item.name);
        vh.desc.setText(item.description);
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder{
		public TextView name, desc;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			name = (TextView) view.findViewById(R.id.tv_name);
			desc = (TextView) view.findViewById(R.id.tv_desc);
		}
	}
}
