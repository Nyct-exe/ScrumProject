package com.example.taskpaper.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskpaper.Adapter.TaskAdapter;
import com.example.taskpaper.MainActivity;
import com.example.taskpaper.TaskActivity;
import com.example.taskpaper.Tasks.AddNewTask;
import com.example.taskpaper.Model.TaskModel;
import com.example.taskpaper.R;
import com.example.taskpaper.Tasks.RecyclerItemTouchHelper;
import com.example.taskpaper.Utils.DatabaseHandler;
import com.example.taskpaper.ui.Dropbox.DropboxActivity;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;
import com.example.taskpaper.ui.Dropbox.FilesActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter tasksAdapter;
    private DatabaseHandler db;
    private FloatingActionButton fab;

    private List<TaskModel> taskList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        taskList = new ArrayList<>();
        db = new DatabaseHandler(getContext());
        db.openDatabase();

        /*
        Removes The overlapping layers, that are created if there were old tasks in a list
         */
        if (container != null) {
            container.removeAllViews();
        }

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Button filesButton = root.findViewById(R.id.files_button);
        filesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(FilesActivity.getIntent(getContext(), ""));
            }
        });

        tasksRecyclerView = root.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksAdapter = new TaskAdapter(db,this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        fab = root.findViewById(R.id.floatingActionButton);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getParentFragmentManager(), AddNewTask.TAG);
            }
        });
        return root;
    }
    /*
    Class responsible for updating the tasks
     */
    public void refreshTasksOnUpdate(){

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
        /*
        Makes sure the button is grayed out after the dialogbox closes
         */
        ((MainActivity)getActivity()).checkButton();

    }

}