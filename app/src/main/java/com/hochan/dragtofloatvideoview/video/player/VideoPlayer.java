package com.hochan.dragtofloatvideoview.video.player;

import android.content.Context;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Surface;

import com.devbrackets.android.exomedia.util.DeviceUtil;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

import java.lang.ref.WeakReference;

/**
 * .
 * <p>
 * Created by hochan on 2018/6/26.
 */

@SuppressWarnings("WeakerAccess")
public abstract class VideoPlayer {

	public static final int STATE_ERROR = -1;            // 播放错误
	public static final int STATE_NONE = 0;              // 播放错误
	public static final int STATE_IDLE = 1;              // 播放未开始
	public static final int STATE_PREPARING = 2;         // 播放准备中
	public static final int STATE_PREPARED = 3;          // 播放准备就绪
	public static final int STATE_PLAYING = 4;           // 正在播放
	public static final int STATE_PAUSED = 5;            // 暂停播放
	public static final int STATE_BUFFERING_PLAYING = 6;
	public static final int STATE_BUFFERING_PAUSED = 7;
	public static final int STATE_COMPLETED = 8;         // 播放完成

	public LruCache<String, Integer> mVideoPositionLRUCache = new LruCache<>(500);

	String mPlayingUrl;
	volatile int mCurrentState;
	long mCurrentPosition;
	WeakReference<VideoPlayLayout> mPlayLayoutWeakReference;

	public static VideoPlayer getInstance(Context context) {
		if (new DeviceUtil().supportsExoPlayer(context.getApplicationContext())) {
			return ExoVideoPlayer.getInstance();
		} else {
			return NativeVideoPlayer.getInstance();
		}
	}

	public void play(VideoPlayLayout videoPlayLayout) {
		createMediaPlayerIfNecessary(videoPlayLayout.getContext());
		checkIsVideoUrlSame(videoPlayLayout);
	}

	protected void checkIsVideoUrlSame(VideoPlayLayout videoPlayLayout) {
		if (TextUtils.isEmpty(mPlayingUrl) || !TextUtils.equals(mPlayingUrl, videoPlayLayout.mVideoUrl)) {
			changePlayUrl(videoPlayLayout);
		} else {
			changeVideoLayout(videoPlayLayout);
		}
	}

	/**
	 * 播放地址不变，无缝切换播放器
	 *
	 * @param videoPlayLayout 新的播放器
	 */
	private void changeVideoLayout(VideoPlayLayout videoPlayLayout) {
		if (mPlayLayoutWeakReference != null && mPlayLayoutWeakReference.get() != null
				&& mPlayLayoutWeakReference.get() != videoPlayLayout) {
			int tmpState = mCurrentState;
			mCurrentState = STATE_PAUSED;
			notifyPlayStateChange();
			notifySurfaceReplace();
			videoPlayLayout.setVideoSize(mPlayLayoutWeakReference.get().getVideoTextureWidth(),
					mPlayLayoutWeakReference.get().getVideoTextureHeight());
			mPlayLayoutWeakReference = new WeakReference<>(videoPlayLayout);
			videoPlayLayout.attachToVideoPlayer();
			mCurrentState = tmpState;
			notifyPlayStateChange();
		}
		if (mCurrentState == STATE_ERROR) {
			playUrl(videoPlayLayout);
		} else if (!isPlaying()) {
			start();
		}
	}

	/**
	 * 播放地址修改
	 *
	 * @param videoPlayLayout 播放器
	 */
	private void changePlayUrl(final VideoPlayLayout videoPlayLayout) {
		if (!TextUtils.isEmpty(mPlayingUrl)) {
			mVideoPositionLRUCache.put(mPlayingUrl, getCurrentPosition());
		}
		mCurrentState = STATE_PAUSED;
		notifyPlayStateChange();
		notifySurfaceReplace();
		playUrl(videoPlayLayout);
	}

	protected abstract void playUrl(VideoPlayLayout videoPlayLayout);

	protected abstract void createMediaPlayerIfNecessary(Context context);

	public abstract void start();

	public abstract void pause();

	public abstract void seekTo(int position);

	public synchronized void setSurface(Surface surface, VideoPlayLayout videoPlayLayout) {
		createMediaPlayerIfNecessary(videoPlayLayout.getContext());

		setSurfaceToPlayer(surface);
		if (mPlayLayoutWeakReference != null && mPlayLayoutWeakReference.get() != videoPlayLayout) {
			notifySurfaceReplace();
		}
		mPlayLayoutWeakReference = new WeakReference<>(videoPlayLayout);
		notifyPlayStateChange();
	}

	protected abstract void setSurfaceToPlayer(Surface surface);

	public abstract boolean isPlaying();

	public String getCurrentPlayUrl() {
		return mPlayingUrl;
	}

	public abstract int getDuration();

	public abstract int getCurrentPosition();

	public abstract int getCurrentState();

	protected void notifyPlayStateChange() {
		if (mPlayLayoutWeakReference != null && mPlayLayoutWeakReference.get() != null) {
			mPlayLayoutWeakReference.get().onPlayStateChange();
		}
	}

	protected void notifySurfaceReplace() {
		if (mPlayLayoutWeakReference != null && mPlayLayoutWeakReference.get() != null) {
			mPlayLayoutWeakReference.get().onSurfaceReplace();
		}
	}

	public VideoPlayLayout getVideoPlayLayout() {
		return mPlayLayoutWeakReference != null ? mPlayLayoutWeakReference.get() : null;
	}

	@SuppressWarnings("unused")
	public interface OnPlayInfoListener {
		void onPlayStateChange();

		void onSurfaceReplace();

		void onVideoSizeChange(int width, int height);

		void onBufferingUpdate(int percent);

		void onPlayError(Exception e);
	}
}
