package com.smelet01.joannamap;

import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;


import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;


import java.util.List;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;


public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
    private GoogleMap googleMap;

    private ClusterManager<PhotoInfoDB> clusterManager;

    private MapViewModel viewModel;
    private boolean isCameraSet;

    FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allPermissionsGranted = true;
                for (Boolean permission : permissions.values()) {
                    if (!permission) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (!allPermissionsGranted) {
                    Toast.makeText(requireContext(), "Permissions not granted.", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            });
    private static final String[] REQUIRED_PERMISSIONS = {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"

    };

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            MapFragment.this.googleMap = googleMap;

            requestLocationUpdate();
            getCurrentLocationAndMoveCamera();

            List<PhotoInfoDB> capturedPhotoInfos = getPhotoInfos(); // This will now filter based on orientation

            if (capturedPhotoInfos != null && capturedPhotoInfos.size() != 0) {
                clusterManager = new ClusterManager<>(requireContext(), googleMap);
                googleMap.setOnCameraIdleListener(clusterManager);
                googleMap.setOnMarkerClickListener(clusterManager);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                Cluster clusterRenderer = new Cluster(requireContext(), googleMap, clusterManager);
                clusterManager.setRenderer(clusterRenderer);

                for (PhotoInfoDB photoInfo : capturedPhotoInfos) {
                    if (photoInfo != null && photoInfo.latitude != -1) {
                        clusterManager.addItem(photoInfo);
                        builder.include(new LatLng(photoInfo.latitude, photoInfo.longitude));
                    }
                }
                if (!isCameraSet) {
                    LatLngBounds bounds = builder.build();
                    int padding = 100;
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }

            if (clusterManager != null) {
                clusterManager.setOnClusterItemClickListener(item -> {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("cameraPosition", googleMap.getCameraPosition());
                    viewModel.setMapState(bundle);
                    bundle.remove("cameraPosition");
                    bundle.putSerializable("photoInfo", item);

                    NavHostFragment.findNavController(MapFragment.this)
                            .navigate(R.id.action_MapFragment_to_PhotoFragment, bundle);

                    return true;
                });

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        viewModel.getMapState().observe(getViewLifecycleOwner(), bundle -> {
            if (bundle != null && !bundle.getBoolean("deleted")) {
                CameraPosition cameraPosition = bundle.getParcelable("cameraPosition");
                if (cameraPosition != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    isCameraSet = true;
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (googleMap != null) {
            googleMap.setOnMarkerClickListener(null); // Remove the listener
        }
    }

    private List<PhotoInfoDB> getPhotoInfos() {
        AppStorage appDatabase = Room.databaseBuilder(requireContext(), AppStorage.class, "PhotoInfoDB").build();
        PhotoInfoDao dao = appDatabase.photoInfoDao();
        DatabaseManager db = new DatabaseManager();

        String currentOrientation = getCurrentOrientation();
        CompletableFuture<List<PhotoInfoDB>> future = db.loadByOrientationAsync(dao, currentOrientation);

        try {
            List<PhotoInfoDB> photoInfoList = future.get();
            for (PhotoInfoDB photoInfoDB : photoInfoList) {
                Log.d("MapGetPhotoInfos", "item: " + photoInfoDB.toString());
            }
            return photoInfoList;
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Button capturePhotoButton = view.findViewById(R.id.take_photo_button);
        capturePhotoButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_MapFragment_to_CameraFragment);
        });
        return view;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public boolean onMarkerClick(Marker marker) {
        PhotoInfoDB photoInfo = (PhotoInfoDB) marker.getTag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("photoInfo", photoInfo); // Assuming PhotoInfo is Serializable
        if (photoInfo != null) {
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_MapFragment_to_PhotoFragment, bundle); // Replace R.id.nav_host_fragment with your NavHostFragment ID
        }
        return true;
    }


    private void requestLocationUpdate() {
        Log.d("requestLocationUpdate", "Entered");
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            Log.d("requestLocationUpdate", "Got Location Successfully");

                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("MapFragment", "Location Found Successfully. Latitude: " + latitude + "   Longitude: " + longitude);
                            LatLng myLatLng = new LatLng(latitude, longitude);
                            Drawable drawable;
                            if (isAdded()) {
                                drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_location);
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(drawableToBitmap(drawable));
                                if (googleMap != null) {
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(myLatLng)
                                            .icon(icon)
                                            .zIndex(1)
                                            .title("My Location"));
                                }
                            }
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                },
                Looper.getMainLooper()
        );

    }


    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private String getCurrentOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        return (orientation == Configuration.ORIENTATION_PORTRAIT) ? "Portrait" : "Landscape";
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        List<PhotoInfoDB> capturedPhotoInfos = getPhotoInfos();
        clusterManager.clearItems();
        for (PhotoInfoDB photoInfo : capturedPhotoInfos) {
            if (photoInfo != null && photoInfo.latitude != -1) {
                clusterManager.addItem(photoInfo);
            }
        }
        clusterManager.cluster(); // Refresh the cluster
    }

    private void getCurrentLocationAndMoveCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Zoom level can be adjusted
                } else {
                    Log.d("MapFragment", "Current location is null");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("MapFragment", "Failed to get current location: " + e.getMessage());
            });
    }

}
