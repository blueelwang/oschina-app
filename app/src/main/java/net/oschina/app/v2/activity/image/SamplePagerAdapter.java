package net.oschina.app.v2.activity.image;

import net.oschina.app.v2.adapter.RecyclingPagerAdapter;
import uk.co.senab.photoview.PhotoView;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.tonlin.osc.happy.R;

public class SamplePagerAdapter extends RecyclingPagerAdapter {

	private String[] images = new String[] {};
	private DisplayImageOptions options;
	private OnPagerClickListener mListener;

	public interface OnPagerClickListener {
		public void onPagerClick();
	}

	SamplePagerAdapter(String[] images, OnPagerClickListener lis) {
		mListener = lis;
		this.images = images;
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.postProcessor(new BitmapProcessor() {

					@Override
					public Bitmap process(Bitmap arg0) {
						return arg0;
					}
				}).cacheOnDisk(true).build();
	}

	public String getItem(int position) {
		return images[position];
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		ViewHolder vh = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(container.getContext())
					.inflate(R.layout.v2_image_preview_item, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		final ProgressBar bar = vh.progress;
		bar.setVisibility(View.GONE);
		ImageLoader.getInstance().displayImage(images[position], vh.image,
				options, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// bar.show();
						bar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingComplete(String imageUri,
							View view, Bitmap loadedImage) {
						// bar.hide();
						bar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						bar.setVisibility(View.GONE);
					}
				});
		vh.image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPagerClick();
				}
			}
		});
		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPagerClick();
				}
			}
		});
		return convertView;
	}

	static class ViewHolder {
		PhotoView image;
		ProgressBar progress;

		ViewHolder(View view) {
			image = (PhotoView) view.findViewById(R.id.photoview);
			progress = (ProgressBar) view.findViewById(R.id.progress);
		}
	}
}