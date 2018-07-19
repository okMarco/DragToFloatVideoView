package com.hochan.dragtofloatvideoview.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * .
 * <p>
 * Created by hochan on 2018/5/24.
 */

public class VideoData implements Serializable {

	public static final List<VideoData> VIDEO_DATA_LIST = new ArrayList<>();

	static {
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/d905e30f7077ef28d552.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/57643280261ed9b05248.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/71d901bfc1e2cf98c97b.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/88bfbebab2d893022c00.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/08dc0599e7c10d94e74d.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/16ca9242339eb6129345.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/addd7bc6d5646ff2f62e.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/7981e4c3dfbe0c9b773a.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/372cd9f414daef4838a9.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/8d724ba7853dc1c2f9de.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/6a1888e0905dd97cd300.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/bbb34f9655d349ebe418.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/d55247bcfd7ab903d2e0.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/419613e9d4d3100cb6fa.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/8d189ffd5e1dcb791dac.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/547e0ce887ffc1a4c7c7.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/fa702ac0e17edb27a590.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/04058b2aecf99d446aa9.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/f7f4852fc4fd42d8d3c0.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/bd39557595ee8ce70ad4.jpg"));
		VIDEO_DATA_LIST.add(new VideoData("http://lc-r4bstpmb.cn-n1.lcfile.com/d5fd72a290df01905a75.mp4",
				"http://lc-r4bstpmb.cn-n1.lcfile.com/c2c97707b0f7f74aeb6b.jpg"));
	}

	private String mVideoUrl;
	private String mVideoThumbnail;

	public VideoData(String videoUrl, String videoThumbnail) {
		mVideoUrl = videoUrl;
		mVideoThumbnail = videoThumbnail;
	}

	public String getVideoUrl() {
		return mVideoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		mVideoUrl = videoUrl;
	}

	public String getVideoThumbnail() {
		return mVideoThumbnail;
	}

	public void setVideoThumbnail(String videoThumbnail) {
		mVideoThumbnail = videoThumbnail;
	}
}
