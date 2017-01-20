package io.github.inflationx.viewpump.util;

import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class NameChangingPreInflationInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        return chain.proceed(
                chain.request()
                        .toBuilder()
                        .name(AnotherTestView.NAME)
                        .build());
    }
}
