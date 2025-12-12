package com.example.wishlist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.wishlist.R;
import com.example.wishlist.models.Wish;
import java.util.List;

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.ViewHolder> {

    private List<Wish> wishes;
    private Context context;
    private OnWishClickListener listener;

    public interface OnWishClickListener {
        void onWishClick(Wish wish);
        void onWishLongClick(Wish wish);
        void onEditClick(Wish wish);
        void onDeleteClick(Wish wish);
        void onStatusChangeClick(Wish wish);
    }

    public WishAdapter(List<Wish> wishes, Context context, OnWishClickListener listener) {
        this.wishes = wishes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wish wish = wishes.get(position);

        holder.tvTitle.setText(wish.getTitle());

        if (wish.getCategoryName() != null && !wish.getCategoryName().isEmpty()) {
            holder.tvCategory.setText(wish.getCategoryName());
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        holder.tvPrice.setText(wish.getFormattedPrice());
        holder.tvStatus.setText(wish.getStatusText());
        holder.tvStatus.setBackgroundColor(context.getResources().getColor(wish.getStatusColorResId()));

        // Цвет приоритета
        holder.viewPriority.setBackgroundColor(
                context.getResources().getColor(wish.getPriorityColorResId()));

        // Загрузка изображения с помощью Glide
        if (wish.getImageUrl() != null && !wish.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(wish.getImageUrl())
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_photo_placeholder);
        }

        // Обработчики кликов
        holder.itemView.setOnClickListener(v -> listener.onWishClick(wish));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onWishLongClick(wish);
            return true;
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(wish));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(wish));
        holder.btnStatus.setOnClickListener(v -> listener.onStatusChangeClick(wish));
    }

    @Override
    public int getItemCount() {
        return wishes != null ? wishes.size() : 0;
    }

    public void updateWishes(List<Wish> newWishes) {
        if (wishes == null) {
            wishes = newWishes;
        } else {
            wishes.clear();
            wishes.addAll(newWishes);
        }
        notifyDataSetChanged();
    }

    public void addWish(Wish wish) {
        if (wishes != null) {
            wishes.add(0, wish);
            notifyItemInserted(0);
        }
    }

    public void updateWish(int position, Wish wish) {
        if (wishes != null && position >= 0 && position < wishes.size()) {
            wishes.set(position, wish);
            notifyItemChanged(position);
        }
    }

    public void removeWish(int position) {
        if (wishes != null && position >= 0 && position < wishes.size()) {
            wishes.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvCategory, tvPrice, tvStatus;
        View viewPriority, btnEdit, btnDelete, btnStatus;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            viewPriority = itemView.findViewById(R.id.viewPriority);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnStatus = itemView.findViewById(R.id.btnStatus);
        }
    }
}