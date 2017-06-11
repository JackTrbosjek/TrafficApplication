package diplomski.jakov.trafficapplication.rest.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UserInfoResponse {
    @SerializedName("displayName")
    public String displayName;

    @SerializedName("email")
    public String email;

    @SerializedName("userName")
    public String userName;

    @SerializedName("id")
    public String id;
}
