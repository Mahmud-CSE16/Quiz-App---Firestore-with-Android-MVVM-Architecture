package com.mahmud.quizapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mahmud.quizapp.R;
import com.mahmud.quizapp.model.QuizListModel;
import com.mahmud.quizapp.viewmodel.QuizListViewModel;

import java.util.List;

import static android.content.ContentValues.TAG;

public class DetailsFragment extends Fragment {

    private NavController navController;
    private QuizListViewModel quizListViewModel;
    private int position;

    private TextView detailsTitle, detailsDesc, detailsDiff, detailsQuestions;
    private ImageView detailsCoverImage;
    private Button detailsStartBtn;
    private String quizId;
    private int totalQuestionToAnswer;
    private String quizName;
    private TextView detailsScore;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        detailsCoverImage = view.findViewById(R.id.details_image);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDesc = view.findViewById(R.id.details_desc);
        detailsDiff = view.findViewById(R.id.details_difficulty_text);
        detailsQuestions = view.findViewById(R.id.details_questions_text);
        detailsStartBtn = view.findViewById(R.id.details_start_btn);
        detailsScore = view.findViewById(R.id.details_score_text);

        detailsStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: "+quizId);
                Log.d(TAG, "onClick: "+totalQuestionToAnswer);

                DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
                action.setTotalQuestions(totalQuestionToAnswer);
                action.setQuizId(quizId);
                action.setQuizName(quizName);
                navController.navigate(action);
            }
        });

        //Load Previous Results
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {

                Glide.with(getContext())
                        .load(quizListModels.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(detailsCoverImage);

                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsDiff.setText(quizListModels.get(position).getLevel());
                detailsQuestions.setText( String.valueOf(quizListModels.get(position).getQuestions()));

                //Assign Value to quizId
                totalQuestionToAnswer = quizListModels.get(position).getQuestions();
                quizId =  quizListModels.get(position).getId();
                quizName = quizListModels.get(position).getName();

                //Load Results Data
                loadResultsData();

            }
        });
    }


    private void loadResultsData() {
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document != null && document.exists()){
                        //Get Result
                        Long correct = document.getLong("correct");
                        Long wrong = document.getLong("wrong");
                        Long missed = document.getLong("unanswered");

                        //Calculate Progress
                        Long total = correct + wrong + missed;
                        Long percent = (correct*100)/total;

                        detailsScore.setText(percent + "%");
                    } else {
                        //Document Doesn't Exist, and result should stay N/A
                    }
                }
            }
        });
    }

}