package com.nikki.i.ren.ren_i_nikki;

import android.app.Application;

public class RenINikkiApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		OkaoScaner.getInstance(OkaoScaner.class).init(this);
		OkaoScaner.getInstance(OkaoScaner.class).scanStart();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
