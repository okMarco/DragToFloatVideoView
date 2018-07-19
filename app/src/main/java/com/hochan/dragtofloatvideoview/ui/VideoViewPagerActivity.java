package com.hochan.dragtofloatvideoview.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.ViewUtils;
import com.hochan.dragtofloatvideoview.model.VideoData;
import com.hochan.dragtofloatvideoview.statusbar.StatusBarCompat;
import com.hochan.dragtofloatvideoview.video.MiniVideoWindowManager;
import com.hochan.dragtofloatvideoview.widget.TikTokViewPager;

public class VideoViewPagerActivity extends AppCompatActivity implements MiniVideoWindowManager.OnMiniWindowListener {

	public static final String EXTRA_VIDEO_DATA = "video_data";
	public static final String EXTRA_SCROLL_TO_INDEX = "scroll_to_index";

	private TikTokViewPager mViewPager;
	private FullViewVideoListFragment mFullViewVideoListFragment;

	public static void playVideo(Activity context, VideoData videoData) {
		Intent intent = new Intent(context, VideoViewPagerActivity.class);
		intent.putExtra(EXTRA_VIDEO_DATA, videoData);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_view_pager);

		StatusBarCompat.setStatusBarTranslucent(getWindow());
		StatusBarCompat.setNavigationBarTranslucent(getWindow());

		mViewPager = findViewById(R.id.view_pager);
		mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				switch (position) {
					case 0: {
						mFullViewVideoListFragment = FullViewVideoListFragment.newInstance((VideoData) getIntent().getSerializableExtra(EXTRA_VIDEO_DATA));
						return mFullViewVideoListFragment;
					}
					case 1: {
						return new VideoListFragment();
					}
				}
				return null;
			}

			@Override
			public int getCount() {
				return 2;
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mViewPager.setCurrentItem(0, true);
		int scrollToPosition = intent.getIntExtra(EXTRA_SCROLL_TO_INDEX, -1);
		if (mFullViewVideoListFragment != null && scrollToPosition >= 0) {
			mFullViewVideoListFragment.scrollToVideo(scrollToPosition);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MiniVideoWindowManager.getInstance().registerOnEnterMiniWindowListener(this);
	}

	@Override
	public void onEnterMiniWindow() {
		moveTaskToBack(true);
	}

	@Override
	public void onMiniWindowDismiss() {
		finish();
	}

	@Override
	public void onRenterFromMiniWindow() {
		Intent intent = new Intent(getApplicationContext(), VideoViewPagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MiniVideoWindowManager.getInstance().unRegisterOnEnterMiniWindowListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mViewPager.setEnable(false);
			ViewUtils.setUiFlags(getWindow(), true);
		} else {
			mViewPager.setEnable(true);
			ViewUtils.setUiFlags(getWindow(), false);
		}
	}
}
