package net.oschina.app.v2.activity.tweet.adapter;

import net.oschina.app.v2.base.ListBaseAdapter;
import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.Tweet;
import net.oschina.app.v2.ui.AvatarView;
import net.oschina.app.v2.ui.text.MyLinkMovementMethod;
import net.oschina.app.v2.ui.text.MyURLSpan;
import net.oschina.app.v2.ui.text.TweetTextView;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.UIHelper;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;

public class TweetAdapter extends RecycleBaseAdapter {

	private DisplayImageOptions options;

	public TweetAdapter() {
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_tweet, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;

        final Tweet item = (Tweet) _data.get(position);
        vh.name.setText(item.getAuthor());

        vh.title.setMovementMethod(MyLinkMovementMethod.a());
        vh.title.setFocusable(false);
        vh.title.setDispatchToParent(true);
        vh.title.setLongClickable(false);
        Spanned span = Html.fromHtml(item.getBody());
        vh.title.setText(span);
        MyURLSpan.parseLinkText(vh.title, span);

        vh.time.setText(DateUtil.getFormatTime(item.getPubDate()));

        switch (item.getAppClient()) {
            default:
                vh.from.setText("");
                break;
            case Tweet.CLIENT_MOBILE:
                vh.from.setText(R.string.from_mobile);
                break;
            case Tweet.CLIENT_ANDROID:
                vh.from.setText(R.string.from_android);
                break;
            case Tweet.CLIENT_IPHONE:
                vh.from.setText(R.string.from_iphone);
                break;
            case Tweet.CLIENT_WINDOWS_PHONE:
                vh.from.setText(R.string.from_windows_phone);
                break;
            case Tweet.CLIENT_WECHAT:
                vh.from.setText(R.string.from_wechat);
                break;
        }

        vh.commentCount.setText(String.valueOf(item.getCommentCount()));

        vh.avatar.setUserInfo(item.getAuthorId(), item.getAuthor());
        vh.avatar.setAvatarUrl(item.getFace());

        vh.longPicTip.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(item.getImgSmall())) {
            vh.pic.setVisibility(View.VISIBLE);
            final View longPicTip = vh.longPicTip;
            ImageLoader.getInstance().displayImage(item.getImgSmall(), vh.pic,
                    options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            ImageView v = (ImageView) view;
                            if (loadedImage.getHeight() > 4 * loadedImage
                                    .getWidth()) {
                                v.setImageResource(R.drawable.ic_long_picture);
                                longPicTip.setVisibility(View.VISIBLE);
                            } else {
                                longPicTip.setVisibility(View.GONE);
                            }
                        }
                    });
            final Context context = vh.pic.getContext();
            vh.pic.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // UIHelper.showImageZoomDialog(parent.getContext(),
                    // item.getImgBig());
                    UIHelper.showImagePreview(context,
                            new String[] { item.getImgBig() });
                }
            });
        } else {
            vh.pic.setVisibility(View.GONE);
            vh.pic.setImageBitmap(null);
        }
    }

	static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
		public TextView name, from, time, commentCount, longPicTip;
		public TweetTextView title;
		public ImageView pic;
		public AvatarView avatar;

		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			longPicTip = (TextView) view.findViewById(R.id.long_pic_tip);
			name = (TextView) view.findViewById(R.id.tv_name);
			title = (TweetTextView) view.findViewById(R.id.tv_title);
			from = (TextView) view.findViewById(R.id.tv_from);
			time = (TextView) view.findViewById(R.id.tv_time);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
			pic = (ImageView) view.findViewById(R.id.iv_pic);
		}
	}
}
