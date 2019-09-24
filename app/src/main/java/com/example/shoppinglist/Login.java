package com.example.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.List;

public class Login extends AppCompatActivity {

    private final static String FILL_ALL_FIELDS = "Wypełnij wszystkie pola!";
    private final static String USER_LOGGING = "Logowanie...Proszę czekać...";
    private final static String LOGGED_SUCCESSFULLY = "Pomyślnie zalogowano";
    private final static String ERROR = "Błąd: ";
    private final static String MAIL_SENT = "Link aktywacyjny ponownie wysłany.";
    private final static String DOWNLOADING_DATA = "Pobieranie danych z bazy...proszę czekać...";
    private final static String RECALL_MAIL_SENT = "Na twoją skyrznkę mailową została wysłana instrukcja zmiany hasła";
    private final static String LOGIN_CREDENTIALS = "Sprawdzanie danych logowania...proszę czekać...";
    private final static String LOGIN_IN_PROGRESS = "Logowanie...proszę czekać...";
    private final static String LOGGED_AS = "Witaj ponownie, ";
    private final static String KEY_NAME = "name";

    private EditText etMail, etPassword;
    private Button btnLogin, btnCreateNewAcc;
    private TextView tvRecall, tvLoad, tvReSend;
    private View progressView, loginFormView;
    private CheckBox chbStayLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViews();
        checkIfLoginIsValid();
    }

    public void tvRecallListener (View view) {
        String mail = etMail.getText().toString().trim();
        if (mail.isEmpty()) {
            showToast(FILL_ALL_FIELDS);
            return;
        }
        sendRecallMail(mail);
    }

    private void sendRecallMail(String mail) {
        showProgress(true);
        tvLoad.setText(DOWNLOADING_DATA);
        Backendless.UserService.restorePassword(mail, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                showToast(RECALL_MAIL_SENT);
                showProgress(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }

    public void btnLoginListener(View view) {
        String mail = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (mail.isEmpty() || password.isEmpty()) {
            showToast(FILL_ALL_FIELDS);
            return;
        }
        showProgress(true);
        tvLoad.setText(USER_LOGGING);
        loginUser(mail, password);
    }

    public void tvReSendListener(View view) {
        String mail = etMail.getText().toString().trim();
        if (mail.isEmpty()) {
            showToast(FILL_ALL_FIELDS);
            return;
        }

        String whereClause = "email = '" + mail +"'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        showProgress(true);
        tvLoad.setText(DOWNLOADING_DATA);
        Backendless.Persistence.of(BackendlessUser.class).find(queryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                if (response.size() >= 1) {
                    BackendlessUser user = response.get(0);
                    reSendEmail(user);
                }
                showProgress(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }

    private void reSendEmail (BackendlessUser user) {
        showProgress(true);
        Backendless.UserService.resendEmailConfirmation(user.getEmail(), new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                showToast(MAIL_SENT);
                showProgress(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }

    public void btnCreateNewAccListener(View view) {
        Intent proceed = new Intent(Login.this, NewAccount.class);
        startActivity(proceed);
    }

    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void findViews() {
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateNewAcc = findViewById(R.id.btnCreateNewAcc);
        tvRecall = findViewById(R.id.tvRecall);
        tvLoad = findViewById(R.id.tvLoad);
        progressView = findViewById(R.id.login_progress);
        loginFormView = findViewById(R.id.login_form);
        chbStayLogged = findViewById(R.id.chbStayLogged);
        tvReSend = findViewById(R.id.tvReSend);
    }

    private void showToast (String text) {
        Toast.makeText(Login.this, text, Toast.LENGTH_LONG).show();
    }

    private void loginUser (String email, String password) {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                ApplicationClass.user = response;
                showToast(LOGGED_SUCCESSFULLY);
                Intent proceed = new Intent(Login.this, MainActivity.class);
                startActivity(proceed);
                Login.this.finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        },chbStayLogged.isChecked());
    }

    private void checkIfLoginIsValid() {
        showProgress(true);
        tvLoad.setText(LOGIN_CREDENTIALS);

        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {
                if (response) {
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                    tvLoad.setText(LOGIN_IN_PROGRESS);
                    login(userObjectId);
                    showProgress(false);
                } else {
                    showProgress(false);
                }

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
                showProgress(false);
            }
        });
    }

    private void login(String userObjectId) {
        Backendless.Data.of(BackendlessUser.class).findById(userObjectId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                ApplicationClass.user = response;
                Intent proceed = new Intent(Login.this, MainActivity.class);
                startActivity(proceed);
                showToast(LOGGED_AS + response.getProperty(KEY_NAME).toString());
                Login.this.finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast(ERROR + fault.getMessage());
            }
        });
    }
}
