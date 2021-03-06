package com.bronytunes.app;

import android.app.Application;

import com.bronytunes.app.data.DataModule;
import com.bronytunes.app.db.DatabaseModule;
import com.bronytunes.app.ui.MainActivity;
import com.bronytunes.app.ui.SettingsActivity;
import com.bronytunes.app.ui.TrackListingFragment;
import com.bronytunes.app.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by berwyn on 03/09/14.
 */
@Module(
        includes = {
                UiModule.class,
                DataModule.class,
                DatabaseModule.class
        },
        injects = {
                BronyTunesApp.class,
                MainActivity.class,
                SettingsActivity.class,
                TrackListingFragment.class
        }
)
public class BronyTunesModule {

    private final BronyTunesApp app;

    public BronyTunesModule(BronyTunesApp app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }

}
