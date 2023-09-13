package com.example.taskpaper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.example.taskpaper.Model.TaskModel;
import com.example.taskpaper.Tasks.TaskEditActivity;
import com.example.taskpaper.Utils.DatabaseHandler;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;
import com.example.taskpaper.ui.Dropbox.GetCurrentAccountTask;
import com.example.taskpaper.ui.Dropbox.GetSharedUrl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    int position;
    private DatabaseHandler db;
    private List<TaskModel> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        WebView view = (WebView) this.findViewById(R.id.taskView);
        WebSettings webSettings = view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Intent intent = getIntent();
        position = intent.getIntExtra("position",position);
        db = new DatabaseHandler(this);
        db.openDatabase();
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        /*
        Call to get a Shared url that is needed for javascript
         */

        new GetSharedUrl(DropboxClientFactory.getClient(),taskList.get(position).getName(), new GetSharedUrl.GetUrlResponse() {
            @Override
            public void processFinish(String output) {

                if(output == null){
//                    Toast.makeText(TaskActivity.this, "File does not exist in dropbox", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else
                    view.loadDataWithBaseURL("file:///android_asset/",
                            "<!DOCTYPE html>\n" +
                                    "<html>\n" +
                                    "\t<head>\n" +
                                    "\t\t<title>To Do</title>\n" +
                                    "\t\t<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                                    "\t\t<!-- Bootstrap -->\n" +
                                    "\t\t<link rel='Stylesheet' href='http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.0.3/css/bootstrap.min.css' />\n" +
                                    "\t\t<link rel='Stylesheet' href='http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.0.3/css/bootstrap-theme.min.css' />\n" +
                                    "\t\t<link rel='Stylesheet' href='./taskpaper.css' />\n" +
                                    "\t</head>\n" +
                                    "\t<body>\n" +
                                    "\t\t<div class='container'>\n" +
                                    "\t\t\t<div class='row'>\n" +
                                    "\t\t\t\t<div class='col-sm-12'>\n" +
                                    "\t\t\t\t\t<div id='scratchTasks' class='taskDiv'></div>\n" +
                                    "\t\t\t\t</div>\n" +
                                    "\n" +
                                    "\t\t\t</div>\n" +
                                    "\t\t</div>\n" +
                                    "\n" +
                                    "\t\t<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->\n" +
                                    "\t\t<script src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js'></script>\n" +
                                    "\t\t<!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                                    "\t\t<script src='http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.0.3/js/bootstrap.min.js'></script>\n" +
                                    "\t\t<script src='./taskpaper.js'></script>\n" +
                                    "\t\t<script type='text/JavaScript'>\n" +
                                    "\t\t\t$(document).ready(function(){\n" +
                            /*
                            The Output is the link to Dropbox file needs to be generated for all different files on creation
                             */
                                    "\t\t\t\tnew TaskPaperPanel('#scratchTasks'," + output + ", 4000);\n" +
                                    "\t\t\t});\n" +
                                    "\t\t</script>\n" +
                                    "\t</body>\n" +
                                    "</html>"
                            ,"text/html","UTF-8",null);

            }
        }).execute();
    db.close();

    view.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            taskList.get(position);
            Intent newIntent = new Intent (getApplicationContext(), TaskEditActivity.class);
            newIntent.putExtra("position",position);
            startActivity(newIntent);
            return false;
        }
    });

    }

}