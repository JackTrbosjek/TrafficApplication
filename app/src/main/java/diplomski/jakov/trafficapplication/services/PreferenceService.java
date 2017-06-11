package diplomski.jakov.trafficapplication.services;

import android.content.SharedPreferences;

public class PreferenceService {
    private static final String TOKEN_KEY = "diplomski.jakov.trafficapplication.tokenkey";
    private static final String USERNAME_KEY = "diplomski.jakov.trafficapplication.usernamekey";
    private static final String USER_ID_KEY = "diplomski.jakov.trafficapplication.useridkey";
    private SharedPreferences preferences;

    public PreferenceService(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void saveToken(String token){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY,token);
        editor.commit();
    }

    public String getToken(){
        return preferences.getString(TOKEN_KEY,"");
    }

    public String getAuthorization(){
        return  "Bearer " + preferences.getString(TOKEN_KEY,"");
    }

    public void saveUsername(String userName){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME_KEY,userName);
        editor.commit();
    }

    public String getUsername(){
        return preferences.getString(USERNAME_KEY,"");
    }

    public void saveUserId(String userId){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID_KEY,userId);
        editor.commit();
    }

    public String getUserId(){
        return preferences.getString(USER_ID_KEY,"");
    }
}
