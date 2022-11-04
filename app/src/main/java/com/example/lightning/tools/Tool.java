package com.example.lightning.tools;

import android.app.Activity;
import android.location.Location;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;

public class Tool {
    public static LatLng getLatLngFromString(String location) {
        LatLng latLng;

        String tempString = location.substring(location.indexOf("(")+1, location.indexOf(")"));
        String[] arrOfStr = tempString.split(",", 2);

        latLng = new LatLng(Double.parseDouble(arrOfStr[0]), Double.parseDouble(arrOfStr[1]));

        return latLng;
    }

    public static double calculateDistance(LatLng origin, LatLng dest) {
        Location locationOri = new Location("LocationA");
        locationOri.setLatitude(origin.latitude);
        locationOri.setLongitude(origin.longitude);

        Location locationDest = new Location("LocationA");
        locationDest.setLatitude(dest.latitude);
        locationDest.setLongitude(dest.longitude);

        return locationOri.distanceTo(locationDest);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }
}
