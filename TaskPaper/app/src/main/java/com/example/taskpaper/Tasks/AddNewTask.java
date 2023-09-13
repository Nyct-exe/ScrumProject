package com.example.taskpaper.Tasks;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxAppClientV2;
import com.dropbox.core.v2.DbxAppClientV2Base;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.example.taskpaper.Model.TaskModel;
import com.example.taskpaper.R;
import com.example.taskpaper.Utils.DatabaseHandler;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;

import com.example.taskpaper.ui.Dropbox.UriHelpers;
import com.example.taskpaper.ui.Dropbox.UserActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.util.Date;

import com.example.taskpaper.ui.Dropbox.FilesActivity;

public class AddNewTask extends BottomSheetDialogFragment {

    private static final String ACCESS_TOKEN = "<ACCESS TOKEN>";

    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private EditText fileName;
    private Button newTaskSaveButton;

    private DatabaseHandler db;

    public static AddNewTask newInstance(){

        return new AddNewTask();
    }

    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    private static void uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            IOUtil.ProgressListener progressListener = l -> printProgress(l, localFile.length());

            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(in, progressListener);

            System.out.println(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            System.exit(1);
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
            System.exit(1);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);

        verifyStoragePermissions(getActivity());

    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);
        fileName = requireView().findViewById(R.id.fileName);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            String title = bundle.getString("file_name");
            title = title.substring(0, title.length() - 4);
            newTaskText.setText(task);
            fileName.setText(title);
            fileName.setFocusable(false);
            assert task != null;
            if(task.length()>0)
                newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary_dark));
        }



        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else{
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary_dark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {

            private String filename = fileName.getText().toString().trim() + ".taskpaper";
            private String content = newTaskText.getText().toString();


            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();

                if(finalIsUpdate){
                    db.updateTask(bundle.getInt("id"), text);

                } else if (fileName.getText().toString().replaceAll("\\s+","").isEmpty()){
                    Toast.makeText(getContext(), "File name was empty", Toast.LENGTH_SHORT).show();
                } else if (db.exists(fileName.getText().toString() + ".taskpaper") == true) {
                    Toast.makeText(getContext(), "File name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    TaskModel task = new TaskModel();
                    task.setTask(text);
                    task.setStatus(0);
                    task.setName(fileName.getText().toString() + ".taskpaper");
                    db.insertTask(task);

                    //display file saved message
                    Toast.makeText(getContext(), "File saved successfully!",
                            Toast.LENGTH_SHORT).show();

                    Log.d("Name", "test:" + fileName.getText().toString());

                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        // Do something for 28 and versions below

                        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Taskpaper");

                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                Log.d("App", "failed to create directory");
                            }
                        }

                        File textFile = new File(mediaStorageDir, fileName.getText().toString() + ".taskpaper");

                        if(!textFile.exists()) {
                            try {
                                textFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try  {
                            FileOutputStream fOut = new FileOutputStream(textFile);
                            OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
                            outputWriter.write(text);
                            outputWriter.close();

                            //display file saved message
                            // Toast.makeText(getContext(), "File saved successfully!",
                            //  Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Can't save the file", Toast.LENGTH_SHORT).show();
                        }

                        androidx.appcompat.app.AlertDialog.Builder alertDlg = new AlertDialog.Builder(getContext());
                        alertDlg.setMessage("Do you want to upload the file to dropbox?");
                        alertDlg.setCancelable(false);
                        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Log.d("CREATION1", fileName.getText().toString() + ".taskpaper");

                                try (InputStream in = new FileInputStream(textFile)) {

                                    Log.d("CREATION2", String.valueOf(in));

                                    FileMetadata metadata = DropboxClientFactory.getClient().files().upload("/" + fileName.getText().toString() + ".taskpaper")
                                            .uploadAndFinish(in);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException | DbxException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        alertDlg.create().show();

                    } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.R ){
                        // do something for phones running an SDK 30


                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Taskpaper");

                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                Log.d("App", "failed to create directory");
                            }
                        }

                        File textFile = new File(mediaStorageDir, fileName.getText().toString() + ".taskpaper");

                        if(!textFile.exists()) {
                            try {
                                textFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try  {
                            FileOutputStream fOut = new FileOutputStream(textFile);
                            OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
                            outputWriter.write(text);
                            outputWriter.close();

                            //display file saved message
                            //Toast.makeText(getContext(), "File saved successfully!",
                            //  Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Can't save the file", Toast.LENGTH_SHORT).show();
                        }

                        androidx.appcompat.app.AlertDialog.Builder alertDlg = new AlertDialog.Builder(getContext());
                        alertDlg.setMessage("Do you want to upload the file to dropbox?");
                        alertDlg.setCancelable(false);
                        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try (InputStream in = new FileInputStream(textFile)) {
                                    FileMetadata metadata = DropboxClientFactory.getClient().files().upload("/" + fileName.getText().toString() + ".taskpaper")
                                            .uploadAndFinish(in);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException | DbxException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        alertDlg.create().show();


                    } else{

                        File mediaStorageDir = new File(String.valueOf(getContext().getExternalFilesDir("Taskpaper")));

                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {
                                Log.d("App", "failed to create directory");
                            }
                        }

                        File textFile = new File(mediaStorageDir, fileName.getText().toString() + ".taskpaper");

                        if(!textFile.exists()) {
                            try {
                                textFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try  {
                            FileOutputStream fOut = new FileOutputStream(textFile);
                            OutputStreamWriter outputWriter=new OutputStreamWriter(fOut);
                            outputWriter.write(text);
                            outputWriter.close();

                            //display file saved message
                            //Toast.makeText(getContext(), "File saved successfully!",
                            //  Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Can't save the file", Toast.LENGTH_SHORT).show();
                        }

                        androidx.appcompat.app.AlertDialog.Builder alertDlg = new AlertDialog.Builder(getContext());
                        alertDlg.setMessage("Do you want to upload the file to dropbox?");
                        alertDlg.setCancelable(false);
                        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try (InputStream in = new FileInputStream(textFile)) {
                                    FileMetadata metadata = DropboxClientFactory.getClient().files().upload("/" + fileName.getText().toString() + ".taskpaper")
                                            .uploadAndFinish(in);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException | DbxException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        alertDlg.create().show();

                    }

                   /* androidx.appcompat.app.AlertDialog.Builder alertDlg = new AlertDialog.Builder(getContext());
                    alertDlg.setMessage("Do you want to upload the file to dropbox?");
                    alertDlg.setCancelable(false);
                    alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(view.getContext(), FilesActivity.class);
                            view.getContext().startActivity(intent);
                        }
                    });
                    alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alertDlg.create().show();*/

                }
                dismiss();

            }

        });

    }

    public boolean checkPermission(String permission){

        int check = ContextCompat.checkSelfPermission(getContext(), permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener)
            ((DialogCloseListener)activity).handleDialogClose(dialog);
    }
}
