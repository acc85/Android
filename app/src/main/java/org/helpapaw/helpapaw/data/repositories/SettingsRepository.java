package org.helpapaw.helpapaw.data.repositories;

import android.content.SharedPreferences;

public class SettingsRepository implements ISettingsRepository {

    private SharedPreferences preferences;

    public SettingsRepository(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void saveRadius(int radius) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("radius", radius);
        editor.apply();
    }

    @Override
    public void saveTimeout(int timeout) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("timeout", timeout);
        editor.apply();
    }

    @Override
    public int getRadius() {
        return preferences.getInt("radius", 10);
    }

    @Override
    public int getTimeout() {
        return preferences.getInt("timeout", 7);
    }

    @Override
    public double getLatitude() {
        String lat = preferences.getString("latitude", "0");
        return Double.valueOf(lat);
    }

    @Override
    public void setLatitude(double latitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.apply();
    }

    @Override
    public double getLongitude() {
        String longi = preferences.getString("longitude", "0");
        return Double.valueOf(longi);
    }

    @Override
    public void setLongitude(double longitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("longitude", String.valueOf(longitude));
        editor.apply();
    }

    @Override
    public float getZoom() {
        return preferences.getFloat("zoom", 0f);
    }

    @Override
    public void setZoom(float zoom) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("zoom", zoom);
        editor.apply();
    }
}
