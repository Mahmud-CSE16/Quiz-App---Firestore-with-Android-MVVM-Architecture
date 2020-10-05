package com.mahmud.quizapp.model;

import com.google.firebase.firestore.DocumentId;

public class QuizListModel {
    @DocumentId
    private String id;
    private  String name, desc, image, level, visibility;
    private int questions;

    public QuizListModel() {
    }

    public QuizListModel(String name, String desc, String image, String level, String visibility, int questions) {
        this.name = name;
        this.desc = desc;
        this.image = image;
        this.level = level;
        this.visibility = visibility;
        this.questions = questions;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getQuestions() {
        return questions;
    }

    public void setQuestions(int questions) {
        this.questions = questions;
    }
}
