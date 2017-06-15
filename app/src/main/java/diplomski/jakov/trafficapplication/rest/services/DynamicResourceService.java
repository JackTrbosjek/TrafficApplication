package diplomski.jakov.trafficapplication.rest.services;

import diplomski.jakov.trafficapplication.rest.models.DynamicFileModel;
import diplomski.jakov.trafficapplication.rest.models.RegisterResponseModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DynamicResourceService {
    @POST("resources/File")
    Call<RegisterResponseModel> createFile(@Header("Authorization") String authorization, @Body DynamicFileModel model);
}
