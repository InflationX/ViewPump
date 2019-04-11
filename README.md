ViewPump
========

View inflation you can intercept.

ViewPump installs a custom LayoutInflater via a ContextThemeWrapper and provides an API of pre/post-inflation interceptors.

## Getting started

### Dependency

Include the dependency [Download (.aar)](http://search.maven.org/remotecontent?filepath=io/github/inflationx/viewpump/2.0.1/viewpump-2.0.1.aar) :

```groovy
dependencies {
    implementation 'io.github.inflationx:viewpump:2.0.1'
}
```

### Usage

Define your interceptor. Below is a somewhat arbitrary example of a post-inflation interceptor that prefixes the text in a TextView.

```java
public class TextUpdatingInterceptor implements Interceptor {
    @Override
    public InflateResult intercept(Chain chain) {
        InflateResult result = chain.proceed(chain.request());
        if (result.view() instanceof TextView) {
            // Do something to result.view()
            // You have access to result.context() and result.attrs()
            TextView textView = (TextView) result.view();
            textView.setText("[Prefix] " + textView.getText());
        }
        return result;
    }
}
```

Below is an example of a pre-inflation interceptor that returns a CustomTextView when a TextView was defined in the layout's XML.

```java
public class CustomTextViewInterceptor implements Interceptor {
    @Override
    public InflateResult intercept(Chain chain) {
        InflateRequest request = chain.request();
        if (request.name().endsWith("TextView")) {
            CustomTextView view = new CustomTextView(request.context(), request.attrs());
            return InflateResult.builder()
                    .view(view)
                    .name(view.getClass().getName())
                    .context(request.context())
                    .attrs(request.attrs())
                    .build();
        } else {
            return chain.proceed(request);
        }
    }
}
```

### Installation

Add your interceptors to the `ViewPump.builder()`, in your `Application` class in the `#onCreate()` method and `init` the `ViewPump`. The order of the interceptors is important since they form the interceptor chain of requests and results.

An interceptor may choose to return a programmatically instantiated view rather than letting the default inflation occur, in which case interceptors added after it will be skipped. For this reason, it is better to add your post-inflation interceptors before the pre-inflation interceptors

```java
@Override
public void onCreate() {
    super.onCreate();
    ViewPump.init(ViewPump.builder()
                .addInterceptor(new TextUpdatingInterceptor())
                .addInterceptor(new CustomTextViewInterceptor())
                .build());
    //....
}
```

### Inject into Context

Wrap the `Activity` Context:

```java
@Override
protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
}
```

_You're good to go!_

To see more ideas for potential use cases, check out the [Recipes](https://github.com/InflationX/ViewPump/wiki/Recipes) wiki page.

# Collaborators

- [@jbarr21](https://github.com/jbarr21)
- [@chrisjenx](https://github.com/chrisjenx)

# Licence

    Copyright 2017 InflationX

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
