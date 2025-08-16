package com.smelet01.joannamap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Future;

public class DetailedPhotoFragment extends Fragment {

    ImageView photoImageView;

    TextView dateTV, latitudeTV, longitudeTV, orientationTV, deletePhotoTV, addressTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detailed_photo, container, false);
        dateTV = view.findViewById(R.id.photos_date_taken_tv);
        latitudeTV = view.findViewById(R.id.photos_latitude_tv);
        longitudeTV = view.findViewById(R.id.photos_longitude_tv);
        orientationTV = view.findViewById(R.id.photos_orientation_tv);
        photoImageView = view.findViewById(R.id.photo_image_view);
        deletePhotoTV = view.findViewById(R.id.delete_photo_tv);
        addressTV = view.findViewById(R.id.address_TV);


        Bundle bundle = getArguments();
        if (bundle != null) {
            PhotoInfoDB photoInfo = (PhotoInfoDB) bundle.getSerializable("photoInfo");
            Uri fileUri = Uri.parse(photoInfo.uri);
            String filePath = fileUri.getPath();

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            bitmap = adjustImageOrientation(bitmap, filePath);
            photoImageView.setImageBitmap(bitmap);

            try {
                ExifInterface exifInterface = new ExifInterface(Objects.requireNonNull(Uri.parse(photoInfo.uri).getPath()));
                String timestamp = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                Date date = dateFormat.parse(timestamp);
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDate = displayFormat.format(date);
                dateTV.setText(formattedDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String address = Objects.requireNonNull(AddressHelp.getAddressFromLocation(requireContext(), photoInfo.latitude, photoInfo.longitude));
            addressTV.setText(address);
            latitudeTV.setText(photoInfo.latitude + "");
            longitudeTV.setText(photoInfo.longitude + "");
            orientationTV.setText(photoInfo.orientation);

            deletePhotoTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File imageFile = new File(filePath);
                    if (imageFile.exists()) {

                        boolean delete = imageFile.delete();
                        if (delete) {
                            Toast.makeText(requireContext(), "Photo Deleted Successfully", Toast.LENGTH_SHORT).show();
                            delete = deletePhotoFromDB(photoInfo);
                            if (delete) {
                                Toast.makeText(requireContext(), "Photo Deleted from DB Successfully", Toast.LENGTH_SHORT).show();
                                bundle.putBoolean("deleted", true);
                                NavController navController = NavHostFragment.findNavController(DetailedPhotoFragment.this);
                                navController.navigate(R.id.action_PhotoFragment_to_MapFragment, bundle);


                            } else {
                                Toast.makeText(requireContext(), "Photo Failed to be Deleted from DB", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to Delete Photo", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "File does not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        return view;
    }

    private boolean deletePhotoFromDB(PhotoInfoDB photoInfo) {
        AppStorage appStorage = Room.databaseBuilder(requireContext(), AppStorage.class, "PhotoInfoDB").build();
        PhotoInfoDao dao = appStorage.photoInfoDao();
        DatabaseManager db = new DatabaseManager();
        db.deletePhotoInfoInBackground(dao, photoInfo);

        Future<List<PhotoInfoDB>> future = db.getPhotoInfoByUriAsync(dao, photoInfo.uri);
        try {
            List<PhotoInfoDB> photoInfoList = future.get();
            if (photoInfoList == null || photoInfoList.size() == 0)
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
