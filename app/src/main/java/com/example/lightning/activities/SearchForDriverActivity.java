package com.example.lightning.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightning.R;
import com.example.lightning.models.CurrentPosition;
import com.example.lightning.models.Trip;
import com.example.lightning.services.MyLocationServices;
import com.example.lightning.tools.Const;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchForDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText edtPickUp, edtDes;
    ImageView imgVehicle;
    TextView textVehicleType, textTimeCost, textMoneyCost;

    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 123;
    private static final int ACCESS_COARSE_LOCATION_REQUEST_CODE = 234;
    MyLocationServices mLocationService;
    private FusedLocationProviderClient fusedLocationClient;
    Intent mServiceIntent;

    public final int maxLength = 34;

    private String tripId;
    private Trip trip;
    public static GoogleMap map;
    public static Marker currentLocationMarker;
    public static String markerIconName = "lightning_circle";
    LatLng UET;

    static SearchForDriverActivity instance;
    public static SearchForDriverActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_driver);

        init();
    }

    private void setTripInfo() {
        edtDes.setText(trip.getDropOffName());
        edtPickUp.setText(trip.getPickUpName());
        textTimeCost.setText(trip.getTimeCost());
        textMoneyCost.setText(trip.getCost());

        if (trip.getVehicleType().equals(Const.car)) {
            imgVehicle.setImageResource(R.drawable.car);
            textVehicleType.setText("Car");
        } else {
            imgVehicle.setImageResource(R.drawable.shipper);
            textVehicleType.setText("Motorbike");
        }
    }

    private void init() {
        instance = this;

        edtDes = findViewById(R.id.edt_dropOfPos);
        edtPickUp = findViewById(R.id.edt_pickUpPos);
        textMoneyCost = findViewById(R.id.text_moneyCost);
        textTimeCost = findViewById(R.id.text_timeCost);
        textVehicleType = findViewById(R.id.text_vehicleType);
        imgVehicle = findViewById(R.id.imgVehicle);

        edtDes.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        edtPickUp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        UET = new LatLng(21.038902482537342, 105.78296809797327); //Dai hoc Cong Nghe Lat Lng

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        if (intent.getStringExtra("tripId") == null) {
           getLastedTrip();
        } else {
            tripId = intent.getStringExtra("tripId");
            getTripInfo(tripId);
        }
    }

    private void getLastedTrip() {
        List<Trip> listTrips = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            if (trip != null)
                                if (trip.getPassengerId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                    listTrips.add(trip);
                                }
                        }
                        if (listTrips.size() > 0) {
                            trip = listTrips.get(listTrips.size() - 1);
                            setTripInfo();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getTripInfo(String tripId) {
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .child(tripId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        trip = snapshot.getValue(Trip.class);
                        if (trip != null) {
                            updateTripStatus(Const.searching, trip);
                        }
                        setTripInfo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateTripStatus(String status, Trip trip) {
        FirebaseDatabase.getInstance().getReference().child("Activities").child("Trips")
                .child(status)
                .setValue(tripId);

        trip.setStatus(status);
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .child(trip.getId())
                .setValue(trip);
    }

    public void updateLocationOnFirebase(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        CurrentPosition currentPosition = new CurrentPosition(
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                latLng.toString(),
                Calendar.getInstance().getTime().toString()
        );

        FirebaseDatabase.getInstance().getReference().child("CurrentPosition")
                .child("Passenger")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(currentPosition);
    }

    private void markCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermission();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (map != null) {
                            if (currentLocationMarker != null) {
                                currentLocationMarker.remove();
                            }
                            currentLocationMarker = map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("You are here!")
                                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(markerIconName, 120, 120))));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        } else {
                            Toast.makeText(SearchForDriverActivity.this, "Map is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_FINE_LOCATION_REQUEST_CODE
        );

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                ACCESS_COARSE_LOCATION_REQUEST_CODE
        );
    }

    private void startServiceFunc(){
        mLocationService = new MyLocationServices();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (!isMyServiceRunning(mLocationService.getClass(), this)) {
            startService(mServiceIntent);
            Toast.makeText(this, "Service start successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopServiceFunc(){
        mLocationService = new MyLocationServices();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (isMyServiceRunning(mLocationService.getClass(), this)) {
            stopService(mServiceIntent);
            Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Activity mActivity) {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        markCurrentLocation();
    }
}