package com.torchcorp.tractrix;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapview;
    GoogleMap mGoogleMap;
    Bitmap BitMapMarker;

    Handler handler = new Handler();
    Runnable runnable;
    ArrayList<LatLng> path = new ArrayList<LatLng>();
    ArrayList<LatLng> passedPath = new ArrayList<LatLng>();
    ArrayList<LatLng> remainingPath = new ArrayList<LatLng>();


    ImageButton sideBarBtn;
    ConstraintLayout sideBarRoot;

    //info Card
    ConstraintLayout infoCard;
    TextView id;
    TextView model;
    TextView speed;
    TextView state;

    //Modes
    ImageButton mapMode;
    int SelectedMapMode = 1;

    ConstraintLayout car1;
    ConstraintLayout car2;
    ConstraintLayout car3;
    ConstraintLayout car4;
    ConstraintLayout car5;

    int chosenCar = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Assigner();

        sideBarRoot.setVisibility(View.INVISIBLE);
        infoCard.setVisibility(View.INVISIBLE);

        sideBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SideBarBtnTapped();
            }
        });

        mapview = findViewById(R.id.mapView);
        mapview.getMapAsync(this);
        mapview.onCreate(savedInstanceState);
    }

    //Assigner
    private void Assigner(){
        sideBarBtn = findViewById(R.id.sideBarBtn);
        sideBarRoot = findViewById(R.id.sideBarRoot);
        infoCard = findViewById(R.id.infoCard);

        car1 = findViewById(R.id.car1_root);
        car2 = findViewById(R.id.car2_root);
        car3 = findViewById(R.id.car3_root);
        car4 = findViewById(R.id.car4_root);
        car5 = findViewById(R.id.car5_root);

        //InfoCard
        id = findViewById(R.id.ic_ID);
        model = findViewById(R.id.ic_Model);
        speed = findViewById(R.id.ic_Speed);
        state = findViewById(R.id.ic_State);

        mapMode = findViewById(R.id.btn_mapMode);
    }


    //Clicker
    private void SideBarBtnTapped(){
        if (sideBarRoot.getVisibility() == View.VISIBLE){
            sideBarRoot.setVisibility(View.INVISIBLE);
            infoCard.setVisibility(View.VISIBLE);
        } else {
            sideBarRoot.setVisibility(View.VISIBLE);
            infoCard.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //Set Map
        mGoogleMap = googleMap;

        fillInPath();

        //initial Zoom
        LatLng origin = new LatLng(33.313786, 44.439598);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 10f));

        //Icon Setting
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car_marker);
        Bitmap b = bitmapdraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 60, false);

        //Map Mode
        mapMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Boolean CurrentMapMode = mGoogleMap.isTrafficEnabled();
//                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                SelectedMapMode += 1;

                if (SelectedMapMode == 1){
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (SelectedMapMode == 2){
                    mGoogleMap.setTrafficEnabled(true);
                } else if (SelectedMapMode == 3){
                    mGoogleMap.setTrafficEnabled(false);
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (SelectedMapMode == 4) {
                    mGoogleMap.setTrafficEnabled(true);
                } else {
                    SelectedMapMode = 1;
                    mGoogleMap.setTrafficEnabled(false);
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }



            }
        });

        //Cars
        car1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chosenCar != 1){
                    chosenCar = 1;
                    startCar1();
                }

                sideBarRoot.setVisibility(View.INVISIBLE);
                infoCard.setVisibility(View.VISIBLE);
            }
        });

        car2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenCar = 2;
                startCar2();
                sideBarRoot.setVisibility(View.INVISIBLE);
                infoCard.setVisibility(View.VISIBLE);

            }
        });

        car3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenCar = 3;
                startCar3();
                sideBarRoot.setVisibility(View.INVISIBLE);
                infoCard.setVisibility(View.VISIBLE);
            }
        });

        car4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenCar = 4;
                startCar4();
                sideBarRoot.setVisibility(View.INVISIBLE);
                infoCard.setVisibility(View.VISIBLE);
            }
        });

        car5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenCar = 5;
                startCar5();
                sideBarRoot.setVisibility(View.INVISIBLE);
                infoCard.setVisibility(View.VISIBLE);
            }
        });

    }

    //Start Car 1
    private void startCar1(){


        remainingPath.addAll(path);
        passedPath.clear();

        LatLng origin = new LatLng(33.313786, 44.439598);
        LatLng dest = new LatLng(33.324632, 44.438228);
        //Camera Movement

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 25f));

        MarkerOptions markerCar = new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker));
        mGoogleMap.addMarker(markerCar);
        //Test Delay
        int loopCount = path.size(); // Number of iterations in the loop
        long delayMillis = 3000; // Delay between each step in milliseconds

        for (int i = 0; i < loopCount; i++) {

            final int currentStep = i;
            runnable = new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.clear();

                    mGoogleMap.addMarker(new MarkerOptions().position(origin));
                    mGoogleMap.addMarker(new MarkerOptions().position(dest));

                    remainingPath.remove(path.get(currentStep));
                    passedPath.add(path.get(currentStep));

//                    PolylineOptions remainingLine = new PolylineOptions().addAll(remainingPath).color(Color.argb(255, 0, 255, 0));
//                    remainingLine.width(17f);
//                    PolylineOptions passedLine = new PolylineOptions().addAll(passedPath).color(Color.argb(255, 0, 0, 255));
//                    passedLine.width(17f);
                    MarkerOptions carMarker = new MarkerOptions().position(path.get(currentStep)).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker));
                    carMarker.flat(true);

                    if (currentStep < 44) {
                        carMarker.rotation(220f);
                    } else {
                        carMarker.rotation(310f);
                    }

//                    mGoogleMap.addPolyline(remainingLine);
//                    mGoogleMap.addPolyline(passedLine);
                    mGoogleMap.addMarker(carMarker);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(path.get(currentStep), 17f));

                }
            };
            handler.postDelayed(runnable, i * delayMillis);
        }

        setInfoCard("BGD A34040", "KIA Bongo 3", "60 Km/h", "Active", 1);
    }

    //Start Car 2
    private void startCar2(){
        mGoogleMap.clear();
        handler.removeCallbacks(runnable);

        LatLng origin = new LatLng(33.315878, 44.427310);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f));
        mGoogleMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));

        setInfoCard("BGD A34042", "White Hyundai Elentra", "0 Km/h", "Stopped", 2);

    }

    //Start Car 3
    private void startCar3(){
        mGoogleMap.clear();
        handler.removeCallbacks(runnable);

        LatLng origin = new LatLng(33.320048, 44.413625);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f));
        mGoogleMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));

        setInfoCard("BGD A34041", "Nissan Sunny", "0 Km/h", "Running, Parking", 3);
    }

    //Start Car 4
    private void startCar4(){
        mGoogleMap.clear();
        handler.removeCallbacks(runnable);

        LatLng origin = new LatLng(33.358482, 44.418190);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f));
        mGoogleMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));

        setInfoCard("BGD A34043", "Nissan Sunny", "0 Km/h", "Stopped", 2);
    }

    //Start Car 5
    private void startCar5(){
        mGoogleMap.clear();
        handler.removeCallbacks(runnable);

        LatLng origin = new LatLng(33.393225, 44.402411);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f));
        mGoogleMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));

        setInfoCard("BGD A34043", "Nissan Sunny", "0 Km/h", "Stopped", 2);
    }



    private void setInfoCard(String idS, String modelS, String SpeedS, String stateS, int colorS){
        id.setText(idS);
        model.setText(modelS);
        speed.setText(SpeedS);
        state.setText(stateS);

        if (colorS == 1){
        state.setTextColor(Color.rgb(0, 200, 0));
        } else if (colorS == 2){
            state.setTextColor(Color.rgb(200, 0, 0));
        } else if (colorS == 3){
            state.setTextColor(Color.rgb(254, 190, 0));
        }
    }

    private void fillInPath(){
        path.add(new LatLng(33.313982, 44.439219));
        path.add(new LatLng(33.314064, 44.439071));
        path.add(new LatLng(33.314296, 44.438615));
        path.add(new LatLng(33.314501, 44.438255));
        path.add(new LatLng(33.314501, 44.438255));
        path.add(new LatLng(33.314501, 44.438255));
        path.add(new LatLng(33.314837, 44.437771));
        path.add(new LatLng(33.314968, 44.437619));
        path.add(new LatLng(33.315163, 44.437380));
        path.add(new LatLng(33.315286, 44.437232));
        path.add(new LatLng(33.315409, 44.437085));
        path.add(new LatLng(33.315532, 44.436937));
        path.add(new LatLng(33.315655, 44.436789));
        path.add(new LatLng(33.315778, 44.436642));
        path.add(new LatLng(33.315901, 44.436494));
        path.add(new LatLng(33.316024, 44.436346));
        path.add(new LatLng(33.316147, 44.436199));
        path.add(new LatLng(33.316270, 44.436051));
        path.add(new LatLng(33.316393, 44.435903));
        path.add(new LatLng(33.316516, 44.435756));
        path.add(new LatLng(33.316639, 44.435608));
        path.add(new LatLng(33.316762, 44.435460));
        path.add(new LatLng(33.316885, 44.435313));
        path.add(new LatLng(33.317008, 44.435165));
        path.add(new LatLng(33.317131, 44.435017));
        path.add(new LatLng(33.317254, 44.434870));
        path.add(new LatLng(33.317377, 44.434722));
        path.add(new LatLng(33.317500, 44.434574));
        path.add(new LatLng(33.317623, 44.434427));
        path.add(new LatLng(33.317746, 44.434279));
        path.add(new LatLng(33.317869, 44.434131));
        path.add(new LatLng(33.317992, 44.433984));
        path.add(new LatLng(33.318115, 44.433836));
        path.add(new LatLng(33.318238, 44.433688));
        path.add(new LatLng(33.318361, 44.433541));
        path.add(new LatLng(33.318484, 44.433393));
        path.add(new LatLng(33.318607, 44.433245));
        path.add(new LatLng(33.318730, 44.433098));
        path.add(new LatLng(33.318853, 44.432950));
        path.add(new LatLng(33.318976, 44.432802));
        path.add(new LatLng(33.319099, 44.432655));
        path.add(new LatLng(33.319222, 44.432507));
        path.add(new LatLng(33.319345, 44.432359));
        path.add(new LatLng(33.319468, 44.432212));
        path.add(new LatLng(33.319618, 44.432440));
        path.add(new LatLng(33.319761, 44.432587));
        path.add(new LatLng(33.319905, 44.432734));
        path.add(new LatLng(33.320048, 44.432881));
        path.add(new LatLng(33.320191, 44.433028));
        path.add(new LatLng(33.320334, 44.433175));
        path.add(new LatLng(33.320477, 44.433322));
        path.add(new LatLng(33.320620, 44.433469));
        path.add(new LatLng(33.320763, 44.433616));
        path.add(new LatLng(33.320906, 44.433763));
        path.add(new LatLng(33.321049, 44.433910));
        path.add(new LatLng(33.321192, 44.434057));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }

}
