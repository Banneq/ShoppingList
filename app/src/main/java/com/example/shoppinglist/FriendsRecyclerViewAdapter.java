package com.example.shoppinglist;

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

import java.util.List;

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {
    private List<BackendlessUser> friends;
    private FriendsManagementContext context;
    private View view;
    private final static String _NAME = "name";
    private final static String NUMBER_OF_LIST_PREFIX = "Liczba współdzielonych list: ";
    private final static String ALERT_DIALOG_TEXT = "Czy na pewno chcesz usunąć tego użytkownika z groma znajomych?";
    private final static String FRIEND_DELETED = "Użytkownik został usunięty z grona znajomych.";

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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews();
            setIvDeleteListener();
        }

        private void findViews() {
            name = view.findViewById(R.id.tvName);
            sharedListsNumber = view.findViewById(R.id.tvSharedListNumber);
            ivDelete = view.findViewById(R.id.ivDelete);
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
