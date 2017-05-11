package diplomski.jakov.trafficapplication.models;


import com.google.gson.annotations.SerializedName;

public class LoginModel {

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("access_url_token")
    public String accessUrlToken;

    @SerializedName("expires_in")
    public long expiresIn;

    @SerializedName("token_type")
    public String tokenType;

}
