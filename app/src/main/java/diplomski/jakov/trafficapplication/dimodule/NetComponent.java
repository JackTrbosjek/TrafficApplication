package diplomski.jakov.trafficapplication.dimodule;

import javax.inject.Singleton;

import dagger.Component;
import diplomski.jakov.trafficapplication.ActivateActivity;
import diplomski.jakov.trafficapplication.CameraPreviewActivity;
import diplomski.jakov.trafficapplication.FileFragment;
import diplomski.jakov.trafficapplication.HomeFragment;
import diplomski.jakov.trafficapplication.LoginActivity;
import diplomski.jakov.trafficapplication.RegisterActivity;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface NetComponent {
    void inject(LoginActivity activity);
    void inject(RegisterActivity activity);
    void inject(ActivateActivity activity);
    void inject(HomeFragment fragment);
    void inject(FileFragment fragment);
    void inject(CameraPreviewActivity activity);
}