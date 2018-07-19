package com.hochan.dragtofloatvideoview.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hochan.dragtofloatvideoview.GlideApp;
import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.VideoTransitionUtils;
import com.hochan.dragtofloatvideoview.ViewUtils;
import com.hochan.dragtofloatvideoview.databinding.ActivityVideoListBinding;
import com.hochan.dragtofloatvideoview.model.VideoData;
import com.hochan.dragtofloatvideoview.statusbar.StatusBarCompat;
import com.hochan.dragtofloatvideoview.video.player.NativeVideoPlayer;
import com.hochan.dragtofloatvideoview.video.player.VideoPlayer;
import com.hochan.dragtofloatvideoview.video.videolayout.FullVideoPlayLayout;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

public class VideoListActivity extends AppCompatActivity implements VideoPlayLayout.OnSingleTapListener, FullVideoPlayLayout.OnFullScreenListener {

	public static final String EXTRA_START_TRANSITION_X = "start_transition_x";
	public static final String EXTRA_START_TRANSITION_Y = "start_transition_y";
	public static final String EXTRA_START_LAYOUT_WIDTH = "start_layout_width";
	public static final String EXTRA_START_LAYOUT_HEIGHT = "start_layout_height";
	public static final String EXTRA_VIDEO_URL = "video_url";

	private ActivityVideoListBinding mViewBinding;

	public static void playVideo(Activity context, VideoPlayLayout videoPlayLayout) {
		Intent intent = new Intent(context, VideoListActivity.class);
		int[] locationInScreen = new int[2];
		videoPlayLayout.getLocationOnScreen(locationInScreen);
		intent.putExtra(EXTRA_START_TRANSITION_X, locationInScreen[0]);
		intent.putExtra(EXTRA_START_TRANSITION_Y, locationInScreen[1]);
		intent.putExtra(EXTRA_START_LAYOUT_WIDTH, videoPlayLayout.mVideoPlayBinding.videoTextureView.getWidth());
		intent.putExtra(EXTRA_START_LAYOUT_HEIGHT, videoPlayLayout.mVideoPlayBinding.videoTextureView.getHeight());
		intent.putExtra(EXTRA_VIDEO_URL, videoPlayLayout.mVideoUrl);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = LayoutInflater.from(this).inflate(R.layout.activity_video_list, null, false);
		mViewBinding = ActivityVideoListBinding.bind(view);

		mViewBinding.llVideoList.setAlpha(0);
		VideoPlayLayout playingVideoLayout = VideoPlayer.getInstance(this).getVideoPlayLayout();
		if (playingVideoLayout != null) {
			VideoTransitionUtils.prepareVideoLayoutForEnterAnimation(playingVideoLayout, mViewBinding.fullVideoPlay);
			setContentView(view);
			ViewUtils.doAfterFadeIn(mViewBinding.fullVideoPlay, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					Rect endLocation = new Rect(0, 0, getResources().getDisplayMetrics().widthPixels,
							(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
					mViewBinding.llVideoList.animate().alpha(1);
					VideoTransitionUtils.startVideoLayoutAnimation(mViewBinding.fullVideoPlay, endLocation, new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mViewBinding.recyclerView.setTranslationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
							mViewBinding.recyclerView.animate().alpha(1).translationY(0);
							mViewBinding.topVideoLayout.setVideoUrl(mViewBinding.fullVideoPlay.mVideoUrl);
							mViewBinding.topVideoLayout.setVideoSize(mViewBinding.fullVideoPlay.getVideoTextureWidth(), mViewBinding.fullVideoPlay.getVideoTextureHeight());
							mViewBinding.topVideoLayout.attachToVideoPlayer();
							ViewUtils.doAfterFadeOut(mViewBinding.fullVideoPlay, new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									mViewBinding.fullVideoPlay.setVisibility(View.INVISIBLE);
								}
							});
						}
					});
				}
			});
		} else {
			setContentView(view);
		}
		StatusBarCompat.setStatusBarTranslucent(getWindow());

		initRecyclerView();

		mViewBinding.btnEnterMiniWindow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enterMiniWindow();
			}
		});

		mViewBinding.topVideoLayout.setOnSingleTapListener(this);
		mViewBinding.fullVideoPlay.setOnFullScreenListener(this);
	}

	private void initRecyclerView() {
		mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		mViewBinding.recyclerView.setAdapter(new RecyclerView.Adapter<ItemVideoViewHolder>() {
			@Override
			public ItemVideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new ItemVideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false));
			}

			@Override
			public void onBindViewHolder(final ItemVideoViewHolder holder, final int position) {
				GlideApp.with(VideoListActivity.this)
						.load(VideoData.VIDEO_DATA_LIST.get(position).getVideoThumbnail())
						.into(holder.ivVideoCover);
				holder.ivVideoCover.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (VideoPlayer.getInstance(VideoListActivity.this).getCurrentPlayUrl()
								.equals(VideoData.VIDEO_DATA_LIST.get(position).getVideoUrl())) {
							if (!VideoPlayer.getInstance(VideoListActivity.this).isPlaying()) {
								mViewBinding.topVideoLayout.play();
							}
						} else {
							mViewBinding.topVideoLayout.setData(
									VideoData.VIDEO_DATA_LIST.get(position).getVideoUrl(),
									VideoData.VIDEO_DATA_LIST.get(position).getVideoThumbnail());
							mViewBinding.topVideoLayout.resetVideoTexture();
							mViewBinding.topVideoLayout.play();
						}
					}
				});
			}

			@Override
			public int getItemCount() {
				return VideoData.VIDEO_DATA_LIST.size();
			}
		});
	}

	private void enterMiniWindow() {
		final VideoPlayLayout playingVideoLayout = findPlayingVideoLayout();
		if (playingVideoLayout != null) {
			mViewBinding.flTitleLayout.setVisibility(View.INVISIBLE);
			VideoTransitionUtils.prepareVideoLayoutForEnterAnimation(playingVideoLayout, mViewBinding.fullVideoPlay);
			ViewUtils.doAfterFadeIn(mViewBinding.fullVideoPlay, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (!(playingVideoLayout instanceof FullVideoPlayLayout)) {
						mViewBinding.llVideoList.animate().alpha(0);
					}
					mViewBinding.fullVideoPlay.initMiniWindowLocation();
					mViewBinding.fullVideoPlay.startEnterMiniWindowAnimation();
				}
			});
		}
	}

	@Nullable
	private VideoPlayLayout findPlayingVideoLayout() {
		VideoPlayLayout playingVideoLayout = mViewBinding.topVideoLayout.isVideoPlayActive() ? mViewBinding.topVideoLayout : null;

		if (playingVideoLayout == null) {
			if (mViewBinding.fullVideoPlay.isVideoPlayActive()) {
				playingVideoLayout = mViewBinding.fullVideoPlay;
			}
		}
		return playingVideoLayout;
	}

	@Override
	public boolean onSingleTap(VideoPlayLayout videoPlayLayout) {
		mViewBinding.fullVideoPlay.enterFullScreen(videoPlayLayout);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (!mViewBinding.fullVideoPlay.exitFullScreen()) {
			super.onBackPressed();
		}
	}

	@Override
	public void onEnterFullScreen() {
		mViewBinding.flTitleLayout.setVisibility(View.VISIBLE);
		mViewBinding.llVideoList.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onEnterMiniScreen() {
		mViewBinding.flTitleLayout.setVisibility(View.INVISIBLE);
		moveTaskToBack(true);
	}

	@Override
	public void onStartExitFullScreen() {
		mViewBinding.llVideoList.setAlpha(1);
		mViewBinding.llVideoList.setVisibility(View.VISIBLE);
	}

	@Override
	public void onExitFullScreen() {
		mViewBinding.topVideoLayout.attachToVideoPlayer();
	}

	@Override
	public void onMiniScreenDismiss() {
		finish();
	}

	public class ItemVideoViewHolder extends RecyclerView.ViewHolder {

		ImageView ivVideoCover;

		public ItemVideoViewHolder(View itemView) {
			super(itemView);
			ivVideoCover = itemView.findViewById(R.id.iv_video_cover);
		}
	}
}
