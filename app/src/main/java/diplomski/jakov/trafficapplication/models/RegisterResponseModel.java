package diplomski.jakov.trafficapplication.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class RegisterResponseModel {

    @SerializedName("creationDate")
    public Date creationDate;

    @SerializedName("email")
    public String email;

    @SerializedName("name")
    public String name;

    @SerializedName("userName")
    public String userName;

    @SerializedName("id")
    public String id;
}
