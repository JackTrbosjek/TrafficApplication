package diplomski.jakov.trafficapplication.rest.services;

import diplomski.jakov.trafficapplication.rest.models.LoginModel;
import diplomski.jakov.trafficapplication.rest.models.RegisterModel;
import diplomski.jakov.trafficapplication.rest.models.RegisterResponseModel;
import diplomski.jakov.trafficapplication.rest.models.UserInfoResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AuthenticationService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginModel> login(@Field("username") String username, @Field("password") String password, @Field("grant_type") String type);

    @POST("register")
    Call<RegisterResponseModel> register(@Body RegisterModel model);

    @PUT("register/activate/{activationToken}")
    Call<ResponseBody> activate(@Path("activationToken") String activationToken);

    @GET("login")
    Call<UserInfoResponse> userInfo(@Header("Authorization") String authorization);
}
