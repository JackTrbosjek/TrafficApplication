package diplomski.jakov.trafficapplication.rest.models;

import com.google.gson.annotations.SerializedName;


public class RegisterModel {

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("confirmPassword")
    public String confirmPassword;

    @SerializedName("activationUrl")
    public String activationUrl;

    @SerializedName("username")
    public String username;

    public RegisterModel(String email, String password, String confirmPassword) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.activationUrl = "activation.com.jakov?token={activationToken}";
        this.username = email;
    }
}
