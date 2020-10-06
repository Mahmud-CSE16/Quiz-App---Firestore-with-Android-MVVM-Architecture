package com.mahmud.quizapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mahmud.quizapp.FirebaseRepository;
import com.mahmud.quizapp.model.QuizListModel;

import java.util.List;

public class QuizListViewModel extends ViewModel implements FirebaseRepository.OnFirestoreTaskComplete {

    private MutableLiveData<List<QuizListModel>> quizListModelData = new MutableLiveData<>();

    public LiveData<List<QuizListModel>> getQuizListModelData() {
        return quizListModelData;
    }

    private FirebaseRepository firebaseRepository = new FirebaseRepository(this);


    public QuizListViewModel(){
        firebaseRepository.getQuizData();
    }

    @Override
    public void quizListDataAdded(List<QuizListModel> quizListModels) {
        quizListModelData.setValue(quizListModels);
    }

    @Override
    public void onError(Exception e) {

    }
}
