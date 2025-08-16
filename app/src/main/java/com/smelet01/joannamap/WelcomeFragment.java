package com.smelet01.joannamap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        TextView appNameTextView = view.findViewById(R.id.appNameTextView);
        TextView welcomeMessageTextView = view.findViewById(R.id.welcomeMessageTextView);
        Button capturePhotoButton = view.findViewById(R.id.capturePhotoButton);
        Button navigateToMapButton = view.findViewById(R.id.navigateToMapButton);

        welcomeMessageTextView.setText(R.string.welcome_message);
        appNameTextView.setText(R.string.app_name);

        capturePhotoButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_WelcomeFragment_to_CameraFragment);
        });

        navigateToMapButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_WelcomeFragment_to_MapFragment);
        });

        return view;
    }
}