package io.github.inflationx.viewpump.util;

import io.github.inflationx.viewpump.InflateRequest;
import io.github.inflationx.viewpump.InflateResult;
import io.github.inflationx.viewpump.Interceptor;

public class AnotherTestViewNewingPreInflationInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateRequest request = chain.request();
        if (AnotherTestView.NAME.equals(request.name())) {
            return InflateResult.builder()
                    .view(new AnotherTestView(request.context()))
                    .name(AnotherTestView.NAME)
                    .context(request.context())
                    .attrs(request.attrs())
                    .build();
        } else {
            return chain.proceed(request);
        }
    }
}
