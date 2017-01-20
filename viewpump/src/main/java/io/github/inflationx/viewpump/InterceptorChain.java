package io.github.inflationx.viewpump;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * A concrete interceptor chain that carries the entire interceptor chain.
 */
class InterceptorChain implements Interceptor.Chain {
    private final List<Interceptor> interceptors;
    private final int index;
    private final InflateRequest request;

    InterceptorChain(@NonNull List<Interceptor> interceptors, int index, @NonNull InflateRequest request) {
        this.interceptors = interceptors;
        this.index = index;
        this.request = request;
    }

    @NonNull
    @Override
    public InflateRequest request() {
        return request;
    }

    @NonNull
    @Override
    public InflateResult proceed(@NonNull InflateRequest request) {
        if (index >= interceptors.size()) {
            throw new AssertionError("no interceptors added to the chain");
        }

        // Call the next interceptor in the chain.
        InterceptorChain next = new InterceptorChain(interceptors, index + 1, request);
        Interceptor interceptor = interceptors.get(index);
        InflateResult result = interceptor.intercept(next);

        // Confirm that the intercepted response isn't null.
        if (result == null) {
            throw new NullPointerException("interceptor " + interceptor + " returned null");
        }

        return result;
    }
}
