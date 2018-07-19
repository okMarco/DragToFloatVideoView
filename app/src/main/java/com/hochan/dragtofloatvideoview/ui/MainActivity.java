package com.hochan.dragtofloatvideoview.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.hochan.dragtofloatvideoview.R;
import com.hochan.dragtofloatvideoview.model.VideoData;
import com.hochan.dragtofloatvideoview.video.RoundCornerViewOutlineProvider;
import com.hochan.dragtofloatvideoview.video.VideoPlayService;
import com.hochan.dragtofloatvideoview.video.videolayout.VideoPlayLayout;

public class MainActivity extends AppCompatActivity implements VideoPlayLayout.OnSingleTapListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent serviceIntent = new Intent(this, VideoPlayService.class);
		startService(serviceIntent);

		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new RecyclerView.Adapter() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				VideoPlayLayout videoPlayLayout = new VideoPlayLayout(getApplicationContext());
				return new RecyclerView.ViewHolder(videoPlayLayout) {
					@Override
					public String toString() {
						return super.toString();
					}
				};
			}

			@Override
			public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
				if (holder.itemView instanceof VideoPlayLayout) {
					final VideoPlayLayout videoPlayLayout = (VideoPlayLayout) holder.itemView;
					videoPlayLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics())));
					videoPlayLayout.setData(VideoData.VIDEO_DATA_LIST.get(position).getVideoUrl(),
							VideoData.VIDEO_DATA_LIST.get(position).getVideoThumbnail());
					videoPlayLayout.setOnSingleTapListener(MainActivity.this);
					if (videoPlayLayout.getVideoControl() != null) {
						videoPlayLayout.getVideoControl().showFullScreenButton();
						videoPlayLayout.getVideoControl().setFullScreenClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								onSingleTap(videoPlayLayout);
							}
						});
					}
				}
			}

			@Override
			public int getItemCount() {
				return VideoData.VIDEO_DATA_LIST.size();
			}
		});
	}

	@Override
	public boolean onSingleTap(VideoPlayLayout videoPlayLayout) {
		VideoViewPagerActivity.playVideo(MainActivity.this,
				new VideoData(videoPlayLayout.getVideoUrl(), videoPlayLayout.getThumbnailUrl()));
		return true;
	}
}
