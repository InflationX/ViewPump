package io.github.inflationx.viewpump;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ViewPump {

    private static ViewPump INSTANCE;

    /** List of interceptors. */
    private final List<Interceptor> interceptors;

    /** List that gets cleared and reused as it holds interceptors with the fallback added. */
    private final List<Interceptor> mInterceptorsWithFallback;

    /** Use Reflection to inject the private factory. */
    private final boolean mReflection;

    /** Use Reflection to intercept CustomView inflation with the correct Context. */
    private final boolean mCustomViewCreation;

    /** Store the resourceId for the layout used to inflate the View in the View tag. */
    private final boolean mStoreLayoutResId;

    /** A FallbackViewCreator used to instantiate a view via reflection when using the create() API. */
    private static FallbackViewCreator mReflectiveFallbackViewCreator;

    private ViewPump(Builder builder) {
        interceptors = immutableList(builder.interceptors);
        List<Interceptor> interceptorsWithFallback = builder.interceptors;
        interceptorsWithFallback.add(new FallbackViewCreationInterceptor());
        mInterceptorsWithFallback = immutableList(interceptorsWithFallback);
        mReflection = builder.reflection;
        mCustomViewCreation = builder.customViewCreation;
        mStoreLayoutResId = builder.storeLayoutResId;
        mReflectiveFallbackViewCreator = builder.reflectiveFallbackViewCreator;
    }

    public static void init(ViewPump viewPump) {
        INSTANCE = viewPump;
    }

    @MainThread
    public static ViewPump get() {
        if (INSTANCE == null) {
            INSTANCE = builder().build();
        }
        return INSTANCE;
    }

    /**
     * Allows for programmatic creation of Views via reflection on class name that are still
     * pre/post-processed by the inflation interceptors.
     *
     * @param context The context.
     * @param clazz The class of View to be created.
     * @return The processed view, which might not necessarily be the same type as clazz.
     */
    @Nullable
    public static View create(Context context, Class<? extends View> clazz) {
        return get().inflate(InflateRequest.builder()
                .context(context)
                .name(clazz.getName())
                .fallbackViewCreator(getReflectiveFallbackViewCreator())
                .build())
                .view();
    }

    public InflateResult inflate(InflateRequest originalRequest) {
        Interceptor.Chain chain = new InterceptorChain(mInterceptorsWithFallback, 0, originalRequest);
        return chain.proceed(originalRequest);
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public boolean isReflection() {
        return mReflection;
    }

    public boolean isCustomViewCreation() {
        return mCustomViewCreation;
    }

    public boolean isStoreLayoutResId() {
        return mStoreLayoutResId;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    /** Returns an immutable copy of {@code list}. */
    private static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    private static FallbackViewCreator getReflectiveFallbackViewCreator() {
        if (mReflectiveFallbackViewCreator == null) {
            mReflectiveFallbackViewCreator = new ReflectiveFallbackViewCreator();
        }
        return mReflectiveFallbackViewCreator;
    }

    public static final class Builder {

        /** List of interceptors. */
        private final List<Interceptor> interceptors = new ArrayList<>();

        /** Use Reflection to inject the private factory. Defaults to true. */
        private boolean reflection = true;

        /** Use Reflection to intercept CustomView inflation with the correct Context. */
        private boolean customViewCreation = true;

        /** Store the resourceId for the layout used to inflate the View in the View tag. */
        private boolean storeLayoutResId = false;

        /** A FallbackViewCreator used to instantiate a view via reflection when using the create() API. */
        private FallbackViewCreator reflectiveFallbackViewCreator = null;

        private Builder() { }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        /**
         * <p>Turn of the use of Reflection to inject the private factory.
         * This has operational consequences! Please read and understand before disabling.</p>
         *
         * <p> If you disable this you will need to override your {@link android.app.Activity#onCreateView(View, String, android.content.Context, android.util.AttributeSet)}
         * as this is set as the {@link android.view.LayoutInflater} private factory.</p>
         * <br>
         * <b> Use the following code in the Activity if you disable FactoryInjection:</b>
         * <pre><code>
         * {@literal @}Override
         * public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
         *   return ViewPumpContextWrapper.onActivityCreateView(this, parent, super.onCreateView(parent, name, context, attrs), name, context, attrs);
         * }
         * </code></pre>
         *
         * @param enabled True if private factory inject is allowed; otherwise, false.
         */
        public Builder setPrivateFactoryInjectionEnabled(boolean enabled) {
            this.reflection = enabled;
            return this;
        }

        /**
         * Due to the poor inflation order where custom views are created and never returned inside an
         * {@code onCreateView(...)} method. We have to create CustomView's at the latest point in the
         * overrideable injection flow.
         *
         * On HoneyComb+ this is inside the {@link android.app.Activity#onCreateView(View, String, android.content.Context, android.util.AttributeSet)}
         *
         * We wrap base implementations, so if you LayoutInflater/Factory/Activity creates the
         * custom view before we get to this point, your view is used. (Such is the case with the
         * TintEditText etc)
         *
         * The problem is, the native methods pass there parents context to the constructor in a really
         * specific place. We have to mimic this in {@link ViewPumpLayoutInflater#createCustomViewInternal(View, View, String, android.content.Context, android.util.AttributeSet)}
         * To mimic this we have to use reflection as the Class constructor args are hidden to us.
         *
         * We have discussed other means of doing this but this is the only semi-clean way of doing it.
         * (Without having to do proxy classes etc).
         *
         * Calling this will of course speed up inflation by turning off reflection, but not by much,
         * But if you want ViewPump to inject the correct typeface then you will need to make sure your CustomView's
         * are created before reaching the LayoutInflater onViewCreated.
         *
         * @param enabled True if custom view inflated is allowed; otherwise, false.
         */
        public Builder setCustomViewInflationEnabled(boolean enabled) {
            this.customViewCreation = enabled;
            return this;
        }

        public Builder setReflectiveFallbackViewCreator(FallbackViewCreator reflectiveFallbackViewCreator) {
            this.reflectiveFallbackViewCreator = reflectiveFallbackViewCreator;
            return this;
        }

        /**
         * The LayoutInflater can store the layout resourceId used to inflate a view into the inflated view's tag
         * where it can be later retrieved by an interceptor.
         *
         * @param enabled True if the view should store the resId; otherwise, false.
         */
        public Builder setStoreLayoutResId(boolean enabled) {
            this.storeLayoutResId = enabled;
            return this;
        }

        public ViewPump build() {
            return new ViewPump(this);
        }
    }
}
