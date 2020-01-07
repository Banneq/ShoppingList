package com.example.shoppinglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.BackendlessUser;

import java.util.List;

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {
    private List<BackendlessUser> friends;
    private Context context;
    private View view;
    private final static String _NAME = "name";
    private final static String NUMBER_OF_LIST_PREFIX = "Liczba współdzielonych list: ";

    public FriendsRecyclerViewAdapter (Context context, List<BackendlessUser> friends) {
        this.context = context;
        this.friends = friends;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView sharedListsNumber;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews();
        }

        private void findViews() {
            name = view.findViewById(R.id.tvName);
            sharedListsNumber = view.findViewById(R.id.tvSharedListNumber);
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
        holder.sharedListsNumber.setText(NUMBER_OF_LIST_PREFIX + "0");
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
