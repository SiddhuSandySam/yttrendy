package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.Snippet;
import com.sandeshkoli.yttrendy.models.Thumbnail;
import com.sandeshkoli.yttrendy.models.Thumbnails;
import com.sandeshkoli.yttrendy.models.VideoEntity;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.network.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class SavedVideosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoItem> videoItemList = new ArrayList<>();
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_videos);

        // 1. Setup Toolbar
        TextView title = findViewById(R.id.toolbar_title);
        title.setText("My Library");
        findViewById(R.id.btn_menu).setOnClickListener(v -> finish()); // Back behavior
        findViewById(R.id.btn_filter).setVisibility(View.GONE); // No filter needed here

        // 2. Init Views
        recyclerView = findViewById(R.id.saved_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);

        setupRecyclerView();
        loadSavedVideos();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 'true' pass kiya taaki bade cards dikhen
        adapter = new VideoAdapter(this, videoItemList, true, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadSavedVideos() {
        AppDatabase db = AppDatabase.getInstance(this);
        List<VideoEntity> savedEntities = db.videoDao().getAllSavedVideos();

        if (savedEntities == null || savedEntities.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);

            // --- THE CONVERTER LOOP ---
            // Database se aane wale VideoEntity ko VideoItem me convert kar rahe hain
            // Taki hamara purana Adapter bina crash huye chal jaye
            videoItemList.clear();
            for (VideoEntity entity : savedEntities) {
                videoItemList.add(convertToVideoItem(entity));
            }
            adapter.notifyDataSetChanged();
        }
    }

    // Helper to map DB object to API object
    private VideoItem convertToVideoItem(VideoEntity entity) {
        VideoItem item = new VideoItem();

        // 1. Set ID (Search fix wala logic dhyan me rakhte hue)
        item.setId(entity.videoId);

        // 2. Set Thumbnail
        Thumbnail highRes = new Thumbnail(entity.thumbnailUrl);
        Thumbnails thumbnails = new Thumbnails(highRes);

        // 3. Set Snippet (Title + Channel Name)
        Snippet snippet = new Snippet(entity.title, entity.channelName, thumbnails);
        item.setSnippet(snippet);

        return item;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSavedVideos(); // Refresh list every time screen opens
    }
}