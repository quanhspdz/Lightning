package com.example.lightning.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lightning.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseDestinationActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText edtPickUp, edtDestination;
    AppCompatButton buttonConfirm;
    LinearLayout layoutBottom;

    private static final Integer CHOOSE_DES_REQUEST_CODE = 1;
    private static final Integer CHOOSE_PICK_UP_REQUEST_CODE = 2;

    LatLng pickUpPos, destination, UET;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination);

        init();
        setStatusBarColor();
        listener();

    }

    private void listener() {
        edtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAutoCompleteDestination(CHOOSE_DES_REQUEST_CODE);
            }
        });

        edtPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAutoCompleteDestination(CHOOSE_PICK_UP_REQUEST_CODE);
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && pickUpPos != null && destination != null) {
                    try {
                        direction(pickUpPos, destination);
                    } catch (IOException e) {
                        Toast.makeText(ChooseDestinationActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getAutoCompleteDestination(Integer requestCode) {
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.NAME);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                fieldList).build(ChooseDestinationActivity.this);

        startActivityForResult(intent, requestCode);
    }

    private void init() {
        edtDestination = findViewById(R.id.edt_dropOfPos);
        edtPickUp = findViewById(R.id.edt_pickUpPos);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        layoutBottom = findViewById(R.id.layoutBottom);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id  .fragment_maps);
        mapFragment.getMapAsync(this);

        Places.initialize(ChooseDestinationActivity.this, getResources().getString(R.string.MAPS_API_KEY));

        UET = new LatLng(21.038902482537342, 105.78296809797327); //Dai hoc Cong Nghe Lat Lng
    }

    private void setStatusBarColor() {
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue_toolbar));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_DES_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            int maxLength = 33;
            edtDestination.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            edtDestination.setText(place.getAddress());

            destination = place.getLatLng();

            //mark this location to google map
            if (map != null) {
                markLocation(destination, 1);
            }
        } else if (requestCode == CHOOSE_PICK_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            int maxLength = 33;
            edtPickUp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            edtPickUp.setText(place.getAddress());

            pickUpPos = place.getLatLng();

            //mark this location to google map
            if (map != null) {
                markLocation(pickUpPos, 0);
            }
        }
    }

    private void direction(LatLng origin, LatLng destination) throws IOException {
        map.clear();
        markLocation(origin, 0);
        markLocation(destination, 1);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Calculating route...");
        progressDialog.show();

        String strOrigin = origin.latitude + ", " + origin.longitude;
        String strDestination = destination.latitude + ", " + destination.longitude;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination", strDestination)
                .appendQueryParameter("origin", strOrigin)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getResources().getString(R.string.MAPS_API_KEY))
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");

                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for (int i=0;i<routes.length();i++){
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            for (int j=0;j<legs.length();j++){
                                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");

                                for (int k=0;k<steps.length();k++){
                                    String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                    List<LatLng> list = decodePoly(polyline);

                                    for (int l=0;l<list.size();l++){
                                        LatLng position = new LatLng((list.get(l)).latitude, (list.get(l)).longitude);
                                        points.add(position);
                                    }
                                }
                            }
                            polylineOptions.addAll(points);
                            polylineOptions.width(10);
                            polylineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                            polylineOptions.geodesic(true);
                        }

                        assert polylineOptions != null;
                        map.addPolyline(polylineOptions);
                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(new LatLng(destination.latitude, destination.longitude))
                                .include(new LatLng(origin.latitude, origin.longitude)).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point.x, 800, 250));

                        layoutBottom.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ChooseDestinationActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChooseDestinationActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decodePoly(String encoded){

        return PolyUtil.decode(encoded);
    }

    private void markLocation(LatLng location, int type) { //type: 0 is pick up point, 1 is destination
        if (type == 0) {
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .title("You are here!")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        } else if (type == 1) {
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Your destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UET, 10));

        if (pickUpPos != null) {
            markLocation(pickUpPos, 0);
        }
        if (destination != null) {
            markLocation(destination, 1);
        }
    }
}