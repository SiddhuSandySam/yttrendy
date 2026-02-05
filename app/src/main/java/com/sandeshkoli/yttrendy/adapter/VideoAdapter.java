package com.sandeshkoli.yttrendy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.sandeshkoli.yttrendy.R;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.utils.NumberFormatter;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_AD = 1;

    private Context context;
    private List<VideoItem> videoList;
    private OnItemClickListener listener;
    private boolean useFullWidth;

    public interface OnItemClickListener {
        void onItemClick(VideoItem item);
    }

    public VideoAdapter(Context context, List<VideoItem> videoList, OnItemClickListener listener) {
        this(context, videoList, false, listener);
    }

    public VideoAdapter(Context context, List<VideoItem> videoList, boolean useFullWidth, OnItemClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.useFullWidth = useFullWidth;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && (position + 1) % 5 == 0) return TYPE_AD;
        return TYPE_VIDEO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_AD) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_native_ad, parent, false);
            return new AdViewHolder(view);
        } else {
            int layoutId = useFullWidth ? R.layout.item_video_full : R.layout.video_item_card;
            View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_AD) {
            populateNativeAd((AdViewHolder) holder);
        } else {
            int videoIndex = position - (position / 5);
            if (videoIndex < videoList.size()) {
                // VideoViewHolder mein cast karke data bind karo
                bindVideoData((VideoViewHolder) holder, videoList.get(videoIndex));
            }
        }
    }

// VideoAdapter.java ke andar bindVideoData method dhundo aur replace karo:

    private void bindVideoData(VideoViewHolder holder, VideoItem currentItem) {
        if (currentItem.getSnippet() != null) {
            holder.titleTextView.setText(currentItem.getSnippet().getTitle());
            String channelName = currentItem.getSnippet().getChannelTitle();
            String detailsText = channelName;

            if (currentItem.getStatistics() != null && currentItem.getStatistics().getViewCount() != null) {
                String formattedViews = NumberFormatter.formatViewCount(Long.parseLong(currentItem.getStatistics().getViewCount()));
                detailsText += " • " + formattedViews + " views";
            }
            holder.detailsTextView.setText(detailsText);

            // --- OPTIMIZED GLIDE LOADING ---
            Glide.with(context)
                    .load(currentItem.getSnippet().getThumbnails().getHigh().getUrl())
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL) // Cache Original & Resized
                    .placeholder(R.color.cardview_dark_background) // Load hone tak Dark Gray dikhega
                    .error(android.R.drawable.ic_menu_report_image) // Agar load fail ho to
                    .centerCrop() // Image ko properly fit karega
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade()) // Smooth Fade-in Effect
                    .into(holder.thumbnailImageView);

            // Click Listeners
            holder.itemView.setOnClickListener(v -> listener.onItemClick(currentItem));

            if (holder.btnShare != null) {
                holder.btnShare.setOnClickListener(v ->
                        shareVideo(context, currentItem.getId(), currentItem.getSnippet().getTitle()));
            }

            if (holder.btnSave != null) {
                holder.btnSave.setOnClickListener(v ->
                        saveVideoToDb(context, currentItem));
            }
        }
    }
    private void shareVideo(Context ctx, String vId, String vTitle) {
        // FIX: Missing URL part fixed
        String youtubeUrl = "https://www.youtube.com/watch?v=" + vId;
        String msg = vTitle + "\n\nWatch here: " + youtubeUrl + "\n\nvia ViralVideo App";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        ctx.startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void saveVideoToDb(Context ctx, VideoItem item) {
        com.sandeshkoli.yttrendy.network.AppDatabase db = com.sandeshkoli.yttrendy.network.AppDatabase.getInstance(ctx);
        com.sandeshkoli.yttrendy.models.VideoEntity entity = new com.sandeshkoli.yttrendy.models.VideoEntity(
                item.getId(),
                item.getSnippet().getTitle(),
                item.getSnippet().getChannelTitle(),
                item.getSnippet().getThumbnails().getHigh().getUrl()
        );
        db.videoDao().saveVideo(entity);
        Toast.makeText(ctx, "Saved to Library!", Toast.LENGTH_SHORT).show();
    }

    private void populateNativeAd(AdViewHolder holder) {
        AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3047884361380270/9499781459")
                .forNativeAd(nativeAd -> {
                    NativeAdView adView = holder.adView;
                    adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
                    adView.setBodyView(adView.findViewById(R.id.ad_body));
                    adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
                    adView.setIconView(adView.findViewById(R.id.ad_app_icon));
                    adView.setMediaView(adView.findViewById(R.id.ad_media));

                    ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
                    if (nativeAd.getBody() == null) adView.getBodyView().setVisibility(View.INVISIBLE);
                    else {
                        adView.getBodyView().setVisibility(View.VISIBLE);
                        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                    }
                    if (nativeAd.getCallToAction() != null) {
                        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
                    }
                    adView.setNativeAd(nativeAd);
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public int getItemCount() {
        if (videoList.isEmpty()) return 0;
        return videoList.size() + (videoList.size() / 4);
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView, btnShare, btnSave;
        TextView titleTextView, detailsTextView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            titleTextView = itemView.findViewById(R.id.video_title_text_view);
            detailsTextView = itemView.findViewById(R.id.video_details_text_view);
            btnShare = itemView.findViewById(R.id.btn_share_card);
            btnSave = itemView.findViewById(R.id.btn_save_card);
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        NativeAdView adView;
        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.native_ad_view);
        }
    }
}