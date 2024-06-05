package com.example.todolist;

public class Task {
    public String id;
    public String task;

    public Task() {}

    public Task(String id, String task) {
        this.id = id;
        this.task = task;
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }
}
