package net.oschina.app.v2.activity.image;

import java.io.IOException;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.image.SamplePagerAdapter.OnPagerClickListener;
import net.oschina.app.v2.base.BaseActivity;
import net.oschina.app.v2.base.Constants;
import net.oschina.app.v2.utils.ImageUtils;
import net.oschina.app.v2.utils.TLog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;
import com.umeng.analytics.MobclickAgent;

public class ImagePreviewActivity extends BaseActivity implements
		OnPageChangeListener, OnPagerClickListener {
    public static final String TAG = ImagePreviewActivity.class.getSimpleName();
	public static final String BUNDLE_KEY_IMAGES = "bundle_key_images";
	private static final String BUNDLE_KEY_INDEX = "bundle_key_index";
	private static final String IMAGE_PREVIEW_SCREEN = "image_preview_screen";
	private HackyViewPager mViewPager;
	private SamplePagerAdapter mAdapter;
	private int mCurrentPostion = 0;
	private String[] mImageUrls;

	public static void showImagePrivew(Context context, int index,
			String[] images) {
		Intent intent = new Intent(context, ImagePreviewActivity.class);
		intent.putExtra(BUNDLE_KEY_IMAGES, images);
		intent.putExtra(BUNDLE_KEY_INDEX, index);
		context.startActivity(intent);
	}

	@Override
	protected boolean hasBackButton() {
		return true;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.v2_activity_image_preview;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image_menu, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.image_menu_download:
			try {
				if (mAdapter.getCount() > 0) {
					String filePath = Constants.IMAGE_SAVE_PAHT
							+ ImageUtils.getTempFileName() + ".jpg";
					ImageUtils.saveImageToSD(
							this,
							filePath,
							ImageLoader.getInstance().getMemoryCache()
									.get(mAdapter.getItem(mCurrentPostion)),
							100);
					AppContext.showToastShort(getString(
							R.string.tip_save_image_suc, filePath));
				}
			} catch (IOException e) {
				e.printStackTrace();
				AppContext.showToastShort(R.string.tip_save_image_faile);
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		super.init(savedInstanceState);
		mViewPager = (HackyViewPager) findViewById(R.id.view_pager);

		mImageUrls = getIntent().getStringArrayExtra(BUNDLE_KEY_IMAGES);
		int index = getIntent().getIntExtra(BUNDLE_KEY_INDEX, 0);

        for(String s:mImageUrls) {
            TLog.log(TAG,"url:"+s);
        }
        
		mAdapter = new SamplePagerAdapter(mImageUrls, this);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setCurrentItem(index);
		mViewPager.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TLog.log("xxxx");
			}
		});
		onPageSelected(index);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int idx) {
		mCurrentPostion = idx;
		if (mImageUrls != null && mImageUrls.length > 1) {
			setActionBarTitle((mCurrentPostion + 1) + "/" + mImageUrls.length);
		}
	}

	@Override
	public void onPagerClick() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar.isShowing()) {
			actionBar.hide();
		} else {
			actionBar.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(IMAGE_PREVIEW_SCREEN);
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(IMAGE_PREVIEW_SCREEN);
		MobclickAgent.onPause(this);
	}
}
