package com.mahmud.quizapp.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mahmud.quizapp.R;
import com.mahmud.quizapp.model.QuestionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private static final String TAG = "QuizFragment";

    private TextView quizTitle,questionFeedback,questionText,questionTime,questionNumber;
    private Button optionOneBtn,optionTwoBtn,optionThreeBtn,nextBtn,closeBtn;
    private ProgressBar questionProgress;

    private FirebaseFirestore firebaseFirestore;

    private String quizName;
    private String quizId;

    private List<QuestionModel> allQuestionList = new ArrayList<>();
    private List<QuestionModel> questionsToAnswer = new ArrayList<>();
    private int totalQuestionsToAnswer = 5;
    private CountDownTimer countdownTimer;

    private boolean canAnswer = false;
    private int currentQuestion =0;

    private int correctAnswers=0;
    private int wrongAnswers=0;
    private int noteAnswered=0;


    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        //initialize
        firebaseFirestore = FirebaseFirestore.getInstance();

        //get quizId
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        Log.d(TAG, "onViewCreated: "+totalQuestionsToAnswer);
        Log.d(TAG, "onViewCreated: "+quizId);

        //UI initialize
        quizTitle = view.findViewById(R.id.quiz_title);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);
        questionText = view.findViewById(R.id.quiz_question);
        questionTime = view.findViewById(R.id.quiz_question_time);
        questionProgress = view.findViewById(R.id.quiz_question_progress);
        questionNumber = view.findViewById(R.id.quiz_question_number);

        //Get all Questions form firebase
        firebaseFirestore.collection("QuizList").document(quizId).collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            allQuestionList = task.getResult().toObjects(QuestionModel.class);
                            Log.d(TAG, "onComplete: "+allQuestionList.get(0).getQuestion());
                            pickQuestions();
                            loadUI();
                        }else{
                            //error
                            quizTitle.setText("Error Loading Data");
                        }
                    }
                });


        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    private void loadUI() {
        //Quiz Data loaded load the UI
        quizTitle.setText(quizName);
        questionText.setText("Load First Question");

        //Enable Options
        enableOptions();

        //Load First Question
        loadQuestion(1);
    }

    private void loadQuestion(int questionNum) {

        currentQuestion = questionNum;
        //Load Question Text
        questionNumber.setText(questionNum+"");
        questionText.setText(questionsToAnswer.get(questionNum-1).getQuestion());

        //Load Options
        optionOneBtn.setText(questionsToAnswer.get(questionNum-1).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(questionNum-1).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(questionNum-1).getOption_c());

        //Question Loaded, Set Can Answer
        canAnswer = true;

        //Load CountDown
        startTimer(questionNum);
    }

    private void startTimer(int questionNum) {
        //Set Timer text
        final long timeToAnswer = questionsToAnswer.get(questionNum-1).getTimer();
        questionTime.setText(String.valueOf(timeToAnswer));

        //show Timer ProgressBar
        questionProgress.setVisibility(View.VISIBLE);


        countdownTimer = new CountDownTimer(timeToAnswer*1000,10){

            @Override
            public void onTick(long l) {
                //Update Time
                questionTime.setText(l/1000+"");

                //progress in percent
                long percent = l/(timeToAnswer*10);
                questionProgress.setProgress((int)percent);
            }

            @Override
            public void onFinish() {
                //Time Up Cannot Answer

                canAnswer = false;
                countdownTimer.cancel();

                noteAnswered++;
                questionFeedback.setText("Time Up! No answer was submitted.");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary,null));

                showNextBtn();
            }
        };

        countdownTimer.start();
    }

    private void enableOptions() {
        //Show All Options Buttons
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        //enable option Buttons
        optionOneBtn.setClickable(true);
        optionTwoBtn.setClickable(true);
        optionThreeBtn.setClickable(true);

        //Hide Feedback and next Button
        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setClickable(false);
    }

    private void pickQuestions() {

        //Toast.makeText(getContext(),"PickQuestions",Toast.LENGTH_SHORT).show();
        Collections.shuffle(allQuestionList);

        if(allQuestionList.size()>=totalQuestionsToAnswer){
            questionsToAnswer.addAll(allQuestionList.subList(0,totalQuestionsToAnswer));
            Log.d(TAG, "pickQuestions true: "+questionsToAnswer);
        }else{
            questionsToAnswer.addAll(allQuestionList);
            Log.d(TAG, "pickQuestions false : "+questionsToAnswer);
            totalQuestionsToAnswer =allQuestionList.size();
        }

       /* for (QuestionModel question: questionsToAnswer) {
            Log.d(TAG, "pickQuestions: "+ question.getQuestion());
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.quiz_option_one:
                verifyAnswer(optionOneBtn);
                break;
            case R.id.quiz_option_two:
                verifyAnswer(optionTwoBtn);
                break;
            case R.id.quiz_option_three:
                verifyAnswer(optionThreeBtn);
                break;
            case R.id.quiz_next_btn:
                if(currentQuestion == totalQuestionsToAnswer){
                    submitResults();
                }else{
                    currentQuestion++;
                    Log.d(TAG, "onClick: "+currentQuestion);
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
                break;
        }
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct",correctAnswers);
        resultMap.put("wrong",wrongAnswers);
        resultMap.put("unanswered", noteAnswered);

        firebaseFirestore.collection("QuizList").document(quizId).collection("Results")
                .document(FirebaseAuth.getInstance().getUid()).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Go To Results Page
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizId(quizId);
                    navController.navigate(action);
                }else{
                    //Show Error
                    quizTitle.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_btn_bg,null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_btn_bg,null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_btn_bg,null));

        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.GONE);
        //nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectBtn) {
        //checkAnswer

        if(canAnswer){

            selectBtn.setTextColor(Color.WHITE);

            if(questionsToAnswer.get(currentQuestion-1).getAnswer().equals(String.valueOf(selectBtn.getText()))){
                //Correct Answer
                Log.d(TAG, "answerSelected: Correct Answer");
                correctAnswers++;
                selectBtn.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg,null));

                questionFeedback.setText("Correct");
                questionFeedback.setTextColor(Color.GREEN);
            }else{
                //Wrong Answer
                Log.d(TAG, "answerSelected: Wrong Answer");
                wrongAnswers++;
                selectBtn.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.wrong_answer_btn_bg));
                questionFeedback.setText(String.format("Wrong\nCorrect Ans: %s", questionsToAnswer.get(currentQuestion - 1).getAnswer()));
                questionFeedback.setTextColor(Color.RED);
            }
        }

        //Set Can Answer To False
        canAnswer = false;

        //Stop The Timer
        countdownTimer.cancel();

        //Show Next Button
        showNextBtn();
    }

    private void showNextBtn() {
        if(currentQuestion == totalQuestionsToAnswer){
            nextBtn.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setClickable(true);

        Log.d(TAG, "showNextBtn: true");
    }
}