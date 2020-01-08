package com.example.shoppinglist.RecyclerViewAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.shoppinglist.Activities.ListManagement;
import com.example.shoppinglist.ApplicationClass;
import com.example.shoppinglist.Models.Friends;
import com.example.shoppinglist.Models.Products;
import com.example.shoppinglist.Models.SharedLists;
import com.example.shoppinglist.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {
    private List<BackendlessUser> friends;
    private FriendsManagementContext context;
    private View view;
    private final static String _NAME = "name";
    private final static String NUMBER_OF_LIST_PREFIX = "Liczba współdzielonych list: ";
    private final static String ALERT_DIALOG_TEXT = "Czy na pewno chcesz usunąć tego użytkownika z groma znajomych?";
    private final static String FRIEND_DELETED = "Użytkownik został usunięty z grona znajomych.";
    private final static String ALERT_DIALOG2_TITLE = "Wybierz listę do udostępnienia: ";
    private final static String LIST_HAS_BEED_SHARED = "Udostępniłeś swoją listę.";

    public FriendsRecyclerViewAdapter (Context context, List<BackendlessUser> friends) {
        this.context = (FriendsManagementContext) context;
        this.friends = friends;
    }

    public interface FriendsManagementContext {
        void updateToolbarTitle();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView sharedListsNumber;
        private ImageView ivDelete;
        private ImageView ivShare;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews();
            setIvDeleteListener();
            setIvShareListener();
        }

        private void findViews() {
            name = view.findViewById(R.id.tvName);
            sharedListsNumber = view.findViewById(R.id.tvSharedListNumber);
            ivDelete = view.findViewById(R.id.ivDelete);
            ivShare = view.findViewById(R.id.ivShare);
        }

        private void setIvDeleteListener() {
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    new AlertDialog.Builder((Context) context)
                            .setMessage(ALERT_DIALOG_TEXT)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteFriend((int) itemView.getTag());
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            });
        }


        private void deleteFriend(final int pos) {
            final BackendlessUser friendToDelete = friends.get(pos);
            String whereClause = "ownerId = '" + ApplicationClass.user.getUserId() +
                    "' AND friendId = '" + friendToDelete.getUserId() + "'";
            DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
            dataQueryBuilder.setWhereClause(whereClause);

            Backendless.Persistence.of(Friends.class).remove(whereClause, new AsyncCallback<Integer>() {
                @Override
                public void handleResponse(Integer response) {
                    showToast(FRIEND_DELETED);
                    friends.remove(pos);
                    notifyDataSetChanged();
                    context.updateToolbarTitle();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    showToast("ERROR: " + fault.getMessage());
                }
            });
        }

        private void setIvShareListener() {
            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getListNames();
                }
            });
        }

        private void getListNames () {
            String whereClause = "ownerId = '" + ApplicationClass.user.getUserId() + "'";
            DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
            dataQueryBuilder.setWhereClause(whereClause);

            Backendless.Persistence.of(Products.class).find(dataQueryBuilder, new AsyncCallback<List<Products>>() {
                @Override
                public void handleResponse(List<Products> response) {
                    Set <Products> productsSet = new HashSet<>();
                    Set <String> setNames = new HashSet<>();
                    for (Products product: response) {
                        if (!setNames.contains(product.getListName())) {
                            setNames.add(product.getListName());
                            productsSet.add(product);
                        }
                    }
                    pickFriend(productsSet);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    showToast("ERROR:" + fault.getMessage());
                }
            });

        }

        private void pickFriend(Set<Products> products) {
            final String [] array = new String[products.size()];
            int p = 0;
            for (Products product: products)
                array[p++] = product.getListName();

            final int [] checkedItem = {0};
            new AlertDialog.Builder((Context) context)
                    .setTitle(ALERT_DIALOG2_TITLE)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setSingleChoiceItems(array, checkedItem[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkedItem[0] = i;
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            addSharedListToDb(array[checkedItem[0]], (int) itemView.getTag());
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        private void addSharedListToDb(String listName, int friendPos) {
            BackendlessUser friend = friends.get(friendPos);
            SharedLists sharedLists = new SharedLists();
            sharedLists.setListName(listName);
            sharedLists.setOwnerName(ApplicationClass.user.getProperty(_NAME).toString());
            sharedLists.setFriendId(friend.getUserId());

            Backendless.Data.of(SharedLists.class).save(sharedLists, new AsyncCallback<SharedLists>() {
                @Override
                public void handleResponse(SharedLists response) {
                    showToast(LIST_HAS_BEED_SHARED);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    showToast("ERROR" + fault.getMessage());
                }
            });
        }
    }


    @NonNull
    @Override
    public FriendsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsRecyclerViewAdapter.ViewHolder holder, int position) {
        BackendlessUser friend = friends.get(position);
        holder.name.setText(friend.getProperty(_NAME).toString());
        holder.sharedListsNumber.setText(NUMBER_OF_LIST_PREFIX + "0"); //TODO
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    private void showToast(String text) {
        Toast.makeText((Context) context, text, Toast.LENGTH_LONG).show();
    }
}
