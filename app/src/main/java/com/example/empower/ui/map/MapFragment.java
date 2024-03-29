package com.example.empower.ui.map;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.example.empower.MainActivity3;
import com.example.empower.R;
import com.example.empower.api.DataParser;
import com.example.empower.database.LikedVenue;
import com.example.empower.entity.ActiveHour;
import com.example.empower.entity.LocationAddressPair;
import com.example.empower.entity.Step;
import com.example.empower.entity.Venue;
import com.example.empower.ui.about.AboutViewModel;
import com.example.empower.ui.dialog.SearchDialogMapFragment;
import com.example.empower.ui.dialog.StepsDialogMapFragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hjq.toast.ToastUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.lxj.xpopup.XPopup;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * class name: MapFragment.java
 * function: Main aim of this fragment is to displayed disability supported sports venues on the map
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback {

    public static final int SEARCH_MAP_FRAGMENT = 1;

    // accessible route flag
    SharedPreferences accessibleFlag;
    private Boolean accessibleBoolean;


    // map search fab
    private FloatingActionButton fab;

    private ArrayList<Venue> combineLocationMapping1;


    private SearchDialogMapFragment dialogMapFragment;

    // root view of the map fragment
    private View root;

    private MapViewModel mapViewModel;
    private GoogleMap mapAPI;
    private SupportMapFragment mapFragment;

    // liked venue
    private ArrayList<LikedVenue> currentLikedVenues;
    private AboutViewModel aboutViewModel;


    // router between two locations
    // https://maps.googleapis.com/maps/api/directions/json?origin=-37.9134167,145.1316983&destination=-35.876859,147.044946&mode=transit&key=AIzaSyBenJ8IiMcO7vlKFYcZXb9WhKWuTQEJeo4
    MarkerOptions currentLocationMarker, destinationMarker;
    private Polyline currentPolyLine;
    private String url;
    private String directionMode;
    private List<HashMap<String, String>> routeSteps;

    // stop information of a route
    private List<HashMap<String, LatLng>> stopsLatlng;


    // arrayList for all all sports venus read from teh cloud firebase data file
    private ArrayList<Venue> sportsVenueList = new ArrayList<>();
    private Marker stopMaker;


    // visual component of the map fragment, include search input, search box, and a progressBar to display progress
    private EditText mapPostcodeEditText;
    private Button searchNearbyButton;
    private ProgressBar mapProgressBar;

    // spinner and related data array
    private Spinner sportSpinner;
    private List<String> sportSpinnerDataList;
    private ArrayAdapter<String> sportSpinnerAdapter;


    private static final String TAG = "searchApp";
    static String result = null;
    Integer responseCode = null;
    String responseMessage = "";


    //user current location
    private Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private static final int STREE_VIEW_FRAGMENT = 1;

    private ActiveHour active_hours = new ActiveHour();

    //FireStore database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    // float bar
    private LinearLayout floatBarLayout;
    private LikeButton float_heartButton;
    private TextView float_selectedAddress;
    private ImageView float_streetViewButton;
    private ImageView float_routerButton;


    // initialize the mapFragment
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        // accessible flag
        accessibleFlag = getActivity().getSharedPreferences("switchFlag", Context.MODE_PRIVATE);
        accessibleBoolean = accessibleFlag.getBoolean("flag", false);
        System.out.println(accessibleBoolean.toString());


        // get like venue list from live data in about view model

        aboutViewModel =
                ViewModelProviders.of(this).get(AboutViewModel.class);
        aboutViewModel.initalizeVars(getActivity().getApplication());


        // get LikedVenue object from
        aboutViewModel.getAllLikedVenues().observe(this, new Observer<List<LikedVenue>>() {
            @Override
            public void onChanged(List<LikedVenue> likedVenues) {
                currentLikedVenues = new ArrayList<>();
                currentLikedVenues = (ArrayList<LikedVenue>) likedVenues;
            }

        });


//        MainActivity2 mainActivity = (MainActivity2) getActivity();

        MainActivity3 mainActivity = (MainActivity3) getActivity();

        currentLocation = new Location("current location");
        currentLocation.setLatitude(mainActivity.getCurrentLatLngFromMain().latitude);
        currentLocation.setLongitude(mainActivity.getCurrentLatLngFromMain().longitude);


        root = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapAPI);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        // router between two locations display
        currentLocationMarker = new MarkerOptions().position(
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

        destinationMarker = new MarkerOptions().position(new LatLng(-37.799446, 144.919102)).title("Destination Location");

        url = getUrl(currentLocationMarker.getPosition(), destinationMarker.getPosition(), "transit");


        // float bar configuration
        floatBarLayout = root.findViewById(R.id.float_bar);
        float_heartButton = root.findViewById(R.id.heart_button);
        float_streetViewButton = root.findViewById(R.id.street_view_imageButton);
        float_routerButton = root.findViewById(R.id.router_planner_imageButton);

        //floatBarLayout.setVisibility(View.GONE);


        fab = root.findViewById(R.id.map_search_fab);


        //createSportsVenueList();
        getSportsListFromFireStore();


        mapProgressBar = root.findViewById(R.id.map_search_progressBar);

        dialogMapFragment = new SearchDialogMapFragment();
        dialogMapFragment.setTargetFragment(this, SEARCH_MAP_FRAGMENT);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatBarLayout.setVisibility(View.INVISIBLE);
                dialogMapFragment.show(getFragmentManager().beginTransaction(), "SearchDialogMapFragment");
                //dialogMapFragment.setCancelable(true);
                mapProgressBar.setVisibility(View.VISIBLE);
            }
        });


        System.out.println("this is a test");
        return root;
    }


    // default method for map is created at the beginning
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mapAPI = googleMap;


        MapsInitializer.initialize(getContext());
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(getActivity(), 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });

        getCurrentLocation();
        // add user current location on map with location info from the MainActivity


        LatLng currentLocationMarkerOnMap = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions currentLocationMarker = new MarkerOptions().position(currentLocationMarkerOnMap)
                .title("Your current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mapAPI.addMarker(currentLocationMarker).showInfoWindow();


        // set the camera position of application when oping the map on ready


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocationMarkerOnMap).zoom(12).build();

        mapAPI.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

    }


    private class AsyncFindVenueNearby extends AsyncTask<ArrayList<Venue>, Void, ArrayList<LocationAddressPair>> {

        @Override
        protected ArrayList<LocationAddressPair> doInBackground(ArrayList<Venue>... arrayLists) {
            // input parameter of the sports venues array list
            ArrayList<Venue> sportsVenueArrayList = arrayLists[0];

            ArrayList<LocationAddressPair> combineLocationMapping = new ArrayList<>();

            if (sportsVenueArrayList.size() > 0) {
                // Use index number to mapping of real address and latitude-longitude address
                for (Venue tempSportsVenue : sportsVenueArrayList) {

                    LatLng tempSportsVenueLatlng = new LatLng(Double.parseDouble(tempSportsVenue.getLatitude()), Double.parseDouble(tempSportsVenue.getLongitude()));

                    if (tempSportsVenueLatlng != null) {
                        combineLocationMapping.add(new LocationAddressPair(tempSportsVenueLatlng, tempSportsVenue));
                    }
                }
            }

            return combineLocationMapping;
        }

        // after getting combineLocationMapping of matched sports venues and their latitude and longitude info,
        // place markers with red color on the map, also the address info can be added with snippet of the marker
        @Override
        protected void onPostExecute(ArrayList<LocationAddressPair> combineLocationMapping) {
            getCurrentLocation();
            mapAPI.clear();

            if (combineLocationMapping.size() != 0) {

                LatLng currentLocationMarkerOnMap = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                Location currentUserLocation = new Location("currentUserLocation");
                currentUserLocation.setLatitude(currentLocation.getLatitude());
                currentUserLocation.setLongitude(currentLocation.getLongitude());


                LatLng closedVenueLatlng = combineLocationMapping.get(0).getLatLngInfo();
                LocationAddressPair closedLocationAddressPair = combineLocationMapping.get(0);
                Location tempLocation = new Location("tempLocation");
                tempLocation.setLatitude(closedVenueLatlng.latitude);
                tempLocation.setLongitude(closedVenueLatlng.longitude);

                // set the first one as initial minimal distance
                double minimalDistance = currentUserLocation.distanceTo(tempLocation);

                for (LocationAddressPair tempLocationAddressPair : combineLocationMapping) {
                    tempLocation = new Location("tempLocation");
                    tempLocation.setLatitude(tempLocationAddressPair.getLatLngInfo().latitude);
                    tempLocation.setLongitude(tempLocationAddressPair.getLatLngInfo().longitude);

                    double tempDistance = currentUserLocation.distanceTo(tempLocation);

                    if (tempDistance < minimalDistance) {
                        minimalDistance = tempDistance;
                        closedVenueLatlng = tempLocationAddressPair.getLatLngInfo();
                        closedLocationAddressPair = tempLocationAddressPair;
                    }

                }
                mapAPI.addMarker(new MarkerOptions().position(closedVenueLatlng)
                        .title(closedLocationAddressPair.getSportsVenueInfo().getName())
                        .snippet(closedLocationAddressPair.getSportsVenueInfo().getAddress()
                                + " " + closedLocationAddressPair.getSportsVenueInfo().getPostcode()));

                mapAPI.addMarker(new MarkerOptions().position(currentLocationMarkerOnMap).title("Your current location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                CameraPosition newCameraPosition = new CameraPosition.Builder()
                        .target(currentLocationMarkerOnMap).zoom(10).build();


                mapProgressBar.setVisibility(View.VISIBLE);

                mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                mapProgressBar.setVisibility(View.GONE);
                Toast toast_success = Toast.makeText(getContext(), "Result found", Toast.LENGTH_SHORT);
                toast_success.show();
            }
        }
    }


    // put markers of selected sports venues on the google map
    private class AsyncAddMarker extends AsyncTask<ArrayList<Venue>, Void, ArrayList<LocationAddressPair>> {

        @Override
        protected ArrayList<LocationAddressPair> doInBackground(ArrayList<Venue>... arrayLists) {

            // input parameter of the sports venues array list
            ArrayList<Venue> sportsVenueArrayList = arrayLists[0];

            ArrayList<LocationAddressPair> combineLocationMapping = new ArrayList<>();

            if (sportsVenueArrayList.size() > 0) {
                // Use index number to mapping of real address and latitude-longitude address
                for (Venue tempSportsVenue : sportsVenueArrayList) {
                    if (tempSportsVenue.getLongitude() == null || tempSportsVenue.getLatitude() == null) {

                        continue;
                    }

                    LatLng tempSportsVenueLatlng = new LatLng(Double.parseDouble(tempSportsVenue.getLatitude()), Double.parseDouble(tempSportsVenue.getLongitude()));

                    if (tempSportsVenue.getLatitude() != null && tempSportsVenue.getLongitude() != null) {
                        combineLocationMapping.add(new LocationAddressPair(tempSportsVenueLatlng, tempSportsVenue));
                    }
                }
            }


            return combineLocationMapping;

        }

        // after getting combineLocationMapping of matched sports venues and their latitude and longitude info,
        // place markers with red color on the map, also the address info can be added with snippet of the marker
        @Override
        protected void onPostExecute(ArrayList<LocationAddressPair> combineLocationMapping) {
            super.onPostExecute(combineLocationMapping);

            if (combineLocationMapping.size() != 0) {
                mapAPI.clear();


                mapAPI.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        floatBarLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), "Marker selected", Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });

                mapAPI.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        url = getUrl(currentLocationMarker.getPosition(), marker.getPosition(), "transit");
                        // add router on the map with selected
                        new FetchURL().execute(url, "transit");
                        Toast.makeText(getActivity(), "Info window clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                // long click jump to street view fragment
                mapAPI.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        Toast.makeText(getActivity(), "Info window long clicked", Toast.LENGTH_SHORT).show();

                    }
                });


                for (LocationAddressPair tempLocationAddressPair : combineLocationMapping) {

                    LatLng tempSportsVenueLocation = tempLocationAddressPair.getLatLngInfo();
                    Venue tempSportsVenue = tempLocationAddressPair.getSportsVenueInfo();


                    mapAPI.addMarker(new MarkerOptions().position(tempSportsVenueLocation)
                            .title(tempSportsVenue.getName())
                            .snippet(tempSportsVenue.getAddress() + " " + tempSportsVenue.getPostcode()));

                }
                getCurrentLocation();
                LatLng currentLocationMarkerOnMap = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mapAPI.addMarker(new MarkerOptions().position(currentLocationMarkerOnMap).title("You current location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                CameraPosition newCameraPosition = new CameraPosition.Builder()
                        .target(currentLocationMarkerOnMap).zoom(10).build();


                mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                mapProgressBar.setVisibility(View.GONE);
                Toast toast_success = Toast.makeText(getContext(), "Result found", Toast.LENGTH_SHORT);
                toast_success.show();
            }
        }

    }


    // get location with lat & lng from the address
    public LatLng getLocationFromAddress(Context context, String venueAddress) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        LatLng resultLatLon = null;

        try {
            addresses = geocoder.getFromLocationName(venueAddress, 3);

            if (addresses == null) {
                return null;
            }

            Address location = addresses.get(0);
            if (location != null) {
                resultLatLon = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultLatLon;

    }


    // get the sports venues info from the Cloud FireStore NoSql database
    private void getSportsListFromFireStore() {
        db.collection("sportsVenues")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> tempSportsVenueMap = document.getData();
                                String venueID = Objects.requireNonNull(tempSportsVenueMap.get("venueID")).toString();
                                String type = Objects.requireNonNull(tempSportsVenueMap.get("type")).toString();
                                String name = Objects.requireNonNull(tempSportsVenueMap.get("name")).toString();
                                String address = Objects.requireNonNull(tempSportsVenueMap.get("address")).toString();
                                String suburb = Objects.requireNonNull(tempSportsVenueMap.get("suburb")).toString();
                                String postcode = Objects.requireNonNull(tempSportsVenueMap.get("postcode")).toString();
                                String businessCategory = Objects.requireNonNull(tempSportsVenueMap.get("businessCategory")).toString();
                                String lga = Objects.requireNonNull(tempSportsVenueMap.get("lga")).toString();
                                String latitude = Objects.requireNonNull(tempSportsVenueMap.get("latitude")).toString();
                                String longitude = Objects.requireNonNull(tempSportsVenueMap.get("longitude")).toString();

                                sportsVenueList.add(new Venue(venueID, type, name, address, suburb, postcode, businessCategory, lga, latitude, longitude));


                            }
                        } else {
                            Log.d(TAG, "Error getting sportsVenues documents: " + task.getException());
                        }
                    }
                });
    }

    // get current location from locationRequest, if permission is right, update user current location
    public void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity())
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(getActivity())
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            currentLocation = new Location("real current location");
                            currentLocation.setLatitude(latitude);
                            currentLocation.setLongitude(longitude);

                        }
                    }
                }, Looper.getMainLooper());


    }

    // get result from input URL with google direction API
    public class FetchURL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            // For storing data from web service
            String data = "";
            directionMode = strings[1];
            try {
                // Fetching the data from web service
                data = downloadUrl(strings[0]);
                Log.d("mylog", "Background task data " + data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Invokes the thread for parsing the JSON data
            new PointsParser().execute(s);
        }
    }


    // parser points information to polyline on map
    public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("mylog", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("mylog", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                routeSteps = parser.stepParse(jObject);
                stopsLatlng = parser.stopParse(jObject);


                Log.d("mylog", "Executing routes");
                Log.d("mylog", routes.toString());

            } catch (Exception e) {
                Log.d("mylog", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<Polyline> mmPolylines = new ArrayList<>();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);


                }

                // add stop marker on the map of selected route
                if (stopsLatlng != null) {
                    // add stop marker on map
                    AsyncAddStopMarkerOnMap(stopsLatlng);
                }


                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                if (directionMode.equalsIgnoreCase("transit")) {
                    lineOptions.width(20);
                    lineOptions.color(Color.BLUE);

                } else {
                    lineOptions.width(20);
                    lineOptions.color(Color.GREEN);
                    //lineOptions.color(Color.rgb(115,214,66));

                }
                Log.d("mylog", "onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (currentPolyLine != null) {
                    currentPolyLine.remove();
                }

                final ArrayList<LatLng> lastPoints = points;

                currentPolyLine = mapAPI.addPolyline(lineOptions);
                currentPolyLine.setClickable(false);


                DisplaySteps(routeSteps);


            } else {
                Log.d("mylog", "without Polylines drawn");
                Toast toast_error = Toast.makeText(getContext(), "No result find", Toast.LENGTH_SHORT);
                toast_error.show();

            }

        }
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void AsyncAddStopMarkerOnMap(List<HashMap<String, LatLng>> stopList) {

        if (stopMaker != null) {
            stopMaker.remove();
        }

        for (int i = 0; i < stopList.size(); i++) {


            HashMap<String, LatLng> stopInfo = stopList.get(i);
            Map.Entry<String, LatLng> entry = stopInfo.entrySet().iterator().next();
            String stopName = entry.getKey();
            LatLng stopLatlng = entry.getValue();
            MarkerOptions tempMakerOptions = new MarkerOptions();
            tempMakerOptions.position(stopLatlng);
            tempMakerOptions.title("Stop: " + stopName);


            tempMakerOptions.icon(BitmapDescriptorFactory.fromResource(R.raw.stop1));
            // tempMakerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            //Marker tempMaker = tempMakerOptions;

            stopMaker = mapAPI.addMarker(tempMakerOptions);
            stopMaker.setTag(0);


        }

    }

    // display step information in every route planner dialogue
    public void DisplaySteps(List<HashMap<String, String>> stepInfoList) {


        HashMap<String, String> resultSummary = stepInfoList.get(0);
        String startAddress = resultSummary.get("start_address");
        String endAddress = resultSummary.get("end_address");
        String totalDistance = resultSummary.get("total_distance");
        String duration = resultSummary.get("duration");


        ArrayList<Step> stepArrayList = new ArrayList<>();

        for (int i = 1; i < stepInfoList.size(); i++) {
            HashMap<String, String> stepsInfo = stepInfoList.get(i);
            Step tempStep = new Step();
            String distance = stepsInfo.get("distance");
            String travel_mode = stepsInfo.get("travel_mode");
            tempStep.setDistance(distance);
            tempStep.setTravelMode(travel_mode);
            if (travel_mode.equals("TRANSIT")) {
                String transitShortName = stepsInfo.get("short_name");
                String vehicleName = stepsInfo.get("vehicleName");
                tempStep.setShortName(transitShortName);
                tempStep.setVehicleName(vehicleName);
            }

            stepArrayList.add(tempStep);

        }


        Bundle bundle = new Bundle();
        bundle.putString("start_address", startAddress);
        bundle.putString("end_address", endAddress);
        bundle.putString("total_distance", totalDistance);
        bundle.putString("duration", duration);
        bundle.putParcelableArrayList("stepList", stepArrayList);
        bundle.putString("venue_name", "sample venue name");


        // display step info in step dialog
        StepsDialogMapFragment stepsDialogMapFragment = new StepsDialogMapFragment();
        stepsDialogMapFragment.setArguments(bundle);
        stepsDialogMapFragment.show(getFragmentManager(), "StepsDialogMapFragment");
        stepsDialogMapFragment.setCancelable(true);


    }


    // create dialog of the route step info
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        String mode;
        if (directionMode.equals("transit")) {
            // Mode
            mode = "mode=" + directionMode;
        } else {
            // Mode
            mode = "mode=transit" + "&transit_mode=train";
        }


        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.map_key);
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("mylog", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("mylog", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // get result from search filter dialogue
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent searchParameter) {
        switch (requestCode) {
            case SEARCH_MAP_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = searchParameter.getExtras();

                    String inputPostcode = bundle.getString("inputPostcode");
                    ArrayList<String> venueArrayList = bundle.getStringArrayList("venueArrayList");
                    ArrayList<String> sportArrayList = bundle.getStringArrayList("sportArrayList");
                    String nearbyResult = bundle.getString("nearbyResult");


                    combineLocationMapping1 = new ArrayList<>();
                    VenueFilter venueFilter = new VenueFilter();
                    combineLocationMapping1 = venueFilter.getVenueWithPostCode(inputPostcode, sportsVenueList);
                    combineLocationMapping1 = venueFilter.getVenueWithType(venueArrayList, combineLocationMapping1);
                    combineLocationMapping1 = venueFilter.getVenueWithSports(sportArrayList, combineLocationMapping1);
                    combineLocationMapping1 = venueFilter.getVenueNearby(nearbyResult, combineLocationMapping1, currentLocation);

                    if (combineLocationMapping1.size() == 0) {

                        ToastUtils.init(getActivity().getApplication());
                        ToastUtils.setView(R.layout.toast_map_result1_view);
                        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                        ToastUtils.show("No Result found");

                    }

                    // add mark result on the map
                    new AsyncAddMarkerOnMap().execute(combineLocationMapping1);


                } else if (resultCode == Activity.RESULT_CANCELED) {

                }
                break;
        }
    }


    // put markers of selected sports venues on the google map
    @SuppressLint("StaticFieldLeak")
    private class AsyncAddMarkerOnMap extends AsyncTask<ArrayList<Venue>, Void, ArrayList<Venue>> {


        @Override
        protected ArrayList<Venue> doInBackground(ArrayList<Venue>... arrayLists) {

            ArrayList<Venue> venueArrayList = arrayLists[0];
            return venueArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Venue> arrayLists) {
            super.onPostExecute(arrayLists);
            // input parameter of the sports venues array list
            ArrayList<Venue> sportsVenueArrayList = arrayLists;

            mapAPI.clear();


            if (!(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                mapAPI.setMyLocationEnabled(true);
            }

            if (sportsVenueArrayList.size() != 0) {
                mapAPI.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        //Toast.makeText(getActivity(), "Info window clicked", Toast.LENGTH_SHORT).show();
                        floatBarLayout.setVisibility(View.INVISIBLE);
                        showStreetViewDialog(marker.getPosition(), marker.getTitle());
                    }
                });


                // long click jump to street view fragment
                mapAPI.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {

                        //Toast.makeText(getActivity(), "Info window long clicked", Toast.LENGTH_SHORT).show();

                    }
                });

                mapAPI.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (!marker.getTitle().equals("You current location")) {

                            VenueFilter venueFilter = new VenueFilter();

                            // get latlng info of the selected marker on the map
                            LatLng tempLatLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                            Venue foundVenue = venueFilter.findVenueByLatLng(tempLatLng, sportsVenueList);

                            // check if current select marker is a venue in dataset
                            if (foundVenue != null) {
                                floatBarLayout.setVisibility(View.VISIBLE);
                                //float_selectedAddress.setText(marker.getSnippet());

                                Bundle venueDeatilBundle = new Bundle();
                                venueDeatilBundle.putParcelable("selectLikedVenue", foundVenue);

                                Log.d(TAG, "Related venue found" );
                                //Toast.makeText(getActivity(), "Related venue found", Toast.LENGTH_SHORT).show();
                                if (venueFilter.checkIfCurrentVeneuLiked(foundVenue, currentLikedVenues)) {
                                    float_heartButton.setLiked(true);
                                    venueDeatilBundle.putBoolean("heartFlag", true);
                                } else {
                                    float_heartButton.setLiked(false);
                                    venueDeatilBundle.putBoolean("heartFlag", false);
                                }


                                float_heartButton.setOnLikeListener(new OnLikeListener() {
                                    @Override
                                    public void liked(LikeButton likeButton) {
                                        aboutViewModel.insert(new LikedVenue(foundVenue.getVenueID(), foundVenue.getName(), foundVenue.getPostcode()));
                                    }

                                    @Override
                                    public void unLiked(LikeButton likeButton) {
                                        aboutViewModel.delete(new LikedVenue(foundVenue.getVenueID(), foundVenue.getName(), foundVenue.getPostcode()));

                                        Toast.makeText(getActivity(), "Venue added", Toast.LENGTH_SHORT).show();
                                    }
                                });


                                venueDeatilBundle.putString("activeHour", active_hours.getAciveHours());


                                new XPopup.Builder(getContext())
                                        .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
                                        .enableDrag(true)
                                        .hasShadowBg(false)
                                        .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
//                        .isThreeDrag(true) //是否开启三阶拖拽，如果设置enableDrag(false)则无效
                                        .asCustom(new VenueDetailPopup(getContext(), venueDeatilBundle)/*.enableDrag(false)*/)
                                        .show();


                                float_streetViewButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showStreetViewDialog(tempLatLng, marker.getTitle());
                                    }
                                });

                                float_routerButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        // accessible flag check
                                        accessibleFlag = getActivity().getSharedPreferences("switchFlag", Context.MODE_PRIVATE);
                                        accessibleBoolean = accessibleFlag.getBoolean("flag", false);
                                        System.out.println(accessibleBoolean.toString());

                                        if (accessibleBoolean) {
                                            url = getUrl(currentLocationMarker.getPosition(), marker.getPosition(), "transit-accessible");
                                            new FetchURL().execute(url, "transit-accessible");
                                            Log.d(TAG, "Info window clicked" );
                                            //Toast.makeText(getActivity(), "Info window clicked", Toast.LENGTH_SHORT).show();
                                        } else {
                                            url = getUrl(currentLocationMarker.getPosition(), marker.getPosition(), "transit");
                                            // add router on the map with selected
                                            new FetchURL().execute(url, "transit");
                                            Log.d(TAG, "Info window clicked" );
                                            //Toast.makeText(getActivity(), "Info window clicked", Toast.LENGTH_SHORT).show();
                                        }


                                        // move map camera position to start point on the map
                                        getCurrentLocation();
                                        LatLng currentLocationMarkerOnMap = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                        mapAPI.addMarker(new MarkerOptions().position(currentLocationMarkerOnMap).title("You current location")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                                        CameraPosition newCameraPosition = new CameraPosition.Builder()
                                                .target(currentLocationMarkerOnMap).zoom(12).build();


                                        mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                                    }
                                });

                            } else {
                                floatBarLayout.setVisibility(View.INVISIBLE);
                            }

//                            Toast.makeText(getActivity(), "Marker selected", Toast.LENGTH_SHORT).show();

                        } else {
                            floatBarLayout.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Current location marker selected", Toast.LENGTH_SHORT).show();
                        }
                        return false;

                    }
                });

                mapAPI.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        floatBarLayout.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "Map clicked" );
                        //Toast.makeText(getActivity(), "Map clicked", Toast.LENGTH_SHORT).show();
                    }
                });


                for (Venue tempLocationAddressPair : sportsVenueArrayList) {

                    if (tempLocationAddressPair.getLatitude().equals("") || tempLocationAddressPair.getLongitude().equals("")) {
                        continue;
                    }


                    LatLng tempSportsVenueLocation = new LatLng(
                            Double.parseDouble(tempLocationAddressPair.getLatitude()),
                            Double.parseDouble(tempLocationAddressPair.getLongitude()));
                    Venue tempSportsVenue = tempLocationAddressPair;


                    if (tempSportsVenue.getType().equals("sport venue")) {
                        mapAPI.addMarker(new MarkerOptions().position(tempSportsVenueLocation)
                                .title(tempSportsVenue.getName()));
                    } else {
                        mapAPI.addMarker(new MarkerOptions().position(tempSportsVenueLocation)
                                .title(tempSportsVenue.getName()));
                    }

                    //}
                }
                getCurrentLocation();
                LatLng currentLocationMarkerOnMap = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mapAPI.addMarker(new MarkerOptions().position(currentLocationMarkerOnMap).title("You current location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                CameraPosition newCameraPosition = new CameraPosition.Builder()
                        .target(currentLocationMarkerOnMap).zoom(10).build();


                mapAPI.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                mapProgressBar.setVisibility(View.GONE);

                ToastUtils.init(getActivity().getApplication());
                ToastUtils.setView(R.layout.toast_map_result0_view);
                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
                ToastUtils.show("Result found");

                //TastyToast.makeText(getContext(), "Result found", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);

            }
            mapProgressBar.setVisibility(View.GONE);
        }
    }


    private void showStreetViewDialog(LatLng pos, String posName) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("streetview");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = StrretViewDialogFragment.newInstance(pos, posName);
        newFragment.show(ft, "streetview");
    }

    public static class StrretViewDialogFragment extends DialogFragment {

        LatLng latLng;

        static StrretViewDialogFragment newInstance(LatLng latLng, String posName) {
            StrretViewDialogFragment f = new StrretViewDialogFragment();

            Bundle args = new Bundle();
            args.putDouble("latitude", latLng.latitude);
            args.putDouble("longitude", latLng.longitude);
            args.putString("posName", posName);
            f.setArguments(args);

            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            latLng = new LatLng(getArguments().getDouble("latitude"),
                    getArguments().getDouble("longitude"));
            String positionName = getArguments().getString("posName");

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
            options.position(latLng);
            StreetViewPanoramaView streetViewPanoramaView =
                    new StreetViewPanoramaView(getActivity(), options);
            streetViewPanoramaView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
            streetViewPanoramaView.setPadding(5, 5, 5, 5);
            streetViewPanoramaView.onCreate(savedInstanceState);

            layout.addView(streetViewPanoramaView);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(positionName)
                    .setPositiveButton("Close", null)
                    .setView(layout)
                    .create();
        }
    }


}