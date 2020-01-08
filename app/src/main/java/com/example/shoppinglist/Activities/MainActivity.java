package com.example.shoppinglist.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.shoppinglist.ApplicationClass;
import com.example.shoppinglist.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String LOGGED_OUT = "Pomyślnie wylogowano.";
    private final static String ERROR = "Błąd: ";
    private final static String LOGGING_OUT = "Trwa wylogowywanie...proszę czekać...";
    private final static String LOGGED_AS = "Zalogowano jako: ";
    private final static String KEY_NAME = "name";

    private View progressView, loginFormView;
    private TextView tvLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);
        getSupportActionBar().setTitle(LOGGED_AS + ApplicationClass.user.getProperty(KEY_NAME));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOutIcon:
                logoutUser();
        }
        return true;
    }

    public void btnListManagementListener(View view) {
        Intent proceed = new Intent(MainActivity.this, ListManagement.class);
        startActivity(proceed);
    }

    public void btnFriendManagementListener(View view) {
        Intent proceed = new Intent(MainActivity.this, FriendsManagement.class);
        startActivity(proceed);
    }

    public void btnSettingsActivityListener (View view) {
        Intent proceed = new Intent(MainActivity.this, Settings.class);
        startActivity(proceed);
    }

    private void logoutUser() {
        showProgress(true);
        tvLoad.setText(LOGGING_OUT);
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                ApplicationClass.user = null;
                ApplicationClass.lastManagedList = new ArrayList<>();
                ApplicationClass.lastManagedListName = "bez nazwy";
                showToast(LOGGED_OUT);
                Intent proceed = new Intent(MainActivity.this, Login.class);
                startActivity(proceed);
                MainActivity.this.finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }

    private void showToast (String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void findViews() {
        progressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);
        loginFormView = findViewById(R.id.login_form);
    }
}
