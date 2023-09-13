package com.example.taskpaper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.auth.DbxAppAuthRequests;
import com.dropbox.core.v2.auth.DbxUserAuthRequests;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.users.FullAccount;
import com.example.taskpaper.Adapter.TaskAdapter;
import com.example.taskpaper.Tasks.DialogCloseListener;
import com.example.taskpaper.databinding.ActivityMainBinding;
import com.example.taskpaper.databinding.NavHeaderMainBinding;
import com.example.taskpaper.ui.Dropbox.DropboxActivity;
import com.example.taskpaper.ui.Dropbox.DropboxClientFactory;
import com.example.taskpaper.ui.Dropbox.GetCurrentAccountTask;
import com.example.taskpaper.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends DropboxActivity implements DialogCloseListener {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    /*
     When the dialog box closes this gives a signal to HomeFragment that the list needs updating
    */
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.homeFragment);
        if (fragment == null) {
            fragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.homeFragment, fragment)
                    .commitNow();
        }
        HomeFragment frag = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.homeFragment);
        frag.refreshTasksOnUpdate();


    }
    /*
    Checks if the user is logged in and activates the navigation button if so.
     */
    public void checkButton(){
        NavigationView navview = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navview.getHeaderView(0);
        if (hasToken()) {
            findViewById(R.id.files_button).setEnabled(true);
            findViewById((R.id.floatingActionButton)).setEnabled(true);
            headerView.findViewById(R.id.name_textView).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.email_textView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.files_button).setEnabled(false);
            findViewById((R.id.floatingActionButton)).setEnabled(false);
            headerView.findViewById(R.id.name_textView).setVisibility(View.GONE);
            headerView.findViewById(R.id.email_textView).setVisibility(View.GONE);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        checkButton();
    }
    /*
     * Loads in user data from dropbox*/
    @Override
    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                /**Changes Navigation header details to dropbox details**/
                NavigationView navview = (NavigationView) findViewById(R.id.nav_view);
                View headerView = navview.getHeaderView(0);
                TextView navUsername = (TextView) headerView.findViewById(R.id.name_textView);
                navUsername.setText(result.getName().getDisplayName());
                TextView navEmail = (TextView) headerView.findViewById(R.id.email_textView);
                navEmail.setText(result.getEmail());
            }
            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }

    /*
    Logs out the user from the app when the app is closed,
    or the user signs out from the browser.
     */
    @Override
    protected void onDestroy() {
        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
        prefs.edit().clear().commit();
        DropboxClientFactory.clearClient();
        AuthActivity.result = null;
        super.onDestroy();

    }


}

