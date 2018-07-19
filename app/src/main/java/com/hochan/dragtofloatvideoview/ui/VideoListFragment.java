package com.hochan.dragtofloatvideoview.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hochan.dragtofloatvideoview.GlideApp;
import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.databinding.FragmentVideoListBinding;
import com.hochan.dragtofloatvideoview.model.VideoData;
import com.hochan.dragtofloatvideoview.ui.VideoViewPagerActivity;

import static com.hochan.dragtofloatvideoview.ui.VideoViewPagerActivity.EXTRA_SCROLL_TO_INDEX;

/**
 * .
 * <p>
 * Created by hochan on 2018/5/22.
 */

public class VideoListFragment extends Fragment {

	private FragmentVideoListBinding mViewBinding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mViewBinding = FragmentVideoListBinding.inflate(inflater, container, false);
		return mViewBinding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mViewBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
		mViewBinding.recyclerView.setAdapter(new RecyclerView.Adapter() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new VideoThumbnailViewHolder(getLayoutInflater().inflate(R.layout.item_video_thumbnail, parent, false));
			}

			@Override
			public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
				ImageView ivThumbnail = holder.itemView.findViewById(R.id.iv_video_thumbnail);
				GlideApp.with(getContext())
						.load(VideoData.VIDEO_DATA_LIST.get(position).getVideoThumbnail())
						.into(ivThumbnail);
				ivThumbnail.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), VideoViewPagerActivity.class);
						intent.putExtra(EXTRA_SCROLL_TO_INDEX, position);
						startActivity(intent);
					}
				});
			}

			@Override
			public int getItemCount() {
				return VideoData.VIDEO_DATA_LIST.size();
			}
		});
	}

	class VideoThumbnailViewHolder extends RecyclerView.ViewHolder {

		public VideoThumbnailViewHolder(View itemView) {
			super(itemView);
		}
	}
}
