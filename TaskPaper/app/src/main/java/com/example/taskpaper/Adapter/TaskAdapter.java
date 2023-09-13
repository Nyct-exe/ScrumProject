package com.example.taskpaper.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.example.taskpaper.Tasks.AddNewTask;
import com.example.taskpaper.Model.TaskModel;
import com.example.taskpaper.R;
import com.example.taskpaper.Utils.DatabaseHandler;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;
import com.example.taskpaper.ui.home.HomeFragment;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskModel> taskList;
    private HomeFragment homeFragment;
    private DatabaseHandler db;
    public Context context;

    public TaskAdapter(DatabaseHandler db, HomeFragment homeFragment){
        this.db = db;
        this.homeFragment = homeFragment;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        context = homeFragment.getContext();
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        db.openDatabase();
        TaskModel item = taskList.get(position);

        String name = item.getName();
        name = name.replace(".taskpaper","");

        holder.filler.setText(name);
        holder.task.setChecked(toBoolean(item.getStatus()));
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    db.updateStatus(item.getId(), 1);
                }
                else{
                    db.updateStatus(item.getId(),0);
                }
            }
        });
    }
    @Override
    public int getItemCount(){
        return taskList.size();
    }

    private boolean toBoolean(int n){
        return n!=0;
    }

    public void setTasks(List<TaskModel> taskList){
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DbxClientV2 client = DropboxClientFactory.getClient();
        TaskModel item = taskList.get(position);

        Log.d("Something", item.getName());

        try {
            client.files().deleteV2("/" + item.getName());
        } catch (DbxException e) {
            e.printStackTrace();
        }

        db.deleteTask(item.getId());
        taskList.remove(position);
        notifyItemRemoved(position);

    }

    public void logError(){
        Toast.makeText(context, "User is not logged into Dropbox", Toast.LENGTH_SHORT).show();
    }

    public void editItem(int position){
        TaskModel item = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task",item.getTask());
        bundle.putString("file_name",item.getName());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(homeFragment.getParentFragmentManager(), AddNewTask.TAG);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView filler;

        ViewHolder(View view){
            super(view);
            task = view.findViewById(R.id.taskCheckBox);
            filler = view.findViewById(R.id.fillerText);
        }

    }

}
