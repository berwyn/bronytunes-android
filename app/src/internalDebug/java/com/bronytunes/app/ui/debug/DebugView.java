package com.bronytunes.app.ui.debug;

import android.animation.ValueAnimator;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.bronytunes.app.BuildConfig;
import com.bronytunes.app.R;
import com.bronytunes.app.data.AnimationSpeed;
import com.bronytunes.app.data.ApiEndpoint;
import com.bronytunes.app.data.ApiEndpoints;
import com.bronytunes.app.data.CaptureIntents;
import com.bronytunes.app.data.Injector;
import com.bronytunes.app.data.IsMockMode;
import com.bronytunes.app.data.LumberYard;
import com.bronytunes.app.data.PicassoDebugging;
import com.bronytunes.app.data.PixelGridEnabled;
import com.bronytunes.app.data.PixelRatioEnabled;
import com.bronytunes.app.data.ScalpelEnabled;
import com.bronytunes.app.data.ScalpelWireframeEnabled;
import com.bronytunes.app.data.api.MockBronyTunesService;
import com.bronytunes.app.data.prefs.BooleanPreference;
import com.bronytunes.app.data.prefs.IntPreference;
import com.bronytunes.app.data.prefs.NetworkProxyPreference;
import com.bronytunes.app.data.prefs.StringPreference;
import com.bronytunes.app.ui.debug.ContextualDebugActions.DebugAction;
import com.bronytunes.app.ui.logs.LogsDialog;
import com.bronytunes.app.ui.misc.EnumAdapter;
import com.bronytunes.app.util.Keyboards;
import com.bronytunes.app.util.Strings;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.StatsSnapshot;

import java.lang.reflect.Method;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.MockRestAdapter;
import retrofit.RestAdapter;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

public final class DebugView extends FrameLayout {
    private static final DateFormat DATE_DISPLAY_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
    private final ContextualDebugActions contextualDebugActions;
    @Bind(R.id.debug_contextual_title)
    View                   contextualTitleView;
    @Bind(R.id.debug_contextual_list)
    LinearLayout           contextualListView;
    @Bind(R.id.debug_network_endpoint)
    Spinner                endpointView;
    @Bind(R.id.debug_network_endpoint_edit)
    View                   endpointEditView;
    @Bind(R.id.debug_network_delay)
    Spinner                networkDelayView;
    @Bind(R.id.debug_network_variance)
    Spinner                networkVarianceView;
    @Bind(R.id.debug_network_error)
    Spinner                networkErrorView;
    @Bind(R.id.debug_network_proxy)
    Spinner                networkProxyView;
    @Bind(R.id.debug_network_logging)
    Spinner                networkLoggingView;
    @Bind(R.id.debug_capture_intents)
    Switch                 captureIntentsView;
    @Bind(R.id.debug_repositories_response)
    Spinner                repositoriesResponseView;
    @Bind(R.id.debug_ui_animation_speed)
    Spinner                uiAnimationSpeedView;
    @Bind(R.id.debug_ui_pixel_grid)
    Switch                 uiPixelGridView;
    @Bind(R.id.debug_ui_pixel_ratio)
    Switch                 uiPixelRatioView;
    @Bind(R.id.debug_ui_scalpel)
    Switch                 uiScalpelView;
    @Bind(R.id.debug_ui_scalpel_wireframe)
    Switch                 uiScalpelWireframeView;
    @Bind(R.id.debug_build_name)
    TextView               buildNameView;
    @Bind(R.id.debug_build_code)
    TextView               buildCodeView;
    @Bind(R.id.debug_build_sha)
    TextView               buildShaView;
    @Bind(R.id.debug_build_date)
    TextView               buildDateView;
    @Bind(R.id.debug_device_make)
    TextView               deviceMakeView;
    @Bind(R.id.debug_device_model)
    TextView               deviceModelView;
    @Bind(R.id.debug_device_resolution)
    TextView               deviceResolutionView;
    @Bind(R.id.debug_device_density)
    TextView               deviceDensityView;
    @Bind(R.id.debug_device_release)
    TextView               deviceReleaseView;
    @Bind(R.id.debug_device_api)
    TextView               deviceApiView;
    @Bind(R.id.debug_picasso_indicators)
    Switch                 picassoIndicatorView;
    @Bind(R.id.debug_picasso_cache_size)
    TextView               picassoCacheSizeView;
    @Bind(R.id.debug_picasso_cache_hit)
    TextView               picassoCacheHitView;
    @Bind(R.id.debug_picasso_cache_miss)
    TextView               picassoCacheMissView;
    @Bind(R.id.debug_picasso_decoded)
    TextView               picassoDecodedView;
    @Bind(R.id.debug_picasso_decoded_total)
    TextView               picassoDecodedTotalView;
    @Bind(R.id.debug_picasso_decoded_avg)
    TextView               picassoDecodedAvgView;
    @Bind(R.id.debug_picasso_transformed)
    TextView               picassoTransformedView;
    @Bind(R.id.debug_picasso_transformed_total)
    TextView               picassoTransformedTotalView;
    @Bind(R.id.debug_picasso_transformed_avg)
    TextView               picassoTransformedAvgView;
    @Bind(R.id.debug_okhttp_cache_max_size)
    TextView               okHttpCacheMaxSizeView;
    @Bind(R.id.debug_okhttp_cache_write_error)
    TextView               okHttpCacheWriteErrorView;
    @Bind(R.id.debug_okhttp_cache_request_count)
    TextView               okHttpCacheRequestCountView;
    @Bind(R.id.debug_okhttp_cache_network_count)
    TextView               okHttpCacheNetworkCountView;
    @Bind(R.id.debug_okhttp_cache_hit_count)
    TextView               okHttpCacheHitCountView;
    @Inject
    OkHttpClient           client;
    @Inject
    @Named("Api")
    OkHttpClient           apiClient;
    @Inject
    Picasso                picasso;
    @Inject
    LumberYard             lumberYard;
    @Inject
    @IsMockMode
    boolean                isMockMode;
    @Inject
    @ApiEndpoint
    StringPreference       networkEndpoint;
    @Inject
    NetworkProxyPreference networkProxy;
    @Inject
    @CaptureIntents
    BooleanPreference      captureIntents;
    @Inject
    @AnimationSpeed
    IntPreference          animationSpeed;
    @Inject
    @PicassoDebugging
    BooleanPreference      picassoDebugging;
    @Inject
    @PixelGridEnabled
    BooleanPreference      pixelGridEnabled;
    @Inject
    @PixelRatioEnabled
    BooleanPreference      pixelRatioEnabled;
    @Inject
    @ScalpelEnabled
    BooleanPreference      scalpelEnabled;
    @Inject
    @ScalpelWireframeEnabled
    BooleanPreference      scalpelWireframeEnabled;
    @Inject
    RestAdapter            restAdapter;
    @Inject
    MockRestAdapter        mockRestAdapter;
    @Inject
    MockBronyTunesService  mockGithubService;
    @Inject
    Application            app;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Injector.obtain(context).inject(this);

        // Inflate all of the controls and inject them.
        LayoutInflater.from(context).inflate(R.layout.debug_view_content, this);
        ButterKnife.bind(this);

        Set<DebugAction<?>> debugActions = Collections.emptySet();
        contextualDebugActions = new ContextualDebugActions(this, debugActions);

        setupNetworkSection();
        setupMockBehaviorSection();
        setupUserInterfaceSection();
        setupBuildSection();
        setupDeviceSection();
        setupPicassoSection();
        setupOkHttpCacheSection();
    }

    private void setupNetworkSection() {
        final ApiEndpoints currentEndpoint = ApiEndpoints.from(networkEndpoint.get());
        final EnumAdapter<ApiEndpoints> endpointAdapter =
                new EnumAdapter<>(getContext(), ApiEndpoints.class);
        endpointView.setAdapter(endpointAdapter);
        endpointView.setSelection(currentEndpoint.ordinal());
        endpointView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ApiEndpoints selected = endpointAdapter.getItem(position);
                if (selected != currentEndpoint) {
                    if (selected == ApiEndpoints.CUSTOM) {
                        Timber.d("Custom network endpoint selected. Prompting for URL.");
                        showCustomEndpointDialog(currentEndpoint.ordinal(), "http://");
                    } else {
                        setEndpointAndRelaunch(selected.url);
                    }
                } else {
                    Timber.d("Ignoring re-selection of network endpoint %s", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkDelayAdapter delayAdapter = new NetworkDelayAdapter(getContext());
        networkDelayView.setAdapter(delayAdapter);
        networkDelayView.setSelection(
                NetworkDelayAdapter.getPositionForValue(mockRestAdapter.getDelay()));
        networkDelayView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                long selected = delayAdapter.getItem(position);
                if (selected != mockRestAdapter.getDelay()) {
                    Timber.d("Setting network delay to %sms", selected);
                    mockRestAdapter.setDelay(selected);
                } else {
                    Timber.d("Ignoring re-selection of network delay %sms", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkVarianceAdapter varianceAdapter = new NetworkVarianceAdapter(getContext());
        networkVarianceView.setAdapter(varianceAdapter);
        networkVarianceView.setSelection(
                NetworkVarianceAdapter.getPositionForValue(mockRestAdapter.getVariancePercentage()));
        networkVarianceView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = varianceAdapter.getItem(position);
                if (selected != mockRestAdapter.getVariancePercentage()) {
                    Timber.d("Setting network variance to %s%%", selected);
                    mockRestAdapter.setVariancePercentage(selected);
                } else {
                    Timber.d("Ignoring re-selection of network variance %s%%", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkErrorAdapter errorAdapter = new NetworkErrorAdapter(getContext());
        networkErrorView.setAdapter(errorAdapter);
        networkErrorView.setSelection(
                NetworkErrorAdapter.getPositionForValue(mockRestAdapter.getErrorPercentage()));
        networkErrorView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = errorAdapter.getItem(position);
                if (selected != mockRestAdapter.getErrorPercentage()) {
                    Timber.d("Setting network error to %s%%", selected);
                    mockRestAdapter.setErrorPercentage(selected);
                } else {
                    Timber.d("Ignoring re-selection of network error %s%%", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        int currentProxyPosition = networkProxy.isSet() ? ProxyAdapter.PROXY : ProxyAdapter.NONE;
        final ProxyAdapter proxyAdapter = new ProxyAdapter(getContext(), networkProxy);
        networkProxyView.setAdapter(proxyAdapter);
        networkProxyView.setSelection(currentProxyPosition);
        networkProxyView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == ProxyAdapter.NONE) {
                    Timber.d("Clearing network proxy");
                    // TODO: Keep the custom proxy around so you can easily switch back and forth.
                    networkProxy.delete();
                    client.setProxy(null);
                    apiClient.setProxy(null);
                } else if (networkProxy.isSet() && position == ProxyAdapter.PROXY) {
                    Timber.d("Ignoring re-selection of network proxy %s", networkProxy.get());
                } else {
                    Timber.d("New network proxy selected. Prompting for host.");
                    showNewNetworkProxyDialog(proxyAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Only show the endpoint editor when a custom endpoint is in use.
        endpointEditView.setVisibility(currentEndpoint == ApiEndpoints.CUSTOM ? VISIBLE : GONE);

        if (currentEndpoint == ApiEndpoints.MOCK_MODE) {
            // Disable network proxy if we are in mock mode.
            networkProxyView.setEnabled(false);
            networkLoggingView.setEnabled(false);
        } else {
            // Disable network controls if we are not in mock mode.
            networkDelayView.setEnabled(false);
            networkVarianceView.setEnabled(false);
            networkErrorView.setEnabled(false);
        }

        // We use the JSON rest adapter as the source of truth for the log level.
        final EnumAdapter<RestAdapter.LogLevel> loggingAdapter =
                new EnumAdapter<>(getContext(), RestAdapter.LogLevel.class);
        networkLoggingView.setAdapter(loggingAdapter);
        networkLoggingView.setSelection(restAdapter.getLogLevel().ordinal());
        networkLoggingView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                RestAdapter.LogLevel selected = loggingAdapter.getItem(position);
                if (selected != restAdapter.getLogLevel()) {
                    Timber.d("Setting logging level to %s", selected);
                    restAdapter.setLogLevel(selected);
                } else {
                    Timber.d("Ignoring re-selection of logging level " + selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupMockBehaviorSection() {
        captureIntentsView.setEnabled(isMockMode);
        captureIntentsView.setChecked(captureIntents.get());
        captureIntentsView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Timber.d("Capture intents set to %s", b);
                captureIntents.set(b);
            }
        });

//        configureResponseSpinner(repositoriesResponseView, MockRepositoriesResponse.class);
    }

    private void setupUserInterfaceSection() {
        final AnimationSpeedAdapter speedAdapter = new AnimationSpeedAdapter(getContext());
        uiAnimationSpeedView.setAdapter(speedAdapter);
        final int animationSpeedValue = animationSpeed.get();
        uiAnimationSpeedView.setSelection(
                AnimationSpeedAdapter.getPositionForValue(animationSpeedValue));
        uiAnimationSpeedView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = speedAdapter.getItem(position);
                if (selected != animationSpeed.get()) {
                    Timber.d("Setting animation speed to %sx", selected);
                    animationSpeed.set(selected);
                    applyAnimationSpeed(selected);
                } else {
                    Timber.d("Ignoring re-selection of animation speed %sx", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // Ensure the animation speed value is always applied across app restarts.
        post(new Runnable() {
            @Override
            public void run() {
                applyAnimationSpeed(animationSpeedValue);
            }
        });

        boolean gridEnabled = pixelGridEnabled.get();
        uiPixelGridView.setChecked(gridEnabled);
        uiPixelRatioView.setEnabled(gridEnabled);
        uiPixelGridView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Timber.d("Setting pixel grid overlay enabled to " + isChecked);
                pixelGridEnabled.set(isChecked);
                uiPixelRatioView.setEnabled(isChecked);
            }
        });

        uiPixelRatioView.setChecked(pixelRatioEnabled.get());
        uiPixelRatioView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Timber.d("Setting pixel scale overlay enabled to " + isChecked);
                pixelRatioEnabled.set(isChecked);
            }
        });

        uiScalpelView.setChecked(scalpelEnabled.get());
        uiScalpelWireframeView.setEnabled(scalpelEnabled.get());
        uiScalpelView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Timber.d("Setting scalpel interaction enabled to " + isChecked);
                scalpelEnabled.set(isChecked);
                uiScalpelWireframeView.setEnabled(isChecked);
            }
        });

        uiScalpelWireframeView.setChecked(scalpelWireframeEnabled.get());
        uiScalpelWireframeView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Timber.d("Setting scalpel wireframe enabled to " + isChecked);
                scalpelWireframeEnabled.set(isChecked);
            }
        });
    }

    private void setupBuildSection() {
        buildNameView.setText(BuildConfig.VERSION_NAME);
        buildCodeView.setText(String.valueOf(BuildConfig.VERSION_CODE));
        buildShaView.setText(BuildConfig.GIT_SHA);

        try {
            // Parse ISO8601-format time into local time.
            DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
            inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date buildTime = inFormat.parse(BuildConfig.BUILD_TIME);
            buildDateView.setText(DATE_DISPLAY_FORMAT.format(buildTime));
        } catch (ParseException e) {
            throw new RuntimeException("Unable to decode build time: " + BuildConfig.BUILD_TIME, e);
        }
    }

    private void setupDeviceSection() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        String densityBucket = getDensityString(displayMetrics);
        deviceMakeView.setText(Strings.truncateAt(Build.MANUFACTURER, 20));
        deviceModelView.setText(Strings.truncateAt(Build.MODEL, 20));
        deviceResolutionView.setText(displayMetrics.heightPixels + "x" + displayMetrics.widthPixels);
        deviceDensityView.setText(displayMetrics.densityDpi + "dpi (" + densityBucket + ")");
        deviceReleaseView.setText(Build.VERSION.RELEASE);
        deviceApiView.setText(String.valueOf(Build.VERSION.SDK_INT));
    }

    private void setupPicassoSection() {
        boolean picassoDebuggingValue = picassoDebugging.get();
        picasso.setIndicatorsEnabled(picassoDebuggingValue);
        picassoIndicatorView.setChecked(picassoDebuggingValue);
        picassoIndicatorView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                Timber.d("Setting Picasso debugging to " + isChecked);
                picasso.setIndicatorsEnabled(isChecked);
                picassoDebugging.set(isChecked);
            }
        });

        refreshPicassoStats();
    }

    private void setupOkHttpCacheSection() {
        Cache cache = client.getCache(); // Shares the cache with apiClient, so no need to check both.
        okHttpCacheMaxSizeView.setText(getSizeString(cache.getMaxSize()));

        refreshOkHttpCacheStats();
    }

    private void showCustomEndpointDialog(final int originalSelection, String defaultUrl) {
        View view = LayoutInflater.from(app).inflate(R.layout.debug_drawer_network_endpoint, null);
        final EditText url = findById(view, R.id.debug_drawer_network_endpoint_url);
        url.setText(defaultUrl);
        url.setSelection(url.length());

        new AlertDialog.Builder(getContext()) //
                .setTitle("Set Network Endpoint")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        endpointView.setSelection(originalSelection);
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Use", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String theUrl = url.getText().toString();
                        if (!Strings.isBlank(theUrl)) {
                            setEndpointAndRelaunch(theUrl);
                        } else {
                            endpointView.setSelection(originalSelection);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        endpointView.setSelection(originalSelection);
                    }
                })
                .show();
    }

    private void setEndpointAndRelaunch(String endpoint) {
        Timber.d("Setting network endpoint to %s", endpoint);
        networkEndpoint.set(endpoint);

        ProcessPhoenix.triggerRebirth(getContext());
    }

    private void showNewNetworkProxyDialog(final ProxyAdapter proxyAdapter) {
        final int originalSelection = networkProxy.isSet() ? ProxyAdapter.PROXY : ProxyAdapter.NONE;

        View view = LayoutInflater.from(app).inflate(R.layout.debug_drawer_network_proxy, null);
        final EditText hostView = findById(view, R.id.debug_drawer_network_proxy_host);

        String host = networkProxy.get();
        if (!Strings.isBlank(host)) {
            hostView.setText(host); // Set the current host.
            hostView.setSelection(0, host.length()); // Pre-select it for editing.

            // Show the keyboard. Post this to the next frame when the dialog has been attached.
            hostView.post(new Runnable() {
                @Override
                public void run() {
                    Keyboards.showKeyboard(hostView);
                }
            });
        }

        new AlertDialog.Builder(getContext()) //
                .setTitle("Set Network Proxy")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        networkProxyView.setSelection(originalSelection);
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Use", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String theHost = hostView.getText().toString();
                        if (!Strings.isBlank(theHost)) {
                            networkProxy.set(theHost); // Persist across restarts.
                            proxyAdapter.notifyDataSetChanged(); // Tell the spinner to update.
                            networkProxyView.setSelection(ProxyAdapter.PROXY); // And show the proxy.

                            Proxy proxy = networkProxy.getProxy();
                            client.setProxy(proxy);
                            apiClient.setProxy(proxy);
                        } else {
                            networkProxyView.setSelection(originalSelection);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        networkProxyView.setSelection(originalSelection);
                    }
                })
                .show();
    }

    private void applyAnimationSpeed(int multiplier) {
        try {
            Method method = ValueAnimator.class.getDeclaredMethod("setDurationScale", float.class);
            method.invoke(null, (float) multiplier);
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply animation speed.", e);
        }
    }

    private static String getDensityString(DisplayMetrics displayMetrics) {
        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxxhdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            default:
                return String.valueOf(displayMetrics.densityDpi);
        }
    }

    private void refreshPicassoStats() {
        StatsSnapshot snapshot = picasso.getSnapshot();
        String size = getSizeString(snapshot.size);
        String total = getSizeString(snapshot.maxSize);
        int percentage = (int) ((1f * snapshot.size / snapshot.maxSize) * 100);
        picassoCacheSizeView.setText(size + " / " + total + " (" + percentage + "%)");
        picassoCacheHitView.setText(String.valueOf(snapshot.cacheHits));
        picassoCacheMissView.setText(String.valueOf(snapshot.cacheMisses));
        picassoDecodedView.setText(String.valueOf(snapshot.originalBitmapCount));
        picassoDecodedTotalView.setText(getSizeString(snapshot.totalOriginalBitmapSize));
        picassoDecodedAvgView.setText(getSizeString(snapshot.averageOriginalBitmapSize));
        picassoTransformedView.setText(String.valueOf(snapshot.transformedBitmapCount));
        picassoTransformedTotalView.setText(getSizeString(snapshot.totalTransformedBitmapSize));
        picassoTransformedAvgView.setText(getSizeString(snapshot.averageTransformedBitmapSize));
    }

    private static String getSizeString(long bytes) {
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int unit = 0;
        while (bytes >= 1024) {
            bytes /= 1024;
            unit += 1;
        }
        return bytes + units[unit];
    }

    private void refreshOkHttpCacheStats() {
        Cache cache = client.getCache(); // Shares the cache with apiClient, so no need to check both.
        int writeTotal = cache.getWriteSuccessCount() + cache.getWriteAbortCount();
        int percentage = (int) ((1f * cache.getWriteAbortCount() / writeTotal) * 100);
        okHttpCacheWriteErrorView.setText(
                cache.getWriteAbortCount() + " / " + writeTotal + " (" + percentage + "%)");
        okHttpCacheRequestCountView.setText(String.valueOf(cache.getRequestCount()));
        okHttpCacheNetworkCountView.setText(String.valueOf(cache.getNetworkCount()));
        okHttpCacheHitCountView.setText(String.valueOf(cache.getHitCount()));
    }

    /**
     * Populates a {@code Spinner} with the values of an {@code enum} and binds it to the value set
     * in
     * the mock service.
     */
    private <T extends Enum<T>> void configureResponseSpinner(Spinner spinner,
                                                              final Class<T> responseClass) {
        final EnumAdapter<T> adapter = new EnumAdapter<>(getContext(), responseClass);
        spinner.setEnabled(isMockMode);
        spinner.setAdapter(adapter);
//        spinner.setSelection(mockGithubService.getResponse(responseClass).ordinal());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                T selected = adapter.getItem(position);
//                if (selected != mockGithubService.getResponse(responseClass)) {
//                    Timber.d("Setting %s to %s", responseClass.getSimpleName(), selected);
//                    mockGithubService.setResponse(responseClass, selected);
//                } else {
//                    Timber.d("Ignoring re-selection of %s %s", responseClass.getSimpleName(), selected);
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public ContextualDebugActions getContextualDebugActions() {
        return contextualDebugActions;
    }

    public void onDrawerOpened() {
        refreshPicassoStats();
        refreshOkHttpCacheStats();
    }

    @OnClick(R.id.debug_network_endpoint_edit)
    void onEditEndpointClicked() {
        Timber.d("Prompting to edit custom endpoint URL.");
        // Pass in the currently selected position since we are merely editing.
        showCustomEndpointDialog(endpointView.getSelectedItemPosition(), networkEndpoint.get());
    }

    @OnClick(R.id.debug_logs_show)
    void showLogs() {
        new LogsDialog(new ContextThemeWrapper(getContext(), R.style.Theme_BronyTunes), lumberYard).show();
    }
}
