package com.example.taskpaper.ui.Dropbox;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.example.taskpaper.MainActivity;
import com.example.taskpaper.R;
import com.example.taskpaper.databinding.ActivityMainBinding;
import com.example.taskpaper.databinding.NavHeaderMainBinding;

import java.util.Arrays;

/**
 * Activity that shows information about the currently logged in user
 */
public class UserActivity extends DropboxActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
  /** Does the actual Logging in part**/
        DropboxActivity.startOAuth2Authentication(UserActivity.this, getString(R.string.app_key), Arrays.asList("account_info.read", "files.content.write"));

    }

    @Override
    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                Toast.makeText(UserActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();

    }
    /**Changes activity to MainActivity after user gets back to the app**/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

}
