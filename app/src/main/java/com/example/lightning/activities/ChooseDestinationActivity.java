package com.example.lightning.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.example.lightning.models.Trip;
import com.example.lightning.tools.Const;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class ChooseDestinationActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText edtPickUp, edtDestination;
    AppCompatButton buttonConfirm;
    LinearLayout layoutBottom;
    FloatingActionButton btnSearchPickUp, btnSearchDes;
    TextView textTimeMotor, textTimeCar, textCostMotor, textCostCar;
    RelativeLayout layoutMotor, layoutCar;

    private static final Integer CHOOSE_DES_REQUEST_CODE = 1;
    private static final Integer CHOOSE_PICK_UP_REQUEST_CODE = 2;
    private static final String pickUpMarkerName = "pick_up_marker";
    private static final String desMarkerName = "flag";
    private static final int markerSize = 120;

    public static String MAPS_API_KEY;
    public static String GOONG_API_KEY;

    boolean motorIsChosen = false, carIsChosen = false, distanceIsCalculated = false;
    boolean tripIsCreatedOnFirebase = false;
    Trip trip;

    LatLng pickUpPos, destination, UET;
    String distance, timeCost, moneyCostCar, moneyCostMotor, pickUpName, desName;

    GoogleMap map;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination);

        loadCurrentApiKey();
        init();
        setStatusBarColor();
        listener();

    }

    private void loadCurrentApiKey() {
        FirebaseDatabase.getInstance().getReference().child("Current-API-KEY")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MAPS_API_KEY = snapshot.getValue(String.class);
                        assert MAPS_API_KEY != null;
                        Places.initialize(ChooseDestinationActivity.this, MAPS_API_KEY);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference().child("GOONG_API_KEY")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GOONG_API_KEY = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void listener() {
        btnSearchPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edtPickUp.getText().toString().trim();
                if (!(input.isEmpty())) {
                    getAutoCompleteDestination(CHOOSE_PICK_UP_REQUEST_CODE, input);
                }
            }
        });

        btnSearchDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edtDestination.getText().toString().trim();
                if (!(input.isEmpty())) {
                    getAutoCompleteDestination(CHOOSE_DES_REQUEST_CODE, input);
                }
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && pickUpPos != null && destination != null && !distanceIsCalculated) {
                    try {
                        direction(pickUpPos, destination);
                        buttonConfirm.setText("Confirm");
                    } catch (IOException e) {
                        Toast.makeText(ChooseDestinationActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                if ((carIsChosen || motorIsChosen) && checkTripData()) {
                    if (carIsChosen) {
                        trip = new Trip(
                                FirebaseAuth.getInstance().getUid(),
                                pickUpPos.toString(),
                                destination.toString(),
                                distance,
                                moneyCostCar,
                                timeCost,
                                Const.car,
                                Calendar.getInstance().getTime().toString()
                        );
                    } else if (motorIsChosen) {
                        trip = new Trip(
                                FirebaseAuth.getInstance().getUid(),
                                pickUpPos.toString(),
                                destination.toString(),
                                distance,
                                moneyCostMotor,
                                timeCost,
                                Const.motor,
                                Calendar.getInstance().getTime().toString()
                        );
                    }
                    if (!(tripIsCreatedOnFirebase)) {
                        createNewTripOnFirebase(trip);
                        tripIsCreatedOnFirebase = true;
                    }
                }
            }
        });

        layoutMotor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMotor.setBackgroundColor(getResources().getColor(R.color.selected_blue));
                layoutCar.setBackgroundColor(getResources().getColor(R.color.white));
                motorIsChosen = true;
                carIsChosen = false;
            }
        });

        layoutCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMotor.setBackgroundColor(getResources().getColor(R.color.white));
                layoutCar.setBackgroundColor(getResources().getColor(R.color.selected_blue));
                motorIsChosen = false;
                carIsChosen = true;
            }
        });
    }

    private void createNewTripOnFirebase(Trip trip) {
        trip.setStatus(Const.active);
        trip.setPickUpName(pickUpName);
        trip.setDropOffName(desName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your trip...");
        progressDialog.show();

        String key = FirebaseDatabase.getInstance().getReference().child("Trips")
                .push().getKey();
        trip.setId(key);
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .child(trip.getId())
                .setValue(trip)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(ChooseDestinationActivity.this, SearchForDriverActivity.class);
                        intent.putExtra("tripId", trip.getId());
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChooseDestinationActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.show();
                    }
                });
    }

    private boolean checkTripData() {
        return !distance.isEmpty() && !moneyCostMotor.isEmpty() && !moneyCostCar.isEmpty()
                && !timeCost.isEmpty() && pickUpPos != null && destination != null;
    }

    private void getAutoCompleteDestination(Integer requestCode, String input) {
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.NAME);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                fieldList)
                .setInitialQuery(input)
                .build(ChooseDestinationActivity.this);

        startActivityForResult(intent, requestCode);
    }

    private void init() {
        edtDestination = findViewById(R.id.edt_dropOfPos);
        edtPickUp = findViewById(R.id.edt_pickUpPos);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        layoutBottom = findViewById(R.id.layoutBottom);
        btnSearchDes = findViewById(R.id.button_search_des);
        btnSearchPickUp = findViewById(R.id.button_search_pickUp);
        textCostCar = findViewById(R.id.text_money_car);
        textCostMotor = findViewById(R.id.text_money_motor);
        textTimeCar = findViewById(R.id.text_time_car);
        textTimeMotor = findViewById(R.id.text_time_motor);
        layoutCar = findViewById(R.id.layoutCar);
        layoutMotor = findViewById(R.id.layoutMotor);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);

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
            int maxLength = 28;
            edtDestination.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            edtDestination.setText(place.getAddress());

            destination = place.getLatLng();
            distanceIsCalculated = false;
            desName = place.getAddress();
            //mark this location to google map
            if (map != null) {
                markLocation(destination, 1);
            }
        } else if (requestCode == CHOOSE_PICK_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            int maxLength = 28;
            edtPickUp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            edtPickUp.setText(place.getAddress());

            pickUpPos = place.getLatLng();
            distanceIsCalculated = false;
            pickUpName = place.getAddress();
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
        String url = Uri.parse("https://rsapi.goong.io/Direction")
                .buildUpon()
                .appendQueryParameter("origin", strOrigin)
                .appendQueryParameter("destination", strDestination)
                .appendQueryParameter("vehicle", "bike")
                .appendQueryParameter("api_key", GOONG_API_KEY)
                .toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                        JSONArray routes = response.getJSONArray("routes");

                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for (int i=0;i<routes.length();i++){
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            getDistanceAndTimeCost(legs);

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
                            polylineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.green));
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
                        distanceIsCalculated = true;
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

    private void getDistanceAndTimeCost(JSONArray legs) throws JSONException {
        JSONObject object = legs.getJSONObject(0);
        JSONObject distanceObj = object.getJSONObject("distance");
        JSONObject durationObj = object.getJSONObject("duration");
        distance = distanceObj.getString("text");
        timeCost = durationObj.getString("text");

        textTimeMotor.setText(String.format("%s (%s)", timeCost, distance));
        textTimeCar.setText(String.format("%s (%s)", timeCost, distance));

        //calculate money cost
        String[] arrayDistance = distance.split(" ");
        double distanceDouble = Double.parseDouble(arrayDistance[0]);
        double costCarDouble = Const.costPerKmCar * distanceDouble;
        double costMotorDouble = Const.costPerKmMotor * distanceDouble;

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        moneyCostCar = nf.format(costCarDouble);
        moneyCostMotor = nf.format(costMotorDouble);
        textCostMotor.setText(moneyCostMotor);
        textCostCar.setText(moneyCostCar);
    }

    private List<LatLng> decodePoly(String encoded){

        return PolyUtil.decode(encoded);
    }

    private void markLocation(LatLng location, int type) { //type: 0 is pick up point, 1 is destination
        if (type == 0) {
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .title("You are here!")
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(pickUpMarkerName, markerSize, markerSize))));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        } else if (type == 1) {
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Your destination")
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(desMarkerName, markerSize, markerSize))));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
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