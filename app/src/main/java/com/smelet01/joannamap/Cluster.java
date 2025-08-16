package com.smelet01.joannamap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import android.media.ExifInterface;

public class Cluster extends DefaultClusterRenderer<PhotoInfoDB> {
    private final Context context;

    public Cluster(Context context, GoogleMap map, ClusterManager<PhotoInfoDB> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(PhotoInfoDB item, MarkerOptions markerOptions) {
        // Set a custom icon based on the item
        Bitmap bitmap = getAdjustedBitmap(item.uri);
        int customWidth = 256;
        int customHeight = 256;
        Bitmap customSizedBitmap = Bitmap.createScaledBitmap(bitmap, customWidth, customHeight, false);

        // Create a BitmapDescriptor from the custom-sized Bitmap
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(customSizedBitmap);
        markerOptions.icon(icon);
    }

    // Method to get the adjusted bitmap based on EXIF orientation
    private Bitmap getAdjustedBitmap(String uriString) {
        Uri uri = Uri.parse(uriString);
        String filePath = uri.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return adjustImageOrientation(bitmap, filePath);
    }

    // Method to adjust the image orientation based on EXIF data
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
                    return bitmap; // No rotation needed
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap; // Return original bitmap if there's an error
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
