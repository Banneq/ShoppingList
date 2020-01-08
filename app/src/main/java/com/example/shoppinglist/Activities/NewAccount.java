package com.example.shoppinglist.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.shoppinglist.ApplicationClass;
import com.example.shoppinglist.R;

public class NewAccount extends AppCompatActivity {

    private final static String FILL_ALL_FIELDS = "Wypełnij wszystkie pola!";
    private final static String PASSWORDS_MUST_BE_IDENTICAL = "Wpisane hasła różnią się od siebie!";
    private final static String REGISTERING_NEW_USER = "SYSTEM REJESTRUE KONTO...PROSZĘ CZEKAĆ...";
    private final static String USER_REGISTERED = "Pomyślnie zarejestrowano konto.\nPotwierdź maila by się zalogować!";
    private final static String ERROR = "Błąd: ";
    private final static String KEY_NAME = "name";

    private EditText etMail, etPassword, etRePassword, etName;
    private TextView tvLoad;
    private View progressView, loginFormView;
    Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        findViews();
    }


    public void btnCreateListener(View view) {
        String email = etMail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || rePassword.isEmpty() || name.isEmpty()) {
            showToast(FILL_ALL_FIELDS);
            return;
        }
        if (!password.equals(rePassword)) {
            showToast(PASSWORDS_MUST_BE_IDENTICAL);
            return;
        }

        BackendlessUser user = setNewUser(email, password, name);
        showProgress(true);
        tvLoad.setText(REGISTERING_NEW_USER);
        registerUser(user);
    }

    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void findViews() {
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);
        tvLoad = findViewById(R.id.tvLoad);
        progressView = findViewById(R.id.login_progress);
        loginFormView = findViewById(R.id.login_form);
        btnCreate = findViewById(R.id.btnCreate);
        etName = findViewById(R.id.etName);
    }

    private void showToast (String text) {
        Toast.makeText(NewAccount.this, text, Toast.LENGTH_SHORT).show();
    }

    private BackendlessUser setNewUser (String email, String password, String name) {
        BackendlessUser newUser = new BackendlessUser();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setProperty(KEY_NAME, name);
        return newUser;
    }

    private void registerUser(final BackendlessUser user) {
        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                ApplicationClass.user = user;
                showToast(USER_REGISTERED);
                showProgress(false);
                NewAccount.this.finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }
}
