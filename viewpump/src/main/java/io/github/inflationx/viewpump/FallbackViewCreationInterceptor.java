package io.github.inflationx.viewpump;

import android.view.View;

class FallbackViewCreationInterceptor implements Interceptor {

    @Override
    public InflateResult intercept(Chain chain) {
        InflateRequest request = chain.request();
        FallbackViewCreator viewCreator = request.fallbackViewCreator();
        View fallbackView = viewCreator.onCreateView(request.parent(), request.name(), request.context(), request.attrs());

        return InflateResult.builder()
                .view(fallbackView)
                .name(fallbackView != null ? fallbackView.getClass().getName() : request.name())
                .context(request.context())
                .attrs(request.attrs())
                .build();
    }
}
