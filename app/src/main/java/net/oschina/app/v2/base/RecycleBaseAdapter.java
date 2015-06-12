package net.oschina.app.v2.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.TLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class RecycleBaseAdapter extends RecyclerView.Adapter<RecycleBaseAdapter.ViewHolder> {

    public static final int STATE_EMPTY_ITEM = 0;
    public static final int STATE_LOAD_MORE = 1;
    public static final int STATE_NO_MORE = 2;
    public static final int STATE_NO_DATA = 3;
    public static final int STATE_LESS_ONE_PAGE = 4;
    public static final int STATE_NETWORK_ERROR = 5;

    public static final int TYPE_FOOTER = 0x101;
    public static final int TYPE_HEADER = 0x102;

    protected int state = STATE_LESS_ONE_PAGE;

    protected int _loadmoreText;
    protected int _loadFinishText;
    protected int mScreenWidth;

    private LayoutInflater mInflater;


    @SuppressWarnings("rawtypes")
    protected ArrayList _data = new ArrayList();

    private WeakReference<OnItemClickListener> mListener;
    private WeakReference<OnItemLongClickListener> mLongListener;
    protected View mHeaderView;


    public interface OnItemClickListener {
        public void onItemClick(View view);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClick(View view);
    }

    protected LayoutInflater getLayoutInflater(Context context) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return mInflater;
    }

    public void setScreenWidth(int width) {
        mScreenWidth = width;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public RecycleBaseAdapter() {
        _loadmoreText = R.string.loading;
        _loadFinishText = R.string.loading_no_more;
    }

    @Override
    public int getItemCount() {
        int size = getDataSize();
        if (hasFooter()) {
            size += 1;
        }
        if (hasHeader()) {
            size += 1;
        }
        //TLog.log("get item count:"+size +" data size:"+getDataSize());
        return size;
    }

    public int getDataSize() {
        return _data.size();
    }

    public Object getItem(int arg0) {
        if (arg0 < 0)
            return null;
        if (_data.size() > arg0) {
            return _data.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @SuppressWarnings("rawtypes")
    public void setData(ArrayList data) {
        _data = data;
        notifyDataSetChanged();
    }

    @SuppressWarnings("rawtypes")
    public ArrayList getData() {
        return _data == null ? (_data = new ArrayList()) : _data;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addData(List data) {
        if (_data == null) {
            _data = new ArrayList();
        }
        _data.addAll(data);
        notifyDataSetChanged();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addItem(Object obj) {
        if (_data == null) {
            _data = new ArrayList();
        }
        _data.add(obj);
        notifyDataSetChanged();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addItem(int pos, Object obj) {
        if (_data == null) {
            _data = new ArrayList();
        }
        _data.add(pos, obj);
        notifyDataSetChanged();
    }

    public void removeItem(Object obj) {
        _data.remove(obj);
        notifyDataSetChanged();
    }

    public void clear() {
        _data.clear();
        notifyDataSetChanged();
    }

    public void setLoadmoreText(int loadmoreText) {
        _loadmoreText = loadmoreText;
    }

    public void setLoadFinishText(int loadFinishText) {
        _loadFinishText = loadFinishText;
    }

    protected boolean loadMoreHasBg() {
        return true;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = new WeakReference<OnItemClickListener>(listener);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mLongListener = new WeakReference<OnItemLongClickListener>(listener);
    }

    public boolean hasHeader() {
        return mHeaderView != null;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
    }

    private boolean hasFooter() {
        switch (getState()) {
            case STATE_EMPTY_ITEM:
            case STATE_LOAD_MORE:
            case STATE_NO_MORE:
            case STATE_NETWORK_ERROR:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            // TLog.log("getItemViewType: hasHeader");
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1 && hasFooter()) {
            return TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TLog.log("onCreateViewHolder: viewType:" + viewType);
        ViewHolder vh;
        if (viewType == TYPE_FOOTER) {
            View v = getLayoutInflater(parent.getContext())
                    .inflate(R.layout.v2_list_cell_footer, null);
            vh = new FooterViewHolder(viewType, v);
        } else if (viewType == TYPE_HEADER) {
            if (mHeaderView == null) {
                throw new RuntimeException("Header view is null");
            }
            vh = new HeaderViewHolder(viewType, mHeaderView);
        } else {
            final View itemView = onCreateItemView(parent, viewType);
            if (itemView != null) {
                if (mListener != null)
                    itemView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            OnItemClickListener lis = mListener.get();
                            if (lis != null) {
                                lis.onItemClick(itemView);
                            }
                        }
                    });
                if (mLongListener != null)
                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            OnItemLongClickListener lis = mLongListener.get();
                            if (lis != null) {
                                return lis.onItemLongClick(itemView);
                            }
                            return false;
                        }
                    });
            }
            vh = onCreateItemViewHolder(itemView, viewType);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //TLog.log("onBindViewHolder:"+holder.viewType +" pos:"+position+" "+ holder);
        if ((getItemViewType(position) == TYPE_HEADER && position == 0)
                || holder instanceof HeaderViewHolder) {
            //TLog.log("bind Header view:" + position + " " + holder.viewType);
            onBindHeaderViewHolder(holder, position);
        } else if ((getItemViewType(position) == TYPE_FOOTER
                && position == getItemCount() - 1) || holder instanceof FooterViewHolder) {
            //TLog.log("bind Footer view:" + position + " " + holder.viewType);
            onBindFooterViewHolder(holder, position);
        } else {
            //TLog.log("bind item view:" + position + " " + holder.viewType);
            onBindItemViewHolder(holder, hasHeader() ? position -1:position);
        }
    }

    private void onBindFooterViewHolder(ViewHolder holder, int position) {
        FooterViewHolder vh = (FooterViewHolder) holder;
        if (!loadMoreHasBg()) {
            vh.loadmore.setBackgroundDrawable(null);
        }
        switch (getState()) {
            case STATE_LOAD_MORE:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.VISIBLE);
                vh.text.setVisibility(View.VISIBLE);
                vh.text.setText(_loadmoreText);
                break;
            case STATE_NO_MORE:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.VISIBLE);
                vh.text.setText(_loadFinishText);
                break;
            case STATE_EMPTY_ITEM:
                vh.progress.setVisibility(View.GONE);
                vh.loadmore.setVisibility(View.GONE);
                vh.text.setVisibility(View.GONE);
                break;
            case STATE_NETWORK_ERROR:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.VISIBLE);
                if (TDevice.hasInternet()) {
                    vh.text.setText(AppContext.string(R.string.tip_load_data_error));
                } else {
                    vh.text.setText(AppContext.string(R.string.tip_network_error));
                }
                break;
            default:
                vh.loadmore.setVisibility(View.GONE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.GONE);
                break;
        }
    }

    protected abstract View onCreateItemView(ViewGroup parent, int viewType);

    protected abstract ViewHolder onCreateItemViewHolder(View view, int viewType);

//    private ViewHolder onCreateHeaderViewHolder(View headerView, int viewType) {
//        if (hasHeader()) {
//            throw new RuntimeException("hasHeader return true, you must implement onCreateHeaderViewHolder");
//        }
//        //TODO do nothing...
//        return null;
//    }

    protected void onBindHeaderViewHolder(ViewHolder holder, int position) {
        //TODO do nothing...
    }

    protected void onBindItemViewHolder(ViewHolder holder, int position) {
        //TODO do nothing...
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int viewType;

        public ViewHolder(int viewType, View v) {
            super(v);
            this.viewType = viewType;
        }
    }

    public static class HeaderViewHolder extends ViewHolder {

        public HeaderViewHolder(int viewType, View v) {
            super(viewType, v);
        }
    }

    public static class FooterViewHolder extends ViewHolder {
        public ProgressBar progress;
        public TextView text;
        public View loadmore;

        public FooterViewHolder(int viewType, View v) {
            super(viewType, v);
            loadmore = v;
            progress = (ProgressBar) v.findViewById(R.id.progressbar);
            text = (TextView) v.findViewById(R.id.text);
        }
    }
}
