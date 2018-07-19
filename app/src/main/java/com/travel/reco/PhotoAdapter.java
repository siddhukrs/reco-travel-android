package com.travel.reco;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<Photo> photos;
    private Context context;

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.photos = photos;
        this.context = context;
    }

    public void prependPhoto(Photo photo) {
        photos.add(0, photo);
        notifyItemInserted(0);
    }

    public void appendPhoto(Photo photo) {
        int itemCount = getItemCount();
        photos.add(itemCount, photo);
        notifyItemInserted(itemCount);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        public ImageView photoImageView;
        public TextView locationTextView;
        public TextView descriptionTextView;

        public PhotoViewHolder(View v) {
            super(v);
            photoImageView = v.findViewById(R.id.photo);
            locationTextView = v.findViewById(R.id.location);
            descriptionTextView = v.findViewById(R.id.description);
        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item, viewGroup, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        String url = photo.getUrl();
        RequestOptions options = new RequestOptions()
                .fitCenter()
                .error(R.drawable.ic_cross_solid);

        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(options)
                .into(holder.photoImageView);
        holder.locationTextView.setText(photo.getLocation());
        holder.descriptionTextView.setText(photo.getDescription());
    }
}
