package com.example.taskpaper.Tasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.example.taskpaper.Model.TaskModel;
import com.example.taskpaper.R;
import com.example.taskpaper.TaskActivity;
import com.example.taskpaper.Utils.DatabaseHandler;
import com.example.taskpaper.ui.Dropbox.DownloadFileTask;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class TaskEditActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private List<TaskModel> taskList;
    int position;
    String path;

    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        Intent intent = getIntent();
        position = intent.getIntExtra("position",0);
        db = new DatabaseHandler(this);
        db.openDatabase();
        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        mEditText = findViewById(R.id.textView);
        /*
        Enables Network on main thread to download the file from Dropbox seamlessly
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            // Dropbox File
            FileMetadata fileMetadata = (FileMetadata) DropboxClientFactory.getClient().files().getMetadata("/" + taskList.get(position).getName());
            String FILE_NAME = taskList.get(position).getName();
            String path = Environment.getExternalStoragePublicDirectory("Taskpaper").toString() +"/" + FILE_NAME;

            new DownloadFileTask(this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
                @Override
                public void onDownloadComplete(File result) {
                    if (result != null) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(path);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder sb = new StringBuilder();
                            String text;

                            while((text = br.readLine()) != null){
                                sb.append(text).append("\n");
                            }

                            mEditText.setText(sb.toString());

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            if(fis != null){
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(TaskEditActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }).execute(fileMetadata);
        } catch (DbxException e) {
            e.printStackTrace();
        }

    }

    public void save() throws IOException {

        String FILE_NAME = taskList.get(position).getName();
        String path = Environment.getExternalStoragePublicDirectory("Taskpaper").toString() +"/" + FILE_NAME;


        String text = mEditText.getText().toString();
        FileOutputStream fos = new FileOutputStream(path);

        fos.write(text.getBytes());

        mEditText.getText().clear();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        if(fos != null)
            fos.close();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String FILE_NAME = taskList.get(position).getName();

        Log.d("CREATION3", FILE_NAME);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Taskpaper");
        File textFile = new File(mediaStorageDir, FILE_NAME);

        try (InputStream in = new FileInputStream(textFile)) {

            Log.d("CREATION4", String.valueOf(in));

            FileMetadata metadata = DropboxClientFactory.getClient().files().uploadBuilder("/" + FILE_NAME).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);

//            FileMetadata metadata = client.files().uploadBuilder(foldername).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | DbxException e) {
            e.printStackTrace();
        }

        Intent newIntent = new Intent(this,TaskActivity.class);
        newIntent.putExtra("position",position);
        startActivity(newIntent);
    }
}