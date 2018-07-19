package com.hochan.dragtofloatvideoview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.squareup.leakcanary.LeakCanary;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * .
 * <p>
 * Created by hochan on 2018/5/30.
 */

public class MainApp extends Application {

	public static final List<Activity> ACTIVITY_LIST = new ArrayList<>();
	public static final List<WeakReference<Activity>> ACTIVITY_WEAKREFERENCE_LIST = new ArrayList<>();

	private ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {

		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			ACTIVITY_LIST.add(activity);
			ACTIVITY_WEAKREFERENCE_LIST.add(new WeakReference<>(activity));
		}

		@Override
		public void onActivityStarted(Activity activity) {

		}

		@Override
		public void onActivityResumed(Activity activity) {

		}

		@Override
		public void onActivityPaused(Activity activity) {

		}

		@Override
		public void onActivityStopped(Activity activity) {

		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			for (int i = ACTIVITY_WEAKREFERENCE_LIST.size() - 1; i >= 0; i--) {
				WeakReference<Activity> activityWeakReference = ACTIVITY_WEAKREFERENCE_LIST.get(i);
				if (activityWeakReference != null && activityWeakReference.get() != null && activityWeakReference.get() == activity) {
					ACTIVITY_WEAKREFERENCE_LIST.remove(activityWeakReference);
				}
			}
			ACTIVITY_LIST.remove(activity);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);
		registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
	}

	@Override
	public void onTerminate() {
		unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
		super.onTerminate();
	}
}
