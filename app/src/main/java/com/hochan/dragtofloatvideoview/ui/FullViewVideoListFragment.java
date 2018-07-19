package com.hochan.dragtofloatvideoview.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.hochan.dragtofloatvideoview.GlideApp;
import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.ViewUtils;
import com.hochan.dragtofloatvideoview.databinding.FragmentFullViewVideoListBinding;
import com.hochan.dragtofloatvideoview.model.VideoData;
import com.hochan.dragtofloatvideoview.video.MiniVideoWindowManager;
import com.hochan.dragtofloatvideoview.video.RoundCornerViewOutlineProvider;
import com.hochan.dragtofloatvideoview.video.player.NativeVideoPlayer;
import com.hochan.dragtofloatvideoview.video.player.VideoPlayer;
import com.hochan.dragtofloatvideoview.video.videolayout.MiniVideoPlayLayout;
import com.hochan.dragtofloatvideoview.video.videolayout.TikTokVideoLayout;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

import java.util.ArrayList;
import java.util.List;

import static com.hochan.dragtofloatvideoview.VideoTransitionUtils.prepareVideoLayoutForEnterAnimation;
import static com.hochan.dragtofloatvideoview.ui.VideoViewPagerActivity.EXTRA_VIDEO_DATA;

/**
 * .
 * <p>
 * Created by hochan on 2018/5/22.
 */

public class FullViewVideoListFragment extends Fragment implements TikTokVideoLayout.OnFullScreenListener,
		TikTokVideoLayout.OnMiniLayoutTransitionListener {

	public static FullViewVideoListFragment newInstance(VideoData videoData) {
		FullViewVideoListFragment fragment = new FullViewVideoListFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(EXTRA_VIDEO_DATA, videoData);
		fragment.setArguments(bundle);
		return fragment;
	}

	private boolean mIsFirstEnter = true;
	private FragmentFullViewVideoListBinding mViewBinding;

	private List<VideoData> mVideoDataList = new ArrayList<>();

	// 屏幕旋转相关
	private boolean mForceToPortrait = false;
	private boolean mForceToLandscape = false;
	private OrientationEventListener mOrientationEventListener;

	private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ItemVideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_titok_video, parent, false));
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			final TikTokVideoLayout tikTokVideoLayout = holder.itemView.findViewById(R.id.video_layout);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				tikTokVideoLayout.getVideoTextureView().setOutlineProvider(new RoundCornerViewOutlineProvider(10));
				tikTokVideoLayout.getVideoTextureView().setClipToOutline(true);
			}
			if (position == 0 && mIsFirstEnter) {
				startFirstEnterAnimation(tikTokVideoLayout);
			}
			tikTokVideoLayout.getThumbnailImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
			GlideApp.with(getActivity())
					.load(mVideoDataList.get(position).getVideoThumbnail())
					.into(tikTokVideoLayout.getThumbnailImageView());
			tikTokVideoLayout.setOnMiniLayoutTransitionListener(FullViewVideoListFragment.this);
			tikTokVideoLayout.setOnFullScreenListener(FullViewVideoListFragment.this);
			tikTokVideoLayout.setVideoUrl(mVideoDataList.get(position).getVideoUrl());
			if (tikTokVideoLayout.getVideoControl() != null) {
				tikTokVideoLayout.getVideoControl().showRotateButton();
				tikTokVideoLayout.getVideoControl().setRotateButtonClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						clickToChangeOrientation();
					}
				});
			}
		}

		@Override
		public int getItemCount() {
			return mVideoDataList.size();
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();

		initOrientationEventListener();
	}

	/**
	 * 初始化数据，获取从上个页面传来的视频数据
	 */
	private void initData() {
		if (getArguments() != null) {
			VideoData videoData = (VideoData) getArguments().getSerializable(EXTRA_VIDEO_DATA);
			if (videoData != null) {
				mVideoDataList.add(videoData);
			}
		}
	}

	/**
	 * 初始化屏幕旋转角度监听
	 */
	private void initOrientationEventListener() {
		mOrientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
					return;
				}
				senorChangeOrientation(orientation);
			}
		};
	}

	/**
	 * 根据屏幕旋转角度来旋转屏幕
	 *
	 * @param orientation 屏幕旋转角度
	 */
	private void senorChangeOrientation(int orientation) {
		if (getActivity() != null) {
			if (orientation > 45 && orientation < 315) {
				mForceToLandscape = false;
				if (!mForceToPortrait) {
					ViewUtils.setScreenOrientationUser(getActivity());
				}
			} else {
				mForceToPortrait = false;
				if (!mForceToLandscape) {
					ViewUtils.setScreenOrientationPortrait(getActivity());
				}
			}
		}
	}

	/**
	 * 点击按钮旋转屏幕
	 */
	private void clickToChangeOrientation() {
		if (getActivity() != null) {
			if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				ViewUtils.setScreenOrientationPortrait(getActivity());
				mForceToPortrait = true;
			} else {
				mForceToLandscape = true;
				ViewUtils.setScreenOrientationLandscape(getActivity());
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		enableOrientationEventListener();

		returnFromMiniWindow();
	}

	/**
	 * 从小窗口返回
	 */
	private void returnFromMiniWindow() {
		VideoPlayLayout playingLayout = VideoPlayer.getInstance(getContext()).getVideoPlayLayout();
		if (playingLayout instanceof MiniVideoPlayLayout) {
			final TikTokVideoLayout tikTokVideoLayout = findFirstVisibleVideoLayout();
			if (tikTokVideoLayout != null) {
				prepareVideoLayoutForEnterAnimation(playingLayout, tikTokVideoLayout);
				tikTokVideoLayout.setAlpha(1);
				tikTokVideoLayout.setVisibility(View.VISIBLE);
				ViewUtils.doAfterFadeOut(MiniVideoWindowManager.getInstance().getRootView(), new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						tikTokVideoLayout.startFullViewAnimation();
						MiniVideoWindowManager.getInstance().removeMiniVideoLayout();
					}
				});
			}
		}
	}

	/**
	 * 启动屏幕旋转监听
	 */
	private void enableOrientationEventListener() {
		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		} else {
			mOrientationEventListener.disable();
		}
	}

	/**
	 * 但此页面不可见时，停止视频播放；当此页面恢复可见时，继续视频播放
	 *
	 * @param isVisibleToUser 当前页面是否可见
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isResumed()) {
			TikTokVideoLayout tikTokVideoLayout = findFirstVisibleVideoLayout();
			if (tikTokVideoLayout != null) {
				if (isVisibleToUser) {
					enableOrientationEventListener();
					tikTokVideoLayout.play();
				} else {
					tikTokVideoLayout.pause();
					mOrientationEventListener.disable();
				}
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mViewBinding = FragmentFullViewVideoListBinding.inflate(inflater);
		return mViewBinding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		mViewBinding.rcvVideoList.setLayoutManager(new LinearLayoutManager(getContext()));
		PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
		pagerSnapHelper.attachToRecyclerView(mViewBinding.rcvVideoList);
		mViewBinding.rcvVideoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					TikTokVideoLayout tikTokVideoLayout = findFirstVisibleVideoLayout();
					tikTokVideoLayout.setOnFullScreenListener(FullViewVideoListFragment.this);
					tikTokVideoLayout.play();
				}
			}
		});
		mViewBinding.rcvVideoList.setAdapter(mAdapter);

		initButtonListener();
	}

	private void initButtonListener() {
		mViewBinding.btnArrowBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() != null) {
					if (getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						getActivity().onBackPressed();
					} else {
						mForceToPortrait = true;
						ViewUtils.setScreenOrientationPortrait(getActivity());
					}
				}
			}
		});

		mViewBinding.btnPip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ViewUtils.canDrawOverlays(getContext())) {
					if (getActivity() != null) {
						mForceToPortrait = true;
						ViewUtils.setScreenOrientationPortrait(getActivity());
					}
					TikTokVideoLayout tikTokVideoLayout = findFirstVisibleVideoLayout();
					if (tikTokVideoLayout != null) {
						onStartEnterMiniLayout();
						tikTokVideoLayout.startMiniWindowAnimation();
					}
				}
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			mViewBinding.llTitleContainer.setVisibility(View.VISIBLE);
		}
	}

	public void scrollToVideo(int position) {
		mViewBinding.rcvVideoList.smoothScrollToPosition(position + 1);
	}

	private void startFirstEnterAnimation(final TikTokVideoLayout tikTokVideoLayout) {
		VideoPlayLayout playingVideoLayout = VideoPlayer.getInstance(getContext()).getVideoPlayLayout();
		prepareVideoLayoutForEnterAnimation(playingVideoLayout, tikTokVideoLayout);
		tikTokVideoLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				tikTokVideoLayout.getViewTreeObserver().removeOnPreDrawListener(this);
				tikTokVideoLayout.startFullViewAnimation();
				return false;
			}
		});
	}

	public TikTokVideoLayout findFirstVisibleVideoLayout() {
		LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mViewBinding.rcvVideoList.getLayoutManager();
		int position = linearLayoutManager.findFirstVisibleItemPosition();
		View firstVisibleView = linearLayoutManager.findViewByPosition(position);
		if (firstVisibleView != null && firstVisibleView.findViewById(R.id.video_layout) instanceof TikTokVideoLayout) {
			return firstVisibleView.findViewById(R.id.video_layout);
		}
		return null;
	}

	@Override
	public void onStartEnterMiniLayout() {
		mViewBinding.flTitleLayout.animate().alpha(0);
	}

	@Override
	public void onReturnFullViewLayout() {
		mViewBinding.flTitleLayout.animate().alpha(1);
		if (mIsFirstEnter) {
			mIsFirstEnter = false;
			mVideoDataList.addAll(VideoData.VIDEO_DATA_LIST);
			mAdapter.notifyItemRangeInserted(1, VideoData.VIDEO_DATA_LIST.size());
		}
	}

	@Override
	public void onFullScreenChange(boolean fullScreen) {
		if (getContext() != null && getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (getContext() instanceof Activity) {
				ViewUtils.setUiFlags(((Activity) getContext()).getWindow(), fullScreen);
			}
			mViewBinding.llTitleContainer.setVisibility(fullScreen ? View.INVISIBLE : View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mOrientationEventListener.disable();
	}

	public class ItemVideoHolder extends RecyclerView.ViewHolder {

		ItemVideoHolder(View itemView) {
			super(itemView);
		}
	}
}
