package diplomski.jakov.trafficapplication.dimodule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import diplomski.jakov.trafficapplication.base.Application;

@Module
public class AppModule {

    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }
}