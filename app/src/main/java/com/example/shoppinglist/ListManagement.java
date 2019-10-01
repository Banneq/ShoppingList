package com.example.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class ListManagement extends AppCompatActivity {
    private static final String LIST = "Lista: ";

    private View progressView, loginFormView;
    private TextView tvLoad;
    private RecyclerView rvProducts;
    private RecyclerView.Adapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_management);

        findViews();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        RecyclerView.LayoutManager rvLayoutManager = new LinearLayoutManager(this);
        rvProducts.setHasFixedSize(true);
        rvProducts.setLayoutManager(rvLayoutManager);
        rvAdapter = new ListRecyclerViewAdapter(this, ApplicationClass.lastManagedList);
        rvProducts.setAdapter(rvAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_management_menu, menu);
        getSupportActionBar().setTitle(LIST + ApplicationClass.lastManagedListName);
        return true;
    }



   public void ivAddListener(View view) {
        Products products = new Products();
        ApplicationClass.lastManagedList.add(products);
        rvAdapter.notifyDataSetChanged();
   }



    private void findViews() {
        progressView = findViewById(R.id.login_progress);
        loginFormView = findViewById(R.id.login_form);
        tvLoad = findViewById(R.id.tvLoad);
        rvProducts = findViewById(R.id.rvProducts);
    }

    private void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }


}
