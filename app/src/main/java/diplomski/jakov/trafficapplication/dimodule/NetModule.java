package diplomski.jakov.trafficapplication.dimodule;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.icu.util.TimeUnit;
import android.preference.PreferenceManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.AppDatabase;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.rest.services.AuthenticationService;
import diplomski.jakov.trafficapplication.rest.services.DynamicResourceService;
import diplomski.jakov.trafficapplication.rest.services.FileService;
import diplomski.jakov.trafficapplication.services.FileUploadService;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.services.PreferenceService;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetModule {

    String mBaseUrl;

    // Constructor needs one parameter to instantiate.
    public NetModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    // Dagger will only look for methods annotated with @Provides
    @Provides
    @Singleton
    // Application reference must come from AppModule.class
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        return client;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(mBaseUrl)
                .client(okHttpClient)
                .build();
        return retrofit;
    }

    @Provides
    @Singleton
    AuthenticationService provideAuthenticationService(Retrofit retrofit) {
        return retrofit.create(AuthenticationService.class);
    }

    @Provides
    @Singleton
    DynamicResourceService provideDynamicResourceService(Retrofit retrofit) {
        return retrofit.create(DynamicResourceService.class);
    }

    @Provides
    @Singleton
    FileService provideFileService(Retrofit retrofit) {
        return retrofit.create(FileService.class);
    }

    @Provides
    @Singleton
    FileUploadService provideFileUploadService(FileService fileService, Application application, PreferenceService preferenceService, LocalFileDao localFileDao, AuthenticationService authenticationService, DynamicResourceService dynamicResourceService) {
        return new FileUploadService(fileService, application.getApplicationContext(), preferenceService, localFileDao, authenticationService, dynamicResourceService);
    }

    @Provides
    @Singleton
    PreferenceService providesPreferencesService(SharedPreferences preferences) {
        return new PreferenceService(preferences);
    }

    @Provides
    @Singleton
    AppDatabase provideRoomDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "LocalFileDatabase").allowMainThreadQueries().build();
    }

    @Provides
    @Singleton
    LocalFileDao provideLocalFileDao(AppDatabase appDatabase) {
        return appDatabase.localFileDao();
    }

    @Provides
    @Singleton
    LocalFileService providesLocalFileService(Application application, LocalFileDao localFileDao) {

        return new LocalFileService(application, localFileDao);
    }
}
