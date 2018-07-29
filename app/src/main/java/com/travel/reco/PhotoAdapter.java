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
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<Photo> photos;
    private Context context;
    public View.OnClickListener onUnitedClickListener;

    public PhotoAdapter(Context context, List<Photo> photos, View.OnClickListener onClickListener) {
        this.photos = photos;
        this.context = context;
        onUnitedClickListener = onClickListener;
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

        public CarouselView imageCarouselView;
        public TextView locationTextView;
        public ImageView unitedImage;

        public PhotoViewHolder(View v) {
            super(v);
            imageCarouselView = v.findViewById(R.id.carouselView);
            locationTextView = v.findViewById(R.id.location);
            unitedImage = v.findViewById(R.id.unitedImage);
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
        final String[] urls = photo.getUrls();
        holder.imageCarouselView.setPageCount(urls.length);
        holder.imageCarouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .error(R.drawable.ic_cross_solid);
                Glide.with(context)
                        .asBitmap()
                        .load(urls[position])
                        .apply(options)
                        .into(imageView);
            }
        });

        holder.locationTextView.setText(photo.getLocation());
        String desc = "";
        for (String tag : photo.getDescription()) {
            desc = desc + tag + ", ";
        }
        holder.unitedImage.setOnClickListener(onUnitedClickListener);
    }
}
