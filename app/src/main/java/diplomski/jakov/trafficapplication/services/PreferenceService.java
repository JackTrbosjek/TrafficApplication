package diplomski.jakov.trafficapplication.services;

import android.content.SharedPreferences;

import java.util.Date;

import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.Enums.TimeUnits;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;

public class PreferenceService {
    private static final String TOKEN_KEY = "diplomski.jakov.trafficapplication.tokenkey";
    private static final String TOKEN_EXPIRATION_KEY = "diplomski.jakov.trafficapplication.tokenkeyexpiration";
    private static final String USERNAME_KEY = "diplomski.jakov.trafficapplication.usernamekey";
    private static final String USER_ID_KEY = "diplomski.jakov.trafficapplication.userid";
    private static final String SYNC_REACTIVE_KEY = "diplomski.jakov.trafficapplication.syncreactive";
    private static final String SYNC_PROACTIVE_KEY = "diplomski.jakov.trafficapplication.syncproactive";
    private static final String SYNC_USER_KEY = "diplomski.jakov.trafficapplication.syncuser";
    private static final String SYNC_WIFI_ONLY_KEY = "diplomski.jakov.trafficapplication.syncwifionly";
    private static final String PROACTIVE_TYPE_KEY = "diplomski.jakov.trafficapplication.proactivetype";
    private static final String PROACTIVE_INTERVAL_KEY = "diplomski.jakov.trafficapplication.proactiveinterval";
    private static final String PROACTIVE_EVERY_UNITS_KEY = "diplomski.jakov.trafficapplication.proactiveeveryunits";
    private static final String PROACTIVE_FOR_DURATION_KEY = "diplomski.jakov.trafficapplication.proactiveforduration";
    private static final String PROACTIVE_FOR_UNITS_KEY = "diplomski.jakov.trafficapplication.proactiveforunits";
    private static final String REACTIVE_SUDDEN_STOPPING_KEY = "diplomski.jakov.trafficapplication.reactivesuddenstopping";
    private static final String REACTIVE_TRAFFIC_JAM_KEY = "diplomski.jakov.trafficapplication.reactivetrafficjam";
    private SharedPreferences preferences;

    public PreferenceService(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.commit();
    }

    public String getToken() {
        return preferences.getString(TOKEN_KEY, "");
    }

    public void saveTokenExpiration(long expiresIn) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(TOKEN_EXPIRATION_KEY, expiresIn + new Date().getTime());
        editor.commit();
    }

    public boolean tokenExpired(){
        long expires = preferences.getLong(TOKEN_EXPIRATION_KEY, 0);
        long now = new Date().getTime();
        return now > expires;
    }



    public String getAuthorization() {
        return "Bearer " + preferences.getString(TOKEN_KEY, "");
    }

    public void saveUsername(String userName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME_KEY, userName);
        editor.commit();
    }

    public String getUsername() {
        return preferences.getString(USERNAME_KEY, "");
    }

    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID_KEY, userId);
        editor.commit();
    }

    public String getUserId() {
        return preferences.getString(USER_ID_KEY, "");
    }

    public void saveSyncReactive(boolean syncReactive) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SYNC_REACTIVE_KEY, syncReactive);
        editor.commit();
    }

    public boolean getSyncReactive() {
        return preferences.getBoolean(SYNC_REACTIVE_KEY, false);
    }

    public void saveSyncProactive(boolean syncProactive) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SYNC_PROACTIVE_KEY, syncProactive);
        editor.commit();
    }

    public boolean getSyncProactive() {
        return preferences.getBoolean(SYNC_PROACTIVE_KEY, false);
    }

    public void saveSyncUser(boolean syncUser) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SYNC_USER_KEY, syncUser);
        editor.commit();
    }

    public boolean getSyncUser() {
        return preferences.getBoolean(SYNC_USER_KEY, false);
    }


    public void saveSyncWifiOnly(boolean syncWifiOnly) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SYNC_WIFI_ONLY_KEY, syncWifiOnly);
        editor.commit();
    }

    public boolean getSyncWifiOnly() {
        return preferences.getBoolean(SYNC_WIFI_ONLY_KEY, false);
    }

    public void saveProactiveType(FileType fileType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROACTIVE_TYPE_KEY, fileType.name());
        editor.commit();
    }

    public FileType getProactiveType() {
        String stringType = preferences.getString(PROACTIVE_TYPE_KEY, "");
        if (stringType.isEmpty()) {
            return FileType.PHOTO;
        }
        return FileType.valueOf(stringType);
    }

    public void saveProactiveInterval(String proactiveInterval) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROACTIVE_INTERVAL_KEY, proactiveInterval);
        editor.commit();
    }

    public String getProactiveInterval() {
        return preferences.getString(PROACTIVE_INTERVAL_KEY, "");
    }

    public void saveProactiveEveryUnits(TimeUnits timeUnits) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROACTIVE_EVERY_UNITS_KEY, timeUnits.name());
        editor.commit();
    }

    public TimeUnits getProactiveEveryUnits() {
        String stringType = preferences.getString(PROACTIVE_EVERY_UNITS_KEY, "");
        if (stringType.isEmpty()) {
            return TimeUnits.SEC;
        }
        return TimeUnits.valueOf(stringType);
    }

    public void saveProactiveForDuration(String forDuration) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROACTIVE_FOR_DURATION_KEY, forDuration);
        editor.commit();
    }

    public String getProactiveForDuration() {
        return preferences.getString(PROACTIVE_FOR_DURATION_KEY, "");
    }

    public void saveProactiveForUnits(VideoDurationUnits videoDurationUnits) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROACTIVE_FOR_UNITS_KEY, videoDurationUnits.name());
        editor.commit();
    }

    public VideoDurationUnits getProactiveForUnits() {
        String stringType = preferences.getString(PROACTIVE_FOR_UNITS_KEY, "");
        if (stringType.isEmpty()) {
            return VideoDurationUnits.SEC;
        }
        return VideoDurationUnits.valueOf(stringType);
    }

    public void saveReactiveSuddenStopping(boolean suddenStopping) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(REACTIVE_SUDDEN_STOPPING_KEY, suddenStopping);
        editor.commit();
    }

    public boolean getReactiveSuddenStopping() {
        return preferences.getBoolean(REACTIVE_SUDDEN_STOPPING_KEY, false);
    }

    public void saveReactiveTrafficJam(boolean trafficJam) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(REACTIVE_TRAFFIC_JAM_KEY, trafficJam);
        editor.commit();
    }

    public boolean getReactiveTrafficJam() {
        return preferences.getBoolean(REACTIVE_TRAFFIC_JAM_KEY, false);
    }

    public void saveAllSettings(boolean syncReactive, boolean syncProactive, boolean syncUser, boolean syncWifiOnly, FileType fileType, String proactiveInterval, TimeUnits timeUnits, String forDuration, VideoDurationUnits videoDurationUnits, boolean suddenStopping, boolean trafficJam) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SYNC_REACTIVE_KEY, syncReactive);
        editor.putBoolean(SYNC_PROACTIVE_KEY, syncProactive);
        editor.putBoolean(SYNC_USER_KEY, syncUser);
        editor.putBoolean(SYNC_WIFI_ONLY_KEY, syncWifiOnly);
        editor.putString(PROACTIVE_TYPE_KEY, fileType.name());
        editor.putString(PROACTIVE_INTERVAL_KEY, proactiveInterval);
        editor.putString(PROACTIVE_EVERY_UNITS_KEY, timeUnits.name());
        editor.putString(PROACTIVE_FOR_DURATION_KEY, forDuration);
        editor.putString(PROACTIVE_FOR_UNITS_KEY, videoDurationUnits.name());
        editor.putBoolean(REACTIVE_SUDDEN_STOPPING_KEY, suddenStopping);
        editor.putBoolean(REACTIVE_TRAFFIC_JAM_KEY, trafficJam);
        editor.commit();
    }
}
