package com.hochan.dragtofloatvideoview.video.videolayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hochan.dragtofloatvideoview.GlideApp;
import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.databinding.LayoutVideoPlayBinding;
import com.hochan.dragtofloatvideoview.video.VideoTextureView;
import com.hochan.dragtofloatvideoview.video.player.VideoPlayer;
import com.hochan.dragtofloatvideoview.video.videocontrol.DefaultVideoControl;
import com.hochan.dragtofloatvideoview.video.videocontrol.VideoControl;

/**
 * .
 * Created by hochan on 2018/1/25.
 */

@SuppressWarnings("unused")
public class VideoPlayLayout extends FrameLayout implements VideoPlayer.OnPlayInfoListener,
		View.OnClickListener, GestureDetector.OnGestureListener, VideoControl.OnVideoControlListener, VideoTextureView.OnVideoTextureListener {

	public static final int MIN_VIDEO_HEIGHT_IN_DIP = 80;
	public static final int MIN_VIDEO_WIDTH_IN_DIP = 100;
	public static final int MINI_LAYOUT_MARGIN_IN_DIP = 15;

	protected VideoControl mVideoControl;

	private GestureDetector mGestureDetector;

	public LayoutVideoPlayBinding mVideoPlayBinding;

	public boolean mIsSurfaceAttach = false;

	private OnSingleTapListener mOnSingleTapListener;

	public String mVideoUrl;
	private String mThumbnailUrl;

	private boolean mChangingPosition = false;

	public VideoPlayLayout(@NonNull Context context) {
		this(context, null);
	}

	public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		View view = LayoutInflater.from(context).inflate(R.layout.layout_video_play, null, false);
		addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mVideoPlayBinding = LayoutVideoPlayBinding.bind(view);
		mVideoPlayBinding.videoTextureView.setVideoTextureListener(this);

		mVideoPlayBinding.ivVideoCover.setOnClickListener(this);

		mGestureDetector = new GestureDetector(context, this);

		mVideoControl = new DefaultVideoControl(getContext());
		mVideoControl.setOnVideoControlListener(this);
		setVideoControl(mVideoControl);
	}

	public void setVideoControl(VideoControl videoControl) {
		if (mVideoControl != null) {
			removeView(mVideoControl);
		}
		mVideoControl = videoControl;
		addView(mVideoControl);
	}

	public void setVideoUrl(String url) {
		mVideoUrl = url;
	}

	/**
	 * 开始播放
	 */
	public void play() {
		if (!TextUtils.isEmpty(mVideoUrl)) {
			VideoPlayer.getInstance(getContext()).play(this);
			if (!mIsSurfaceAttach) {
				attachToVideoPlayer();
			}
		}
	}

	/**
	 * 设置视频地址和封面地址
	 *
	 * @param videoUrl     视频地址
	 * @param thumbnailUrl 封面地址
	 */
	public void setData(String videoUrl, String thumbnailUrl) {
		mIsSurfaceAttach = false;
		mVideoUrl = videoUrl;
		mThumbnailUrl = thumbnailUrl;
		mVideoPlayBinding.ivVideoCover.setVisibility(VISIBLE);
		GlideApp.with(getContext()).load(thumbnailUrl).into(mVideoPlayBinding.ivVideoCover);
	}

	public void setBackgroundTransparent() {
		mVideoPlayBinding.flVideoLayoutRoot.setBackgroundColor(Color.TRANSPARENT);
	}

	/**
	 * 播放状态改变
	 */
	@Override
	public void onPlayStateChange() {
		System.out.println("VideoPlayLayout" + ".onPlayStateChange" + " " + VideoPlayer.getInstance(getContext()).getCurrentState());
		switch (VideoPlayer.getInstance(getContext()).getCurrentState()) {
			case VideoPlayer.STATE_PLAYING: {
				mVideoPlayBinding.ivVideoCover.setVisibility(INVISIBLE);
				break;
			}
		}
		if (mVideoControl != null) {
			mVideoControl.onPlayStateChange();
		}
	}

	/**
	 * 当前VideoLayout被替换
	 */
	@Override
	public void onSurfaceReplace() {
		mIsSurfaceAttach = false;
		mVideoControl.stopSeekbarUpdate();
		mVideoControl.hideProgressBar();
		mVideoControl.hideLoadingIndicator();
	}

	@Override
	public void onVideoSizeChange(int width, int height) {
		mVideoPlayBinding.videoTextureView.setVideoSize(width, height);
	}

	@Override
	public void onBufferingUpdate(int percent) {

	}

	@Override
	public void onPlayError(Exception e) {

	}

	public String getVideoUrl() {
		return mVideoUrl;
	}

	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}

	public Bitmap getCurrentFrame() {
		return mVideoPlayBinding.videoTextureView.getBitmap();
	}

	public ImageView getThumbnailImageView() {
		return mVideoPlayBinding.ivVideoCover;
	}

	public VideoTextureView getVideoTextureView() {
		return mVideoPlayBinding.videoTextureView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mVideoPlayBinding.ivVideoCover.getId()) {
			handlePlayPauseRequest();
		}
	}

	public void pause() {
		if (isVideoPlayActive()) {
			VideoPlayer.getInstance(getContext()).pause();
		}
	}

	private void handlePlayPauseRequest() {
		if (!isVideoPlayActive()) {
			play();
			return;
		}

		switch (VideoPlayer.getInstance(getContext()).getCurrentState()) {
			case VideoPlayer.STATE_ERROR:
			case VideoPlayer.STATE_NONE:
			case VideoPlayer.STATE_IDLE:
			case VideoPlayer.STATE_PAUSED:
			case VideoPlayer.STATE_BUFFERING_PAUSED:
			case VideoPlayer.STATE_COMPLETED: {
				play();
				break;
			}
			case VideoPlayer.STATE_PLAYING:
			case VideoPlayer.STATE_BUFFERING_PLAYING: {
				VideoPlayer.getInstance(getContext()).pause();
				break;
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if (mChangingPosition) {
					mVideoControl.seekTo(mVideoControl.getSeekBarProgress());
				}
				requestDisallowInterceptTouchEvent(false);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		mChangingPosition = false;
		requestDisallowInterceptTouchEvent(false);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (isVideoPlayActive()) {
			if (mVideoControl.getVideoControlVisibility() == VISIBLE) {
				boolean handled = false;
				if (mOnSingleTapListener != null) {
					handled = mOnSingleTapListener.onSingleTap(this);
				}
				if (!handled) {
					mVideoControl.hide();
				}
			} else {
				if (mVideoControl != null) {
					mVideoControl.show();
				}
			}
		} else {
			play();
		}
		return true;
	}

	public boolean scrollToChangePosition() {
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (!mChangingPosition) {
			if (Math.abs(distanceX) > Math.abs(distanceY) && isVideoPlayActive() && scrollToChangePosition()) {
				requestDisallowInterceptTouchEvent(true);
				mChangingPosition = true;
				mVideoControl.startSeek();
			} else {
				return false;
			}
		}

		if (mChangingPosition) {
			int progress = mVideoControl.getSeekBarProgress();
			int duration = VideoPlayer.getInstance(getContext()).getDuration();
			if (duration > 0) {
				mVideoControl.setProgress((int) (progress - distanceX * duration / getMeasuredWidth()));
			}
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	public void attachToVideoPlayer() {
		mVideoPlayBinding.videoTextureView.attachToVideoPlayer();
	}

	public void setVideoSize(int width, int height) {
		mVideoPlayBinding.videoTextureView.setVideoSize(width, height);
	}

	public int getVideoTextureWidth() {
		return mVideoPlayBinding.videoTextureView.getWidth();
	}

	public int getVideoTextureHeight() {
		return mVideoPlayBinding.videoTextureView.getHeight();
	}

	public float getVideoTextureW2HRatio() {
		return mVideoPlayBinding.videoTextureView.getW2HRatio();
	}

	public void resetVideoTexture() {
		mVideoPlayBinding.flTextureContainer.removeView(mVideoPlayBinding.videoTextureView);
		mVideoPlayBinding.flTextureContainer.addView(mVideoPlayBinding.videoTextureView);
	}

	public void setOnSingleTapListener(OnSingleTapListener listener) {
		mOnSingleTapListener = listener;
	}

	@Override
	public boolean isVideoPlayActive() {
		return this == VideoPlayer.getInstance(getContext()).getVideoPlayLayout();
	}

	@Override
	public void onVisibilityChange(boolean visible) {
	}

	public float getW2HRatio() {
		return mVideoPlayBinding.videoTextureView.getW2HRatio();
	}

	@Override
	public void onPlayPauseClicked() {
		handlePlayPauseRequest();
	}

	public VideoControl getVideoControl() {
		return mVideoControl;
	}

	@Override
	public void onSurfaceDestroyed() {
		mVideoPlayBinding.ivVideoCover.setVisibility(VISIBLE);
		mVideoControl.pause();
		VideoPlayer.getInstance(getContext()).mVideoPositionLRUCache.put(mVideoUrl, VideoPlayer.getInstance(getContext()).getCurrentPosition());
		if (isVideoPlayActive()) {
			VideoPlayer.getInstance(getContext()).pause();
		}
	}

	@Override
	public void onSurfaceAttach() {
		mIsSurfaceAttach = true;
	}

	@Override
	public boolean shouldAttachOnAvailable() {
		return isVideoPlayActive();
	}

	public interface OnSingleTapListener {
		boolean onSingleTap(VideoPlayLayout videoPlayLayout);
	}
}
