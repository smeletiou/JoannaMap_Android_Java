package com.smelet01.joannamap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private Camera camera;

    FusedLocationProviderClient fusedLocationClient;

    private static final String[] REQUIRED_PERMISSIONS = {
            "android.permission.CAMERA",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"

    };

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        previewView = view.findViewById(R.id.preview_view);
        Button captureButton = view.findViewById(R.id.capture_photo_btn);
        Button showMapsButton = view.findViewById(R.id.show_map_btn);

        showMapsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(CameraFragment.this)
                        .navigate(R.id.action_CameraFragment_to_MapFragment));


        captureButton.setOnClickListener(v -> capturePhoto());
        requestCameraPermission();

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isLocationEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        requestLocationUpdate();

        return view;
    }


    private void requestLocationUpdate() {
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
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                        }
                    }
                },
                Looper.getMainLooper()
        );
    }

    private void requestCameraPermission() {
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraFragment", "Error initializing cameraProviderFuture: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageCapture.Builder builder = new ImageCapture.Builder();
        imageCapture = builder.build();

        if (camera != null) {
            cameraProvider.unbindAll();
        }

        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void capturePhoto() {
        File internalStorageDir = requireContext().getFilesDir();
        if (internalStorageDir != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
            String currentTime = sdf.format(System.currentTimeMillis());
            String fileName = currentTime + ".jpg";
            File outputFile = new File(internalStorageDir, fileName);

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile)
                    .build();

            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(requireContext(), "Photo saved: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            Log.i("CameraFragment", "Photo saved: " + outputFile.getAbsolutePath());
                            Uri uri = Uri.fromFile(outputFile);
                            Bitmap photoBitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                            // Adjust the bitmap orientation based on EXIF data
                            photoBitmap = adjustImageOrientation(photoBitmap, outputFile.getAbsolutePath());
                            saveLocationAndOrientation(uri, photoBitmap);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e("CameraFragment", "Photo capture failed: " + exception.getMessage());
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "Internal storage directory not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap adjustImageOrientation(Bitmap bitmap, String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap; // Return original bitmap if there's an error
        }
    }

    private void saveLocationAndOrientation(Uri uri, Bitmap photoBitmap) {
        requestLocationUpdate();
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            Double latitude = (location != null) ? location.getLatitude() : null;
            Double longitude = (location != null) ? location.getLongitude() : null;
            String orientation = (photoBitmap.getWidth() > photoBitmap.getHeight()) ? "Landscape" : "Portrait";
            PhotoInfoDB photoInfo;
            Log.d("Capture Photo", "Uri = " + uri);
            Log.d("Capture Photo", "Latitude = " + latitude);
            Log.d("Capture Photo", "Longitude = " + longitude);
            Log.d("Capture Photo", "Orientation = " + orientation);

            if (latitude != null) {
                photoInfo = new PhotoInfoDB(uri.toString(), latitude, longitude, orientation);
                Log.d("CapturePhoto", "Photo info stored:" + photoInfo);
            } else {
                photoInfo = new PhotoInfoDB(uri.toString(), -1, -1, orientation);
            }
            savePhotoInfoToDB(photoInfo);
        });

    }

    private void savePhotoInfoToDB(PhotoInfoDB photoInfo) {
        AppStorage db = Room.databaseBuilder(requireContext(), AppStorage.class, "PhotoInfoDB").build();
        PhotoInfoDao dao = db.photoInfoDao();
        DatabaseManager dm = new DatabaseManager();
        Log.d("PhotoInfoDB", "Before insert");
        dm.insertPhotoInfoInBackground(dao,
                new PhotoInfoDB(photoInfo.uri, photoInfo.latitude, photoInfo.longitude, photoInfo.orientation));
        Log.d("PhotoInfoDB", "After insert");
    }


    @Override
    public void onResume() {
        super.onResume();
        if (camera == null) {
            startCamera();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    // Release camera resources
    private void releaseCamera() {
        if (cameraProviderFuture != null) {
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll(); // Release the camera
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("CameraFragment", "Error releasing camera: " + e.getMessage());
                }
            }, ContextCompat.getMainExecutor(requireContext()));
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
