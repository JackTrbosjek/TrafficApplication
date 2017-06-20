package diplomski.jakov.trafficapplication.dimodule;

import javax.inject.Singleton;

import dagger.Component;
import diplomski.jakov.trafficapplication.ActivateActivity;
import diplomski.jakov.trafficapplication.FileFragment;
import diplomski.jakov.trafficapplication.HomeFragment;
import diplomski.jakov.trafficapplication.LoginActivity;
import diplomski.jakov.trafficapplication.MainActivity;
import diplomski.jakov.trafficapplication.RegisterActivity;
import diplomski.jakov.trafficapplication.SettingsFragment;
import diplomski.jakov.trafficapplication.services.LocalFileGPSService;
import diplomski.jakov.trafficapplication.services.ProactiveService;
import diplomski.jakov.trafficapplication.services.ReactiveService;
import diplomski.jakov.trafficapplication.services.SuddenStoppingDetectionService;
import diplomski.jakov.trafficapplication.services.TrafficJamGPSService;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface NetComponent {
    void inject(LoginActivity activity);
    void inject(RegisterActivity activity);
    void inject(ActivateActivity activity);
    void inject(HomeFragment fragment);
    void inject(FileFragment fragment);
    void inject(ProactiveService proactiveService);
    void inject(ReactiveService reactiveService);
    void inject(TrafficJamGPSService trafficJamGPSService);
    void inject(SuddenStoppingDetectionService suddenStoppingDetectionService);
    void inject(LocalFileGPSService localFileGpsService);
    void inject(MainActivity activity);
    void inject(SettingsFragment fragment);
}
