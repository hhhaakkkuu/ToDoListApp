package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnItemClickListener{


    private EditText task_et;
    private ImageButton button_add , exit;
    private RecyclerView rv;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        task_et = findViewById(R.id.task_et);
        button_add = findViewById(R.id.button_add);
        exit = findViewById(R.id.exit);
        rv = findViewById(R.id.rv);

        if(currentUser == null){
            startActivity(new Intent(MainActivity.this, Start_Activity.class));
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Log Out")
                        .setMessage("Вийти з облікового запису? ")
                        .setPositiveButton("Так", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this,Start_Activity.class));
                                Toast.makeText(MainActivity.this, "Ви вийшли з облікового запису", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Ні", null)
                        .show();


            }
        });

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(taskAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("tasks");

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        fetchTasks();

    }

    private void addTask() {
        String taskText = task_et.getText().toString().trim();
        if (!taskText.isEmpty()) {
            String id = databaseReference.push().getKey();
            Task task = new Task(id, taskText);
            databaseReference.child(id).setValue(task);
            task_et.setText("");
        }
    }

    private void fetchTasks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    taskList.add(task);
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    @Override
    public void onItemClick(Task task) {
        // Підтвердження видалення завдання
        if (task != null && task.getId() != null) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Видалити завдання?")
                    .setMessage("Ви хочете видалити наступне завдання: " + task.getTask())
                    .setPositiveButton("Так", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.child(task.getId()).removeValue();
                            Toast.makeText(MainActivity.this, "Завдання видалено!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Ні", null)
                    .show();
        }
    }
}