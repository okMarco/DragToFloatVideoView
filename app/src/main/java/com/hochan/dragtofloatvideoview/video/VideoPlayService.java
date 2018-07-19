package com.hochan.dragtofloatvideoview.video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VideoPlayService extends Service {
	public VideoPlayService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MiniVideoWindowManager.getInstance().init(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
