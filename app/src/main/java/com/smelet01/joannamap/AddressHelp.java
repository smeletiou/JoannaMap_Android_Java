package com.smelet01.joannamap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressHelp {
    public static String getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i)).append(", ");
                }
                String addrs = addressText.toString().trim();
                while (addrs.endsWith(",")) {
                    addrs = addrs.substring(0, addrs.length() - 1);
                }
                return addrs;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if no address is found
    }
}
