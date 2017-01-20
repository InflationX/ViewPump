package io.github.inflationx.viewpump;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class InflateResult {
    private final View view;
    private final String name;
    private final Context context;
    private final AttributeSet attrs;

    private InflateResult(Builder builder) {
        view = builder.view;
        name = builder.name;
        context = builder.context;
        attrs = builder.attrs;
    }

    @Nullable
    public View view() {
        return view;
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
        return "InflateResult{" +
                "view=" + view +
                ", name=" + name +
                ", context=" + context +
                ", attrs=" + attrs +
                '}';
    }

    public static final class Builder {
        private View view;
        private String name;
        private Context context;
        private AttributeSet attrs;

        private Builder() { }

        private Builder(InflateResult result) {
            this.view = result.view;
            this.name = result.name;
            this.context = result.context;
            this.attrs = result.attrs;
        }

        public Builder view(@Nullable View view) {
            this.view = view;
            return this;
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

        public InflateResult build() {
            if (name == null) {
                throw new IllegalStateException("name == null");
            }
            if (context == null) {
                throw new IllegalStateException("context == null");
            }
            if (view != null && !name.equals(view.getClass().getName())) {
                throw new IllegalStateException("name (" + name + ") "
                        + "must be the view's fully qualified name (" + view.getClass().getName() + ")");
            }
            return new InflateResult(this);
        }
    }
}
