package diplomski.jakov.trafficapplication.base;

import diplomski.jakov.trafficapplication.dimodule.AppModule;
import diplomski.jakov.trafficapplication.dimodule.DaggerNetComponent;
import diplomski.jakov.trafficapplication.dimodule.NetComponent;
import diplomski.jakov.trafficapplication.dimodule.NetModule;

public class Application extends android.app.Application {
    private NetComponent mNetComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule("https://api.baasic.com/v1/traffic-application/"))
                .build();
    }

    public NetComponent getNetComponent() {
        return mNetComponent;
    }
}
