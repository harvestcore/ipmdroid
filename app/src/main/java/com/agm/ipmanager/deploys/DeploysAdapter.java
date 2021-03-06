package com.agm.ipmanager.deploys;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.agm.ipmanager.R;

import java.util.ArrayList;

public class DeploysAdapter extends RecyclerView.Adapter<DeploysAdapter.Holder> {
    Context context;
    LayoutInflater inflater;
    ArrayList<Container> data;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public DeploysAdapter(Context context, ArrayList<Container> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.fragment_container_row, parent, false);
        return new Holder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Container container = data.get(position);

        holder.containerRowName.setText(container.name);
        holder.containerRowId.setText(container.id);

        if (container.status.equals("running")) {
            holder.containerRowIcon.setImageResource(R.drawable.running);
        } else if (container.status.equals("exited")) {
            holder.containerRowIcon.setImageResource(R.drawable.stopped);
        } else if (container.status.equals("paused")) {
            holder.containerRowIcon.setImageResource(R.drawable.paused);
        } else {
            holder.containerRowIcon.setImageResource(R.drawable.offline);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    public class Holder extends RecyclerView.ViewHolder {
        ConstraintLayout containerRowLayout;
        TextView containerRowName;
        TextView containerRowId;
        ImageView containerRowIcon;


        public Holder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            containerRowLayout = itemView.findViewById(R.id.containerRowLayout);
            containerRowName = itemView.findViewById(R.id.containerRowName);
            containerRowId = itemView.findViewById(R.id.containerRowId);
            containerRowIcon = itemView.findViewById(R.id.containerRowIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
