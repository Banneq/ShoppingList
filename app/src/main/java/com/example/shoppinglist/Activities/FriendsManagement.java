package com.example.shoppinglist.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.shoppinglist.ApplicationClass;
import com.example.shoppinglist.RecyclerViewAdapters.FriendsRecyclerViewAdapter;
import com.example.shoppinglist.Models.Friends;
import com.example.shoppinglist.R;

import java.util.ArrayList;
import java.util.List;

public class FriendsManagement extends AppCompatActivity implements FriendsRecyclerViewAdapter.FriendsManagementContext {
    private final static String NAV_BAR_TITLE = "Znajomi: ";
    private final static String ALERT_DIALOG_TITLE = "Dodaj znajomego.";
    private final static String TYPE_USER_NAME = "Wpisz nazwę użytkownika: ";
    private final static String NO_USER_IN_DB = "Operacja nie powiodła się, taki użytkownik nie istnieje!";
    private final static String FRIEND_ADDED_SUCCESSFULLY = "Dodano użytkownika do grona znajomych.";
    private final static String CANCEL = "Anuluj";
    private final static String CANNOT_ADD_YOURSELF = "Nie możesz dodać samego siebie.";
    private final static String USER_ALREADY_A_FRIEND = "Posiadasz już tę osobę w znajomych";

    private RecyclerView rvFriends;
    private RecyclerView.Adapter rvAdapter;
    private List<Friends> friends = new ArrayList<>();
    private List<BackendlessUser> friendsUsers = new ArrayList<>();
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_management);

        findViews();
        setUpRecyclerView();
        retrieveFriendsFromServer();
        actionBar = getSupportActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icAddFriend:
                addNewFriend();
        }

        return true;
    }

    private void addNewFriend() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(ALERT_DIALOG_TITLE)
                .setMessage(TYPE_USER_NAME)
                .setView(input)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newFriendsName = input.getText().toString();
                        searchForUserInDb(newFriendsName);
                    }
                })
                .setNegativeButton(CANCEL, null)
                .show();
    }

    private boolean newFriendIsValid(BackendlessUser user) {
        if (user.getUserId().equals(ApplicationClass.user.getUserId())) {
            showToast(CANNOT_ADD_YOURSELF);
            return false;
        }
        for (BackendlessUser userInDb: friendsUsers) {
            if (userInDb.getUserId().equals(user.getUserId())) {
                showToast(USER_ALREADY_A_FRIEND);
                return false;
            }
        }
        return true;
    }

    private void searchForUserInDb(String userName) {
        String whereClause = "name = '" + userName + "'";
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);
        Backendless.Persistence.of(BackendlessUser.class).find(dataQueryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                if (response.size() == 0) {
                    showToast(NO_USER_IN_DB);
                    return;
                }
                BackendlessUser user = response.get(0);
                if (newFriendIsValid(user)) {
                    friendsUsers.add(user);
                    addNewRelationship(user.getUserId());
                }

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast("ERROR:" + fault);
            }
        });
    }

    private void addNewRelationship(String userId) {
        final Friends newRelationship = new Friends();
        newRelationship.setFriendId(userId);
        Backendless.Persistence.of(Friends.class).save(newRelationship, new AsyncCallback<Friends>() {
            @Override
            public void handleResponse(Friends response) {
                showToast(FRIEND_ADDED_SUCCESSFULLY);
                friends.add(newRelationship);
                updateToolbarTitle();
                rvAdapter.notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast("ERROR: " + fault.getMessage());
                friendsUsers.remove(friendsUsers.size()-1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friends_management_menu, menu);
        updateToolbarTitle();
        return true;
    }

    private void setUpRecyclerView() {
        RecyclerView.LayoutManager rvLayoutManager= new LinearLayoutManager(this);
        rvFriends.setHasFixedSize(true);
        rvFriends.setLayoutManager(rvLayoutManager);
        rvAdapter = new FriendsRecyclerViewAdapter(this, friendsUsers);
        rvFriends.setAdapter(rvAdapter);
    }

    private void findViews() {
        rvFriends = findViewById(R.id.rv_friends);
    }

    private void retrieveFriendsFromServer() {
        String whereClause = "ownerId = '" + ApplicationClass.user.getUserId() + "'";
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);

        Backendless.Persistence.of(Friends.class).find(dataQueryBuilder, new AsyncCallback<List<Friends>>() {
            @Override
            public void handleResponse(List<Friends> response) {
                friends.addAll(response);
                retrieveUsersFromServer();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast("ERROR: " + fault.toString());
            }
        });
    }

    private void retrieveUsersFromServer() {
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < friends.size(); i++) {
            whereClause.append("objectId = '" + friends.get(i).getFriendId() + "' OR ");
        }
        int length = whereClause.length();
        if (length <= 4) {
            return;
        }
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause.substring(0, length-4));

        Backendless.Persistence.of(BackendlessUser.class).find(dataQueryBuilder, new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                friendsUsers.addAll(response);
                rvAdapter.notifyDataSetChanged();
                updateToolbarTitle();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast("ERROR: " + fault.toString());
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void updateToolbarTitle() {
        actionBar.setTitle(NAV_BAR_TITLE + friendsUsers.size());
    }
}
