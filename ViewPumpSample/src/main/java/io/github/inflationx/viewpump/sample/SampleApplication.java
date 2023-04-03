package io.github.inflationx.viewpump.sample;

import android.app.Application;

import io.github.inflationx.viewpump.ViewPump;

public class SampleApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        // For testing
        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(new TextUpdatingInterceptor())
                        .addInterceptor(new CustomTextViewInterceptor())
                        .build()
        );
    }
}
