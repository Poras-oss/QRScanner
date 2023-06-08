package com.example.qrscanner;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.mViewHolder> {

    Context context;
    ArrayList<String> list;

    public Adapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Adapter.mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.mViewHolder holder, int position) {


        holder.textView.setText(list.get(position));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class mViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;

        public mViewHolder(@NonNull View itemView) {
            super(itemView);
              textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}
