package io.github.inflationx.viewpump.util;

import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class TestPostInflationInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateResult result = chain.proceed(chain.request());
        if (result.view() instanceof TestView) {
            ((TestView) result.view()).setPostProcessed(true);
        }
        return result;
    }
}
