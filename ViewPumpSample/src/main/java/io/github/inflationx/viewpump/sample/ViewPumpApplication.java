package io.github.inflationx.viewpump.sample;

import android.app.Application;

import io.github.inflationx.viewpump.ViewPump;

/**
 * For ViewPump.
 */
public class ViewPumpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new TextUpdatingInterceptor())
                .addInterceptor(new MyViewInterceptor())
                .build());
    }
}
