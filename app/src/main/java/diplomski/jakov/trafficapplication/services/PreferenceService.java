package diplomski.jakov.trafficapplication.services;

import android.content.SharedPreferences;

public class PreferenceService {
    private static final String TOKEN_KEY = "diplomski.jakov.trafficapplication.tokenkey";
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
}
