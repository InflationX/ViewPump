package io.github.inflationx.viewpump;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class InflateRequest {
    private final String name;
    private final Context context;
    private final AttributeSet attrs;
    private final View parent;
    private final FallbackViewCreator fallbackViewCreator;

    private InflateRequest(Builder builder) {
        name = builder.name;
        context = builder.context;
        attrs = builder.attrs;
        parent = builder.parent;
        fallbackViewCreator = builder.fallbackViewCreator;
    }

    @NonNull
    public String name() {
        return name;
    }

    @NonNull
    public Context context() {
        return context;
    }

    @Nullable
    public AttributeSet attrs() {
        return attrs;
    }

    @Nullable
    public View parent() {
        return parent;
    }

    @NonNull
    public FallbackViewCreator fallbackViewCreator() {
        return fallbackViewCreator;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    public Builder toBuilder() {
        return new Builder(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "InflateRequest{" +
                "name='" + name + '\'' +
                ", context=" + context +
                ", attrs=" + attrs +
                ", parent=" + parent +
                ", fallbackViewCreator=" + fallbackViewCreator +
                '}';
    }

    public static final class Builder {
        private String name;
        private Context context;
        private AttributeSet attrs;
        private View parent;
        private FallbackViewCreator fallbackViewCreator;

        private Builder() { }

        private Builder(InflateRequest request) {
            this.name = request.name;
            this.context = request.context;
            this.attrs = request.attrs;
            this.parent = request.parent;
            this.fallbackViewCreator = request.fallbackViewCreator;
        }

        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        public Builder context(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public Builder attrs(@Nullable AttributeSet attrs) {
            this.attrs = attrs;
            return this;
        }

        public Builder parent(@Nullable View parent) {
            this.parent = parent;
            return this;
        }

        public Builder fallbackViewCreator(@NonNull FallbackViewCreator fallbackViewCreator) {
            this.fallbackViewCreator = fallbackViewCreator;
            return this;
        }

        public InflateRequest build() {
            if (name == null) {
                throw new IllegalStateException("name == null");
            }
            if (context == null) {
                throw new IllegalStateException("context == null");
            }
            if (fallbackViewCreator == null) {
                throw new IllegalStateException("fallbackViewCreator == null");
            }
            return new InflateRequest(this);
        }
    }
}
