package com.example.shoppinglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListRecyclerViewAdapter extends RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder> {

    private List<Products> productsList;
    private View view;
    Context context;




    public ListRecyclerViewAdapter (Context context, List <Products> productsList) {
        this.productsList = productsList;
        this.context = context;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText etProductName, etQuantity;
        private TextView tvUnit;
        private Spinner snrUnit;
        private ImageView ivDelete, ivEdit;
        private boolean isInEditMode;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            findViews(itemView);
            setUpUnitSpinner();
            setIvDeleteListener();
            setIvEditListener();
        }

        private void findViews (@NonNull View itemView) {
            etProductName = itemView.findViewById(R.id.etProductName);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            snrUnit = itemView.findViewById(R.id.snrUnit);
            tvUnit = itemView.findViewById(R.id.tvUnit);
        }

        private void setIvEditListener() {
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isInEditMode) {
                        ivEdit.setImageResource(R.drawable.check_icon);
                        setAllEditable(true);
                        isInEditMode = true;
                    } else {
                        ivEdit.setImageResource(R.drawable.edit_icon);
                        setNewValues();
                        setAllEditable(false);
                        isInEditMode = false;
                    }
                }
            });
        }

        private void setIvDeleteListener() {
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productsList.remove((Products) itemView.getTag());
                    notifyDataSetChanged();
                }
            });
        }

        private void setUpUnitSpinner() {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.units, android.R.layout.simple_spinner_dropdown_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            snrUnit.setAdapter(adapter);
            snrUnit.setEnabled(false);
        }

        private void setAllEditable (final boolean editable) {
            etProductName.setEnabled(editable);
            etQuantity.setEnabled(editable);
            snrUnit.setEnabled(editable);
            if (editable) {
                tvUnit.setVisibility(View.GONE);
                snrUnit.setVisibility(View.VISIBLE);
            } else {
                tvUnit.setVisibility(View.VISIBLE);
                snrUnit.setVisibility(View.GONE);
            }
        }

        private void setNewValues () {
            Products product = (Products) itemView.getTag();
            product.setProductName(etProductName.getText().toString().trim());
            product.setQuantity(etQuantity.getText().toString().trim());
            product.setUnit(snrUnit.getSelectedItem().toString());
            tvUnit.setText(product.getUnit());
        }

    }

    @NonNull
    @Override
    public ListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListRecyclerViewAdapter.ViewHolder holder, int position) {
        Products product = productsList.get(position);
        holder.itemView.setTag(product);
        holder.etProductName.setText(product.getProductName());
        holder.etQuantity.setText(product.getQuantity());
        holder.tvUnit.setText(product.getUnit());

        if (product.getUnit() != null) {
            holder.tvUnit.setVisibility(View.VISIBLE);
            holder.snrUnit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }



}
