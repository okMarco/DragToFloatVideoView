package com.hochan.dragtofloatvideoview.video.videolayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SpringUtil;
import com.hochan.dragtofloatvideoview.VideoTransitionUtils;
import com.hochan.dragtofloatvideoview.ViewUtils;
import com.hochan.dragtofloatvideoview.video.MiniVideoWindowManager;

import static com.hochan.dragtofloatvideoview.VideoTransitionUtils.startVideoLayoutAnimation;
import static com.hochan.dragtofloatvideoview.VideoTransitionUtils.updateLayout;

/**
 * .
 * Created by hochan on 2018/1/25.
 */

public class FullVideoPlayLayout extends VideoPlayLayout {

	private boolean mHasOriginalVideoLayout;

	public int mMinVideoHeight;
	public int mMiniLayoutMargin;
	public final Rect mFullScreenLocation = new Rect();
	private final Rect mMiniWindowLocation = new Rect();

	private float mLastTouchRawY;
	private boolean mChangingLayout;

	public OnFullScreenListener mOnFullScreenListener;

	public FullVideoPlayLayout(@NonNull Context context) {
		this(context, null);
	}

	public FullVideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FullVideoPlayLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mMinVideoHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_VIDEO_HEIGHT_IN_DIP, getResources().getDisplayMetrics());
		mMiniLayoutMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MINI_LAYOUT_MARGIN_IN_DIP, getResources().getDisplayMetrics());
		mFullScreenLocation.set(0, 0, getResources().getDisplayMetrics().widthPixels,
				getResources().getDisplayMetrics().heightPixels);
//		mVideoPlayBinding.btnRotate.setVisibility(VISIBLE);
//		mVideoPlayBinding.btnRotate.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startRotationAnimation();
//			}
//		});
	}

	private void startRotationAnimation() {
		ValueAnimator valueAnimator;
		if (getRotation() == 0) {
			valueAnimator = ValueAnimator.ofInt(0, 90);
		} else {
			valueAnimator = ValueAnimator.ofInt(90, 0);
		}
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				ViewGroup.LayoutParams layoutParams = getLayoutParams();
				layoutParams.width = (int) SpringUtil.mapValueFromRangeToRange((int) animation.getAnimatedValue(), 0, 90,
						getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
				layoutParams.height = (int) SpringUtil.mapValueFromRangeToRange((int) animation.getAnimatedValue(), 0, 90,
						getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels);
				setLayoutParams(layoutParams);
				setPivotX(0);
				setPivotY(0);
				setRotation((int) animation.getAnimatedValue());
				int transitionX = (int) SpringUtil.mapValueFromRangeToRange((int) animation.getAnimatedValue(), 0, 90,
						0, getResources().getDisplayMetrics().widthPixels);
				setTranslationX(transitionX);
			}
		});
		valueAnimator.start();
	}

	public void setOnFullScreenListener(OnFullScreenListener listener) {
		mOnFullScreenListener = listener;
	}

	public void startEnterMiniWindowAnimation() {
		AnimatorListenerAdapter startEnterMiniWindowAnimation = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				MiniVideoWindowManager.getInstance().showMiniVideoLayout(FullVideoPlayLayout.this);
			}
		};
		startVideoLayoutAnimation(this, mMiniWindowLocation, startEnterMiniWindowAnimation);
	}

	public void enterFullScreen(VideoPlayLayout originalVideoLayout) {
		VideoTransitionUtils.prepareVideoLayoutForEnterAnimation(originalVideoLayout, this);
		AnimatorListenerAdapter startEnterFullScreenAnimation = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				Rect endLocation = new Rect();
				endLocation.left = 0;
				endLocation.right = getResources().getDisplayMetrics().widthPixels;
				endLocation.top = 0;
				endLocation.bottom = getResources().getDisplayMetrics().heightPixels;
				startVideoLayoutAnimation(FullVideoPlayLayout.this, endLocation, new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if (mOnFullScreenListener != null) {
							mOnFullScreenListener.onEnterFullScreen();
						}
					}
				});
			}
		};
		ViewUtils.doAfterFadeIn(this, startEnterFullScreenAnimation);
	}

	public boolean exitFullScreen() {
		if (getVisibility() == VISIBLE) {
			startExitFullViewAnimation();
			return true;
		}
		return false;
	}

	private void startExitFullViewAnimation() {
		if (mOnFullScreenListener != null) {
			mOnFullScreenListener.onStartExitFullScreen();
		}
		if (mHasOriginalVideoLayout) {
			Rect endLocation = new Rect();
			endLocation.left = 0;
			endLocation.right = getResources().getDisplayMetrics().widthPixels;
			endLocation.top = 0;
			endLocation.bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
			AnimatorListenerAdapter startExitFullScreenAnimation = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (mOnFullScreenListener != null) {
						mOnFullScreenListener.onExitFullScreen();
					}
					ViewUtils.doAfterFadeOut(FullVideoPlayLayout.this, new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							setVisibility(INVISIBLE);
						}
					});
				}
			};
			startVideoLayoutAnimation(this, endLocation, startExitFullScreenAnimation);
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (mVideoControl.getVideoControlVisibility() == View.VISIBLE && getRotation() == 0) {
			exitFullScreen();
		}
		return super.onSingleTapUp(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		mChangingLayout = false;
		return super.onDown(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (!mChangingLayout) {
			boolean changingPosition = super.onScroll(e1, e2, distanceX, distanceY);
			if (!changingPosition && Math.abs(distanceY) > Math.abs(distanceX) && getRotation() == 0) {
				mChangingLayout = true;
				mLastTouchRawY = e2.getRawY();
				initMiniWindowLocation();
				if (mVideoControl != null) {
					mVideoControl.hide();
				}
			}
		}

		if (mChangingLayout) {
			MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
			float dY = e2.getRawY() - mLastTouchRawY;
			updateLayout(this, layoutParams.height - (int) dY,
					getResources().getDisplayMetrics().heightPixels,
					mMinVideoHeight,
					mFullScreenLocation,
					mMiniWindowLocation);
			mLastTouchRawY = e2.getRawY();
		}
		return true;
	}

	public void initMiniWindowLocation() {
		int minVideoWidth = (int) (mMinVideoHeight * mVideoPlayBinding.videoTextureView.getW2HRatio());
		int endTransitionX = getResources().getDisplayMetrics().widthPixels - minVideoWidth - mMiniLayoutMargin;
		int endTransitionY = getResources().getDisplayMetrics().heightPixels - mMinVideoHeight - mMiniLayoutMargin;
		mMiniWindowLocation.set(endTransitionX, endTransitionY, endTransitionX + minVideoWidth, endTransitionY + mMinVideoHeight);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP && mChangingLayout) {
			if (getMeasuredHeight() > getResources().getDisplayMetrics().heightPixels / 2) {
				startVideoLayoutAnimation(this, mFullScreenLocation, null);
			} else {
				startEnterMiniWindowAnimation();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	public interface OnFullScreenListener {
		void onEnterFullScreen();

		void onEnterMiniScreen();

		void onStartExitFullScreen();

		void onExitFullScreen();

		void onMiniScreenDismiss();
	}
}
