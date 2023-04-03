package io.github.inflationx.viewpump.sample;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class MainActivity extends AppCompatActivity {

//    Init in Application.
//    private final ViewPump pump = ViewPump.builder()
//            .addInterceptor(new TextUpdatingInterceptor())
//            .addInterceptor(new CustomTextViewInterceptor())
//            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
    }

    /*
        Uncomment if you disable PrivateFactory injection. See ViewPumpConfig#setPrivateFactoryInjectionEnabled(boolean)
     */
//    @Override
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public View onCreateView(View parent, String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//        return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs);
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // This is the new way
        // super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase, pump));
        // This is depreciate, create your own instance of ViewPump.
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
