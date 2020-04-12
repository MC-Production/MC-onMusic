package com.mc.onmusic_relase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mc.onmusic_relase.adapters.NPlayAdapter;
import com.mc.onmusic_relase.helper.OnStartDragListener;
import com.mc.onmusic_relase.helper.SimpleItemTouchHelperCallback;
import com.mc.onmusic_relase.models.MetaModel;
import com.mc.onmusic_relase.models.NPlayModel;
import com.mc.onmusic_relase.services.MusicService;
import com.mc.onmusic_relase.utils.YTMeta;
import com.mc.onmusic_relase.utils.YTutils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NPlaylistActivity extends AppCompatActivity  implements OnStartDragListener {

    Toolbar toolbar;

    NPlayAdapter adapter;
    ArrayList<NPlayModel> models;
    TextView cTitle,cAuthor;
    TextView removeFromQueue;
    RecyclerView recyclerView;
    ImageView cImageView; CircularProgressBar progressBar;
    RelativeLayout relativeLayout;
    LinearLayoutManager manager; ArrayList<String> checklist = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;
    int whitecolor,accentcolor; private Handler handler = new Handler();
    private Runnable runnable; ItemTouchHelper.Callback callback;
    private static final String TAG = "NPlaylistActivity"; boolean preloaded=false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nplaylist);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(" ");

        progressBar = findViewById(R.id.progressBar);

        whitecolor = ContextCompat.getColor(this,R.color.white);
        accentcolor = ContextCompat.getColor(this,R.color.colorAccent);
        cTitle = findViewById(R.id.cTitle);
        relativeLayout = findViewById(R.id.relativeLayout);
        removeFromQueue = findViewById(R.id.removeFromQueue);
        cAuthor = findViewById(R.id.cAuthor);
        recyclerView = findViewById(R.id.my_recycler_view);
        cImageView = findViewById(R.id.cImage);
        models = new ArrayList<>();

        // Set current song...

        cTitle.setText(MusicService.videoTitle);
        cAuthor.setText(MusicService.channelTitle);
        cImageView.setImageBitmap(MusicService.bitmapIcon);

        // Set recycler view...

        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        // Set Data and set which one is playing right now...

        adapter = new NPlayAdapter(models,NPlaylistActivity.this, this);

        setAdapterClicks();

        recyclerView.setAdapter(adapter);

        removeFromQueue.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    TextView view = (TextView ) v;
                    view.setTextColor(whitecolor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    boolean isCurrentOne=false;
                    if (checklist.size()>0) {
                        Integer[] pos = new Integer[checklist.size()];
                        int j=-1;
                        // Multi-remove
                        for (int i=0;i<checklist.size();i++) {
                            String val = checklist.get(i);
                            pos[++j] = Integer.parseInt(val.split("=")[1]);
                        }
                        // Reversing list...
                        Arrays.sort(pos, Collections.reverseOrder());
                        for (int c : pos) {
                            if (MusicService.ytIndex==c) {
                                isCurrentOne=true;
                            }
                            removeItem(c);
                        }
                        reloadAdapter();

                        checklist.clear();
                        setCheckedCallbacks();

                        if (isCurrentOne) {
                            if (MusicService.yturls.size()>0) {
                                MainActivity.PlayVideo(YTutils.convertListToArrayMethod(MusicService.yturls),0);
                            }else closePlayer();
                        }

                        if (MusicService.yturls.size()<=0) closePlayer();
                    }

                case MotionEvent.ACTION_CANCEL: {
                    TextView view = (TextView) v;
                    view.setTextColor(accentcolor);
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //Blank...
                return false;
            }
        });

        recyclerView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                return false;
            }
        });


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: LocalPlayBack: "+MusicService.localPlayBack);
                if (!MusicService.videoTitle.equals(cTitle.getText().toString())) {
                    cTitle.setText(MusicService.videoTitle);
                }

                if  (!MusicService.channelTitle.equals(cAuthor.getText().toString())) {
                    Log.e(TAG, "run: Beginning of the end" );
                    cAuthor.setText(MusicService.channelTitle);
                    //cImageView.setColorFilter(ContextCompat.getColor(NPlaylistActivity.this,R.color.black));
                    Bitmap localBitMap = YTutils.drawableToBitmap(ContextCompat.getDrawable(NPlaylistActivity.this,R.drawable.ic_pulse));
                    if (MusicService.bitmapIcon.sameAs(localBitMap))
                        cImageView.setColorFilter(ContextCompat.getColor(NPlaylistActivity.this,R.color.black));
                    else {
                        cImageView.clearColorFilter();
                        cImageView.setImageBitmap(MusicService.bitmapIcon);
                    }

                    for (NPlayModel model : models) {

                        /** For local playback stuff */
                        if (MusicService.localPlayBack) {
                            //TODO: Remove this color filter when you find a suitable offline image
                         //   cImageView.setColorFilter(ContextCompat.getColor(NPlaylistActivity.this,R.color.black));
                            if (MusicService.videoID.equals(model.getUrl()))
                                model.set_playing(true);
                            else model.set_playing(false);
                            continue;
                        }

                        if (YTutils.getVideoID(model.getUrl()).equals(MusicService.videoID)) {
                            model.set_playing(true);
                        }else model.set_playing(false);
                    }

                    adapter.notifyDataSetChanged();
                }
                handler.postDelayed(this, 2000);
            }
        };

        handler.postDelayed(runnable, 2000);



    }

    void closePlayer() {
        MusicService.onClear();
        MainActivity.bottom_player.setVisibility(View.GONE);
        MusicService.notificationManagerCompat.cancel(1);
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    void enabled() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        recyclerView.setVisibility(View.GONE);
    }

    void disabled() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onStart() {
        super.onStart();
        if (MusicService.yturls.size()>0) {
            models.clear();
            // Check for old data....
            Log.e(TAG, "onStart: Data Size: "+MusicService.nPlayModels.size()+", YTUrl size: "+MusicService.yturls.size() );
            if (MusicService.nPlayModels.size()>0 && MusicService.yturls.size() == MusicService.nPlayModels.size()) {
                new AsyncTask<Void,Float,Void>() {
                    boolean sameData=true;

                    @Override
                    protected void onPreExecute() {
                        enabled();
                        super.onPreExecute();
                    }

                    @Override
                    protected void onProgressUpdate(Float... values) {
                        super.onProgressUpdate(values);
                        progressBar.setProgress(values[0]);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        final int totalSize = MusicService.nPlayModels.size();
                        for(int i=0;i<MusicService.nPlayModels.size();i++) {
                            MusicService.nPlayModels.get(i).set_playing(false);
                            String yturl = MusicService.yturls.get(i);
                            String nurl = MusicService.nPlayModels.get(i).getUrl();

                            String videoID;

                            /** For local playback stuff */
                            MetaModel metaData = MusicService.nPlayModels.get(i).getModel().getVideMeta();
                            if (MusicService.localPlayBack)
                                videoID = MusicService.nPlayModels.get(i).getUrl();
                           /* else if (metaData.getVideoID()!=null && metaData.getVideoID().contains("soundcloud.com")) videoID = metaData.getVideoID();
                          */  else videoID = metaData.getVideoID();

                            Log.e(TAG, "doInBackground: MainId: "+MusicService.videoID+", LocalId: "+videoID );
                            if (MusicService.videoID.equals(videoID)) {
                                Log.e(TAG, "doInBackground: This is playing..." );
                                MusicService.nPlayModels.get(i).set_playing(true);
                            }

                            publishProgress((float)((float)i*(float)100.0/(float)(totalSize)));

                            if (!yturl.equals(nurl)) {
                                sameData = false;
                                return null;
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        disabled();
                        if (sameData) {
                            reloadAdapter();
                            preloaded=true;
                            return;
                        }
                        runCommonTask();
                    }
                }.execute();
            }else runCommonTask();
        }else runCommonTask();

    }


    void runCommonTask() {
        if (MusicService.localPlayBack)
            new getAllOfflineData(this).execute();
        else {
            new getData(this).execute();
        }
    }

    class getAllOfflineData extends AsyncTask<Void,Float,Void> {
        Context context;

        public getAllOfflineData(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enabled();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            disabled();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (MusicService.localPlayBack) {
                int i=0;
                final int totalSize = MusicService.yturls.size();
                for (String url: MusicService.yturls) {
                    File f = new File(url);
                    Uri uri = Uri.fromFile(f);
                    try {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(NPlaylistActivity.this,uri);
                        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                        byte [] data = mmr.getEmbeddedPicture();

                        Bitmap icon;

                        if(data != null)
                            icon = BitmapFactory.decodeByteArray(data, 0, data.length);
                        else
                            icon = YTutils.drawableToBitmap(ContextCompat.getDrawable(NPlaylistActivity.this,
                                    R.drawable.ic_pulse));

                        if (artist==null) artist ="Unknown artist";
                        if (title==null) title = YTutils.getVideoTitle(f.getName());

                        if (title.contains("."))
                            title = title.split("\\.")[0];

                        MetaModel model = new MetaModel(url,title,artist,null);
                        YTMeta meta = new YTMeta(model);
                        if (MusicService.videoID.equals(url))
                            models.add(new NPlayModel(url,meta,true));
                        else  models.add(new NPlayModel(url,meta,false));

                        publishProgress((float)((float)i*(float)100.0/(float)(totalSize)));

                        models.get(models.size()-1).setIcon(icon);
                        i++;

                    }catch (Exception e) {
                        // TODO: Do something when cannot played...
                    }
                }
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        PlayerActivity2.setViewPagerData();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    void reloadAdapter() {
        models = new ArrayList<>(MusicService.nPlayModels);
        adapter = new NPlayAdapter(models,this, this);
        setAdapterClicks();
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    void setAdapterClicks() {

        callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.setOnSingleClickListener((view, position ,model, holder) -> {
            // Remove current queue song and make it current...

            for(int i=0;i<models.size();i++) {
                models.get(i).set_playing(false);
            }
            models.get(position).set_playing(true);

            /** For local playback stuff */
            if (!MusicService.localPlayBack)
                MainActivity.ChangeVideo(position);
            else MainActivity.ChangeVideoOffline(position);
            adapter.notifyDataSetChanged();
        });

        adapter.setOnCheckClickListener((view, position, model, holder) -> {
            CheckBox checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                checklist.add("value="+position);
                setCheckedCallbacks();
            }else{
                checklist.remove("value="+position);
                setCheckedCallbacks();
            }
        });
    }


    void setCheckedCallbacks() {
        if (checklist.size()>0) {
            relativeLayout.setVisibility(View.VISIBLE);
        }else relativeLayout.setVisibility(View.GONE);
    }

    void removeItem(int position) {
      //  models.remove(position);
       try {
           if (MusicService.videoID.equals(MusicService.yturls.get(position))) {
               Toast.makeText(this, "Cannot remove currently playing song!", Toast.LENGTH_SHORT).show();
               return;
           }
           MusicService.nPlayModels.remove(position);
           MusicService.yturls.remove(position);
       }catch (Exception e) {
           e.printStackTrace();
           Log.e(TAG, "removeItem: Error: "+e.getMessage() );
       }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
        checklist.clear();
        setCheckedCallbacks();
    }

    class getData extends AsyncTask<Void,Float,Void> {

         Context context; YTMeta meta;

        public getData(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enabled();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            disabled();
            if (models.size()>0) {
                adapter.notifyDataSetChanged();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int i=0;
            final int totalSize = MusicService.yturls.size();
            for (String url: MusicService.yturls) {
                Log.e(TAG, "doInBackground: NPlayListActivity: " + url);

                meta = new YTMeta(YTutils.getVideoID(url));
                if (meta.getVideMeta() != null) {
                    if (meta.getVideMeta().getVideoID().equals(MusicService.videoID)) {
                        models.add(new NPlayModel(url, meta, true));
                    } else
                        models.add(new NPlayModel(url, meta, false));
                }
                publishProgress((float) ((float) i * (float) 100.0 / (float) (totalSize)));
                i++;
            }
            return null;
        }
    }

    class setCurrentData extends AsyncTask<Void,Void,Void> {

        String yturl;
        YTMeta meta;

        public setCurrentData(String yturl) {
            this.yturl = yturl;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (MusicService.localPlayBack) {

            }
            if (meta.getVideMeta()!=null && !MusicService.localPlayBack) {
                cTitle.setText(YTutils.getVideoTitle(meta.getVideMeta().getTitle()));
                cAuthor.setText(YTutils.getChannelTitle(meta.getVideMeta().getTitle(),meta.getVideMeta().getAuthor()));
                Glide.with(NPlaylistActivity.this)
                        .asBitmap()
                        .load(meta.getVideMeta().getImgUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Palette.generateAsync(resource, palette -> {
                                    cImageView.setImageBitmap(resource);

                                    MusicService.bitmapIcon = resource;
                                    MusicService.nColor = palette.getVibrantColor(NPlaylistActivity.this
                                            .getResources().getColor(R.color.light_white));
                                    Log.e(TAG, "setCurrentData: Changing nColor: "+MusicService.nColor +
                                            ", ImageUri: "+MusicService.imgUrl );
                                    MusicService.rebuildNotification();
                                });

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            meta = new YTMeta(YTutils.getVideoID(yturl));
            return null;
        }
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}