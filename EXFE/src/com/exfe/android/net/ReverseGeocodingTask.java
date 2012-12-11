package com.exfe.android.net;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {

	public static final int MSG_ID_FILL_ADDRESS = 31421;

	private Context mContext;
	private Handler mHandler;

	public ReverseGeocodingTask(Context context, Handler handler) {
		super();
		mContext = context;
		mHandler = handler;
	}

	@Override
	protected Void doInBackground(Location... params) {
		Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        Location loc = params[0];
        List<Address> addresses = null;
        try {
            // Call the synchronous getFromLocation() method by passing in the lat/long values.
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            // Update UI field with the exception.
            //Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            // Format the first line of address (if available), city, and country name.
            String addressText = String.format("%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName());
            // Update the UI via a message handler.
            Message.obtain(mHandler, MSG_ID_FILL_ADDRESS, addressText).sendToTarget();
        }
		return null;
	}

}
