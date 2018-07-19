package com.hochan.dragtofloatvideoview.video.player;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.devbrackets.android.exomedia.core.exoplayer.ExoMediaPlayer;
import com.devbrackets.android.exomedia.core.listener.ExoPlayerListener;
import com.devbrackets.android.exomedia.core.listener.MetadataListener;
import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.metadata.Metadata;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

/**
 * .
 * <p>
 * Created by hochan on 2018/6/26.
 */

public class ExoVideoPlayer extends VideoPlayer implements ExoPlayerListener {

	public static ExoVideoPlayer getInstance() {
		return ExoVideoPlayer.Holder.INSTANCE;
	}

	protected static final class Holder {
		private static final ExoVideoPlayer INSTANCE = new ExoVideoPlayer();
	}

	protected ExoMediaPlayer mExoMediaPlayer;

	@Override
	protected void playUrl(VideoPlayLayout videoPlayLayout) {
		createMediaPlayerIfNecessary(videoPlayLayout.getContext());
		mExoMediaPlayer.setUri(Uri.parse(videoPlayLayout.getVideoUrl()));
		mExoMediaPlayer.setPlayWhenReady(true);
	}

	@Override
	protected void createMediaPlayerIfNecessary(Context context) {
		if (mExoMediaPlayer == null) {
			mExoMediaPlayer = new ExoMediaPlayer(context);
		}

		mExoMediaPlayer.setMetadataListener(new MetadataListener() {
			@Override
			public void onMetadata(Metadata metadata) {

			}
		});
		mExoMediaPlayer.setBufferUpdateListener(new OnBufferUpdateListener() {
			@Override
			public void onBufferingUpdate(int percent) {
				if (mPlayLayoutWeakReference.get() != null) {
					mPlayLayoutWeakReference.get().onBufferingUpdate(percent);
				}
			}
		});
		mExoMediaPlayer.addListener(this);
	}

	@Override
	public void start() {
		if (mExoMediaPlayer != null) {
			mExoMediaPlayer.setPlayWhenReady(true);
		}
	}

	@Override
	public void pause() {
		if (mExoMediaPlayer != null) {
			mExoMediaPlayer.setPlayWhenReady(false);
			notifyPlayStateChange();
		}
	}

	@Override
	public void seekTo(int position) {
		if (mExoMediaPlayer != null) {
			mExoMediaPlayer.seekTo(position);
		}
	}

	@Override
	protected void setSurfaceToPlayer(Surface surface) {
		mExoMediaPlayer.setSurface(surface);
	}

	@Override
	public void onStateChanged(boolean playWhenReady, int playbackState) {
		notifyPlayStateChange();
	}

	@Override
	public void onError(ExoMediaPlayer exoMediaPlayer, Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {
		if (mPlayLayoutWeakReference != null && mPlayLayoutWeakReference.get() != null) {
			mPlayLayoutWeakReference.get().onVideoSizeChange(width, height);
		}
	}

	@Override
	public void onSeekComplete() {

	}

	@Override
	public boolean isPlaying() {
		return mExoMediaPlayer != null && mExoMediaPlayer.getPlayWhenReady();
	}

	@Override
	public int getDuration() {
		return mExoMediaPlayer != null ? (int) mExoMediaPlayer.getDuration() : 0;
	}

	@Override
	public int getCurrentPosition() {
		return mExoMediaPlayer != null ? (int) mExoMediaPlayer.getCurrentPosition() : 0;
	}

	@Override
	public int getCurrentState() {
		if (mExoMediaPlayer == null) {
			return STATE_NONE;
		}
		switch (mExoMediaPlayer.getPlaybackState()) {
			case Player.STATE_READY: {
				return mExoMediaPlayer.getPlayWhenReady() ? STATE_PLAYING : STATE_PAUSED;
			}
			case Player.STATE_BUFFERING: {
				return mExoMediaPlayer.getPlayWhenReady() ? STATE_BUFFERING_PLAYING : STATE_BUFFERING_PAUSED;
			}
			case Player.STATE_ENDED: {
				return STATE_COMPLETED;
			}
			case Player.STATE_IDLE: {
				return STATE_IDLE;
			}
			default: {
				return STATE_NONE;
			}
		}
	}

}
