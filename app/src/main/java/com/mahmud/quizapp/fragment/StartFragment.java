package com.mahmud.quizapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mahmud.quizapp.R;


public class StartFragment extends Fragment {

    private static final String TAG = "StartFragment";

    private ProgressBar startProgressBar;
    private TextView startFeedBackText;

    private NavController navController;

    private FirebaseAuth firebaseAuth;

    public StartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startProgressBar = view.findViewById(R.id.start_progress);
        startFeedBackText = view.findViewById(R.id.start_feedback);

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        startFeedBackText.setText("Checking User Account...");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser == null){
            // create user

            startFeedBackText.setText("Creating Account...");
            firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        startFeedBackText.setText("Account Created...");
                        navController.navigate(R.id.action_startFragment_to_listFragment);
                    }else{
                        Log.d(TAG, "onComplete: "+task.getException());
                    }
                }
            });
        }else{
            // navigate to home page
            startFeedBackText.setText("Logged in...");
            navController.navigate(R.id.action_startFragment_to_listFragment);
        }
    }
}