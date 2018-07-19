package com.hochan.dragtofloatvideoview.video;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.hochan.dragtofloatvideoview.ViewUtils;
import com.hochan.dragtofloatvideoview.databinding.LayoutFloatVideoWindowBinding;
import com.hochan.dragtofloatvideoview.video.videolayout.MiniVideoPlayLayout;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

/**
 * .
 * Created by hochan on 2018/1/29.
 */

public class MiniVideoWindowManager {

	private WindowManager mWindowManager;
	private boolean isVideoLayoutAdded = false;
	private LayoutFloatVideoWindowBinding mMiniVideoWindowBinding;
	private boolean mIsReturningFullScreen;

	private OnMiniWindowListener mOnMiniWindowListener;

	public static MiniVideoWindowManager getInstance() {
		return Holder.INSTANCE;
	}

	private static final class Holder {
		private static final MiniVideoWindowManager INSTANCE = new MiniVideoWindowManager();
	}

	void init(final VideoPlayService context) {
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mMiniVideoWindowBinding = LayoutFloatVideoWindowBinding.inflate(LayoutInflater.from(context));
		mMiniVideoWindowBinding.miniVideoLayout.setOnMiniWindowListener(new MiniVideoPlayLayout.MiniVideoWindowListener() {
			@Override
			public void onDrag(int x, int y) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mMiniVideoWindowBinding.getRoot().getLayoutParams();
				layoutParams.x = x;
				layoutParams.y = y;
				mWindowManager.updateViewLayout(mMiniVideoWindowBinding.getRoot(), layoutParams);
			}

			@Override
			public void onScale(float factor) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mMiniVideoWindowBinding.getRoot().getLayoutParams();
				layoutParams.height = (int) (layoutParams.height * factor);
				layoutParams.width = (int) (layoutParams.height * mMiniVideoWindowBinding.miniVideoLayout.getVideoTextureW2HRatio());
				mWindowManager.updateViewLayout(mMiniVideoWindowBinding.getRoot(), layoutParams);
			}

			@Override
			public void returnFullScreen() {
				if (mOnMiniWindowListener != null) {
					mIsReturningFullScreen = true;
					mOnMiniWindowListener.onRenterFromMiniWindow();
				}
			}

			@Override
			public void onSurfaceReplace() {
				if (!mIsReturningFullScreen) {
					dismiss();
				}
			}

			@Override
			public void onPlayComplete() {
				dismiss();
			}
		});
	}

	private void dismiss() {
		ViewUtils.doAfterFadeOut(mMiniVideoWindowBinding.getRoot(), new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				removeMiniVideoLayout();
				if (mOnMiniWindowListener != null) {
					mOnMiniWindowListener.onMiniWindowDismiss();
				}
			}
		});
	}

	public View getRootView() {
		return mMiniVideoWindowBinding.getRoot();
	}

	public void removeMiniVideoLayout() {
		mWindowManager.removeViewImmediate(mMiniVideoWindowBinding.getRoot());
		isVideoLayoutAdded = false;
	}

	public void showMiniVideoLayout(final VideoPlayLayout fromVideoLayout) {
		if (!isVideoLayoutAdded) {
			mMiniVideoWindowBinding.miniVideoLayout.setVideoUrl(fromVideoLayout.mVideoUrl);
			mMiniVideoWindowBinding.getRoot().setAlpha(0);
			mWindowManager.addView(mMiniVideoWindowBinding.getRoot(), getLayoutParams(fromVideoLayout));
			mMiniVideoWindowBinding.miniVideoLayout.setVideoSize(fromVideoLayout.getVideoTextureWidth(),
					fromVideoLayout.getVideoTextureHeight());
			ViewUtils.doAfterFadeIn(mMiniVideoWindowBinding.getRoot(), new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					fromVideoLayout.setVisibility(View.INVISIBLE);
					mMiniVideoWindowBinding.getRoot().setVisibility(View.VISIBLE);
					mMiniVideoWindowBinding.getRoot().setAlpha(1);
					if (mOnMiniWindowListener != null) {
						mOnMiniWindowListener.onEnterMiniWindow();
					}
				}
			});
			mMiniVideoWindowBinding.miniVideoLayout.attachToVideoPlayer();
			isVideoLayoutAdded = true;
			mIsReturningFullScreen = false;
		}
	}

	@SuppressLint("RtlHardcoded")
	private WindowManager.LayoutParams getLayoutParams(VideoPlayLayout originalVideoLayout) {
		WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		} else {
			windowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		}
		windowLayoutParams.format = PixelFormat.TRANSLUCENT; // 设置图片格式，效果为背景透明(RGBA_8888)
		windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		windowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 设置悬浮窗的长宽
		windowLayoutParams.width = originalVideoLayout.getWidth();
		windowLayoutParams.height = originalVideoLayout.getHeight();

		int[] locationInScreen = new int[2];
		originalVideoLayout.getLocationOnScreen(locationInScreen);
		windowLayoutParams.x = locationInScreen[0];
		windowLayoutParams.y = locationInScreen[1];
		return windowLayoutParams;
	}

	public void registerOnEnterMiniWindowListener(OnMiniWindowListener onEnterMiniWindowListener) {
		mOnMiniWindowListener = onEnterMiniWindowListener;
	}

	public void unRegisterOnEnterMiniWindowListener(OnMiniWindowListener onEnterMiniWindowListener) {
		if (mOnMiniWindowListener == onEnterMiniWindowListener) {
			mOnMiniWindowListener = null;
		}
	}

	public interface OnMiniWindowListener {
		void onEnterMiniWindow();

		void onMiniWindowDismiss();

		void onRenterFromMiniWindow();
	}
}
