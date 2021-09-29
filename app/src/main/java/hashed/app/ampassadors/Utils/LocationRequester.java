package hashed.app.ampassadors.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import hashed.app.ampassadors.Activities.sign_up;

public class LocationRequester {

  private static final int REQUEST_CHECK_SETTINGS = 100;
  private final Context context;
  private final Activity activity;
  private Boolean mRequestingLocationUpdates;
  private FusedLocationProviderClient fusedLocationClient;
  private LocationRequest locationRequest;
  private LocationCallback locationCallback;
  private ProgressDialog progressDialog;
  private int retries = 0;
  private EditText countryEd;
  private EditText cityEd;
  private ImageView locationIv;
  public String countryCode;

  private LocationRequesterListener locationRequesterListener;


  public interface LocationRequesterListener{
    void onAddressFetched(String country,String city);
  }

  public LocationRequester(Context context, Activity activity) {
    this.activity = activity;
    this.context = context;
  }
  public LocationRequester(Context context,Activity activity,LocationRequesterListener locationRequesterListener) {
    this.context = context;
    this.activity = activity;
    this.locationRequesterListener = locationRequesterListener;
  }

  public LocationRequester(Context context, Activity activity, EditText countryEd,
                           EditText cityEd, ImageView locationIv) {

    this.activity = activity;
    this.context = context;
    this.countryEd = countryEd;
    this.cityEd = cityEd;
    this.locationIv = locationIv;

    progressDialog = new ProgressDialog(context);
    progressDialog.setMessage("Fetching location info!");
    progressDialog.setCancelable(false);
    progressDialog.show();

  }


  @SuppressLint("MissingPermission")
  public void geCountryFromLocation(){

    Log.d("ttt","getting last known location");

    if(fusedLocationClient != null && locationCallback != null){

      fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,
              Looper.myLooper());

      return;
    }

    locationRequest = LocationRequest.create().
            setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(10000).setFastestInterval(5000);

    final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);

    LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(locationSettingsResponse -> {
              Log.d("ttt", "location is enabled");

              getLastKnownLocation();

            }).addOnFailureListener(e -> {
      if (e instanceof ResolvableApiException) {
        Log.d("ttt", "location is not enabled");
        try {
          final ResolvableApiException resolvable = (ResolvableApiException) e;
          resolvable.startResolutionForResult(activity,
                  REQUEST_CHECK_SETTINGS);

        } catch (IntentSender.SendIntentException sendEx) {
          // Ignore the error.
        }
      }
    });

  }

  @SuppressLint("MissingPermission")
  public void getLastKnownLocation(){

    Log.d("ttt","getting last known location");
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

    fusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
              @Override
              public void onSuccess(Location location) {
                if(location == null){

                  Log.d("ttt","last location is null");

                  mRequestingLocationUpdates = true;

                  fusedLocationClient.requestLocationUpdates(locationRequest,
                          locationCallback = addLocationCallback(),
                          Looper.getMainLooper());

                }else{

                  getCountryInfoFromLocation(location);

                  Log.d("ttt","last known location: " + location);

                }

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback = addLocationCallback(),
                Looper.getMainLooper());

        Log.d("ttt","last known failed: "+e.getMessage());
      }
    });

  }

  @SuppressLint("MissingPermission")
  public void resumeLocationUpdates(){

    if (mRequestingLocationUpdates != null && !mRequestingLocationUpdates
    && locationCallback != null && fusedLocationClient!=null) {
        mRequestingLocationUpdates = true;

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback = addLocationCallback(),
                Looper.getMainLooper());
      }

  }

  public void stopLocationUpdates() {
    Log.d("ttt", "stopping location updates");
    if (mRequestingLocationUpdates != null && mRequestingLocationUpdates) {
      if (locationCallback != null && fusedLocationClient!=null) {
        mRequestingLocationUpdates = false;
        dismissProgressDialog();
        fusedLocationClient.removeLocationUpdates(locationCallback);
      }
    }
  }


  void dismissProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  LocationCallback addLocationCallback(){

   return new LocationCallback() {
      @Override
      public void onLocationResult(@NotNull LocationResult locationResult) {

        if (locationResult == null) {
          return;
        }

        for (Location location : locationResult.getLocations()) {
            Log.d("ttt", "location result is not null");

            if(location!=null){

              stopLocationUpdates();
              getCountryInfoFromLocation(location);

              break;
            }
        }

      }

    };

  }

  private void getCountryInfoFromLocation(Location location){

    final Geocoder geocoder = new Geocoder(context, new Locale("en"));

    try {

      final List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
              location.getLongitude(), 1);

      if (!addresses.isEmpty() && addresses.get(0).getCountryName() != null) {

        final Address a = addresses.get(0);

        final String country = a.getCountryName();
        countryCode= a.getCountryCode();
        final String city = a.getLocality();

        if(locationRequesterListener!=null){

          locationRequesterListener.onAddressFetched(country,city);

        }else{
          countryEd.setText(country);
          cityEd.setText(city);
          locationIv.setVisibility(View.GONE);

          ((sign_up)activity).selectDefaultPhoneCode(countryCode.toUpperCase());
          dismissProgressDialog();
        }
        Log.d("ttt",a.toString());


      } else {
        fetchFromApi(location.getLatitude(), location.getLongitude());
      }
    } catch (IOException e) {
      stopLocationUpdates();

      fetchFromApi(location.getLatitude(), location.getLongitude());
      Log.d("ttt", "geocoder error:" + e.getLocalizedMessage());
    }

  }

  private void fetchFromApi(double latitude, double longitude) {

    final String url =
            "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q="
            + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

    final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
      try {
        if (response.getJSONObject("status").getString("message")
                .equalsIgnoreCase("ok")) {

          final JSONObject address = response.getJSONArray("results")
                  .getJSONObject(0).getJSONObject("components");

          final String country = address.getString("country");
          countryCode = address.getString("country_code");
          final String city = address.getString("city");

          if(locationRequesterListener!=null){

            locationRequesterListener.onAddressFetched(country,city);

          }else{

            countryEd.setText(country);
            cityEd.setText(city);
            locationIv.setVisibility(View.GONE);
            ((sign_up)activity).selectDefaultPhoneCode(countryCode.toUpperCase());
            dismissProgressDialog();

          }



        } else {
          failedInfo();
          Log.d("ttt", "error here man 3: "+
                  response.getJSONObject("status").getString("message"));
        }
      } catch (JSONException e) {
        failedInfo();
        Log.d("ttt", "error here man 1: "+e.getMessage());
        e.printStackTrace();
      }
    }, error -> {

      if(retries < 3){
        retries++;

        fetchFromApi(latitude,longitude);
      }else{
        failedInfo();
      }
//      else{
//        Toast.makeText(context, "حصلت مشكلة! حاول اعادة تشغيل التطبيق"
//                , Toast.LENGTH_SHORT).show();
//        ((Activity) context).finish();
//      }
      Log.d("ttt", "error here man 2: "+error.getMessage());
    });
    queue.add(jsonObjectRequest);
//    queue.start();
  }

  private void failedInfo(){
    Toast.makeText(context, "Failed while trying to fetch location info!" +
                    "Please try again",
            Toast.LENGTH_SHORT).show();
    locationIv.setClickable(true);
  }

}

