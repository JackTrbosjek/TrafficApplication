package diplomski.jakov.trafficapplication.services;

import diplomski.jakov.trafficapplication.models.LoginModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginModel> login(@Field("username") String username, @Field("password") String password, @Field("grant_type") String type);
}
