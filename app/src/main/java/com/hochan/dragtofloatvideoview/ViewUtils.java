package com.hochan.dragtofloatvideoview;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;

import java.lang.ref.WeakReference;

/**
 * .
 * Created by hochan on 2018/1/31.
 */

public class ViewUtils {

	public static void doAfterFadeOut(final View view, AnimatorListenerAdapter animatorListenerAdapter) {
		final ViewPropertyAnimator viewPropertyAnimator = view.animate().alpha(0).setListener(animatorListenerAdapter);
		view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(View v) {
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				viewPropertyAnimator.cancel();
			}
		});
	}

	public static void doAfterFadeIn(final View view, AnimatorListenerAdapter animatorListenerAdapter) {
		final ViewPropertyAnimator viewPropertyAnimator = view.animate().alpha(1).setListener(animatorListenerAdapter);
		view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(View v) {
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				viewPropertyAnimator.cancel();
			}
		});
	}

	public static void setUiFlags(Window window, boolean fullscreen) {
		View decorView = window.getDecorView();
		if (decorView != null) {
			int flags = getLimitedUiFlags();
			decorView.setSystemUiVisibility(fullscreen ? getFullscreenUiFlags() : flags);
		}
	}

	private static int getLimitedUiFlags() {
		int flags = View.SYSTEM_UI_FLAG_VISIBLE;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE // 保持View Layout不变，隐藏状态栏或者导航栏后，View不会拉伸。
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 让View全屏显示，Layout会被拉伸到StatusBar下面，不包含NavigationBar。
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;// 让View全屏显示，Layout会被拉伸到StatusBar和NavigationBar下面
		}
		return flags;
	}

	private static int getFullscreenUiFlags() {
		int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE // 保持View Layout不变，隐藏状态栏或者导航栏后，View不会拉伸。
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 让View全屏显示，Layout会被拉伸到StatusBar下面，不包含NavigationBar。
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 让View全屏显示，Layout会被拉伸到StatusBar和NavigationBar下面。
					| View.SYSTEM_UI_FLAG_FULLSCREEN // Activity全屏显示，且状态栏被隐藏覆盖掉。等同于（WindowManager.LayoutParams.FLAG_FULLSCREEN）。
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // 隐藏虚拟按键(导航栏)。有些手机会用虚拟按键来代替物理按键。
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				// 这个flag只有当设置了SYSTEM_UI_FLAG_HIDE_NAVIGATION才起作用。如果没有设置这个flag，
				// 任意的View相互动作都退出SYSTEM_UI_FLAG_HIDE_NAVIGATION模式。如果设置就不会退出。
				flags |= View.SYSTEM_UI_FLAG_IMMERSIVE
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
		}
		return flags;
	}

	public static boolean canDrawOverlays(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.canDrawOverlays(context)) {
				return true;
			} else {
				//若没有权限，提示获取.
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				context.startActivity(intent);
				return false;
			}
		} else {
			return true;
		}
	}

	public static void setScreenOrientationPortrait(Activity activity) {
		setScreenOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public static void setScreenOrientationLandscape(Activity activity) {
		setScreenOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public static void setScreenOrientationUser(Activity activity) {
		setScreenOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_USER);
	}

	private static void setScreenOrientation(Activity activity, int orientation) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			activity.setRequestedOrientation(orientation);
		} else {
			int index = -1;
			for (int i = MainApp.ACTIVITY_WEAKREFERENCE_LIST.size() - 1; i >= 0; i--) {
				WeakReference<Activity> activityWeakReference = MainApp.ACTIVITY_WEAKREFERENCE_LIST.get(i);
				if (activityWeakReference != null && activityWeakReference.get() != null
						&& activityWeakReference.get() == activity) {
					index = i - 1;
				}
			}
			if (index >= 0 && index < MainApp.ACTIVITY_WEAKREFERENCE_LIST.size() - 1) {
				if (MainApp.ACTIVITY_WEAKREFERENCE_LIST.get(index).get() != null) {
					MainApp.ACTIVITY_WEAKREFERENCE_LIST.get(index).get().setRequestedOrientation(orientation);
				}
			}
		}
	}
}
