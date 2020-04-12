package com.mc.onmusic_relase.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mc.onmusic_relase.AppSettings;
import com.mc.onmusic_relase.MainActivity;
import com.mc.onmusic_relase.R;
import com.mc.onmusic_relase.models.DiscoverModel;
import com.mc.onmusic_relase.models.MetaModel;
import com.mc.onmusic_relase.models.NPlayModel;
import com.mc.onmusic_relase.services.MusicService;
import com.mc.onmusic_relase.utils.YTLength;
import com.mc.onmusic_relase.utils.YTMeta;
import com.mc.onmusic_relase.utils.YTSearch;
import com.mc.onmusic_relase.utils.YTutils;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<DiscoverModel> dataSet;
    private ArrayList<String> yturls;
    Context con; boolean CP_ADAPTER, O_PLAYLIST;
    View.OnClickListener playlistListener;
    View.OnClickListener cplaylistener;boolean isSearchAdpater;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView AuthorText;
        ImageView imageView, removeButton;
        ConstraintLayout mainLayout; View disabledView;
        LinearLayout adLayout;
        AdView adView;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.aTitle);
            this.disabledView = itemView.findViewById(R.id.disabledView);
            this.removeButton = itemView.findViewById(R.id.removeButton);
            this.AuthorText = itemView.findViewById(R.id.aAuthor);
            this.imageView = itemView.findViewById(R.id.aImage);
            this.mainLayout = itemView.findViewById(R.id.mainlayout);
            this.adLayout = itemView.findViewById(R.id.adViewLayout);
            this.adView = itemView.findViewById(R.id.adView);
        }
    }

    public SongAdapter(ArrayList<DiscoverModel> data, Context context) {
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (DiscoverModel model: data)
            yturls.add(0,model.getYtUrl());
    }

    public SongAdapter(boolean isSearchAdapter, ArrayList<DiscoverModel> data, Context context) {
        this.isSearchAdpater = isSearchAdapter;
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (DiscoverModel model: data)
            yturls.add(0,model.getYtUrl());
    }

    public SongAdapter(ArrayList<DiscoverModel> data, Context context,boolean iscpAdapter,boolean isOPlaylist,View.OnClickListener cplaylistListener) {
        CP_ADAPTER = iscpAdapter;
        this.cplaylistener = cplaylistListener;
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (DiscoverModel model: data)
            yturls.add(0,model.getYtUrl());
    }

    public SongAdapter(ArrayList<DiscoverModel> data, Context context, boolean isOPlaylist, View.OnClickListener oplaylistListener) {
        O_PLAYLIST = isOPlaylist;
        this.dataSet = data;
      //  this.playlistListener = oplaylistListener;
        this.con = context;
        yturls = new ArrayList<>();
        for (DiscoverModel model: data)
            yturls.add(0,model.getYtUrl());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @SuppressLint({"ClickableViewAccessibility", "StaticFieldLeak"})
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final DiscoverModel discoverModel = dataSet.get(listPosition);

        if (isSearchAdpater) {
            holder.titleText.setText(discoverModel.getTitle().replace("&amp;","&"));
            holder.AuthorText.setText(discoverModel.getAuthor());
        }else {
            holder.titleText.setText(YTutils.getVideoTitle(discoverModel.getTitle()));
            holder.AuthorText.setText(YTutils.getChannelTitle(discoverModel.getTitle(),discoverModel.getAuthor()));
        }

        Glide.with(con).load(discoverModel.getImgUrl()).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.imageView.setImageDrawable(resource);
                return true;
            }
        }).into(holder.imageView);

        if (!CP_ADAPTER) {


            int greyColor = ContextCompat.getColor(con,R.color.grey);
            int white = ContextCompat.getColor(con,R.color.white);
            int light_white = ContextCompat.getColor(con,R.color.light_white);
            if (discoverModel.isDisabled()) {
                holder.removeButton.setColorFilter(ContextCompat.getColor(con,android.R.color.holo_red_dark));
                holder.disabledView.setVisibility(View.VISIBLE);
                holder.titleText.setTextColor(greyColor);
                holder.AuthorText.setTextColor(greyColor);
            }else {
                holder.removeButton.clearColorFilter();
                holder.disabledView.setVisibility(View.GONE);
                holder.titleText.setTextColor(white);
                holder.AuthorText.setTextColor(light_white);
            }

            holder.removeButton.setOnClickListener(view -> {
                //TODO: Update logic for soundcloud links...

                final String ytID = YTutils.getVideoID(discoverModel.getYtUrl());

                if (discoverModel.isDisabled()) {
                    dataSet.get(listPosition).setDisabled(false);

                    String data = YTutils.readContent((Activity) con,"removedList.csv");
                    if (data!=null && !data.isEmpty()) {
                        if (data.contains(",")) {
                            String[] items = data.split(",");

                            StringBuilder builder = new StringBuilder();
                            for (String item : items) {
                                if (!item.contains(ytID))
                                    builder.append(item).append(",");
                            }
                            String content = builder.deleteCharAt(builder.length()-1).toString();

                            YTutils.writeContent((Activity)con,"removedList.csv",content.trim());

                        }else YTutils.writeContent((Activity)con,"removedList.csv","");
                    }

                    notifyItemChanged(listPosition);
                }else {
                    dataSet.get(listPosition).setDisabled(true);
                    String wr = "ytID:"+ytID;
                    if (ytID.contains("soundcloud.com"))
                        wr = "sd:"+ytID;
                    String data = YTutils.readContent(con,"removedList.csv");
                    if (data!=null && !data.isEmpty()) {
                        YTutils.writeContent((Activity)con,"removedList.csv",data.trim()+","+wr);
                    }else  YTutils.writeContent((Activity)con,"removedList.csv",wr);

                    notifyItemChanged(listPosition);

                    if (MusicService.yturls.size()>0 && !MusicService.localPlayBack) {
                        if (MusicService.videoID.contains(ytID))
                           MusicService.playNext();
                    }

                    Toast.makeText(con, "Song is hidden in all list!", Toast.LENGTH_SHORT).show();
                }
            });

            holder.mainLayout.setOnLongClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(con,v);
                popupMenu.inflate(R.menu.song_popup_menu);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_add_playlist:
                            if (discoverModel.getSeconds()<=0) {
                                ProgressDialog dialog = new ProgressDialog(con);
                                dialog.setMessage("Processing...");
                                dialog.show();
                                new AsyncTask<Void,Void,Void>() {
                                    YTLength length;
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        length = new YTLength(YTutils.getVideoID(discoverModel.getYtUrl()));
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        dialog.dismiss();
                                        if (length.getSeconds()>0) {
                                            YTutils.addToPlayList(con,YTutils.getVideoID(discoverModel.getYtUrl()),discoverModel.getTitle(),discoverModel.getAuthor(),
                                                    discoverModel.getImgUrl(),length.getSeconds());
                                        }else
                                            Toast.makeText(con, "Error: Generating playlist!", Toast.LENGTH_SHORT).show();
                                        super.onPostExecute(aVoid);
                                    }
                                }.execute();
                            }else
                                YTutils.addToPlayList(con,YTutils.getVideoID(discoverModel.getYtUrl()),discoverModel.getTitle(),discoverModel.getAuthor(),
                                    discoverModel.getImgUrl(),discoverModel.getSeconds());
                            break;
                        case R.id.action_add_queue:
                            if (MusicService.yturls.size()>0 && !MusicService.localPlayBack) {
                                final String ytUri = YTutils.getVideoID(discoverModel.getYtUrl());
                                boolean toAdd=true;
                                for (String url: MusicService.yturls) {
                                    if (YTutils.getVideoID(url).equals(ytUri)) {
                                        toAdd=false;
                                        Toast.makeText(con, "Song exists in queue!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                if (toAdd) {
                                    if (MusicService.nPlayModels.size()>0 && MusicService.nPlayModels.size()== MusicService.yturls.size()) {
                                        MetaModel metaModel = new MetaModel(YTutils.getVideoID(discoverModel.getYtUrl()),discoverModel.getTitle()
                                                ,discoverModel.getAuthor(),discoverModel.getImgUrl());
                                        NPlayModel nPlayModel = new NPlayModel(discoverModel.getYtUrl(),new YTMeta(metaModel),false);
                                        MusicService.nPlayModels.add(nPlayModel);
                                    }
                                    MusicService.yturls.add(discoverModel.getYtUrl());
                                    Toast.makeText(con, "Song queue updated!", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                holder.mainLayout.performClick();
                            }
                            break;
                        case R.id.action_download:
                            MetaModel model = new MetaModel(YTutils.getVideoID(discoverModel.getYtUrl()),
                                    discoverModel.getTitle(),discoverModel.getAuthor(),discoverModel.getImgUrl());
                            YTutils.downloadDialog(con,model);
                            break;
                        case R.id.action_share:
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,discoverModel.getYtUrl());
                            con.startActivity(Intent.createChooser(shareIntent, "Share"));
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            /*if (discoverModel.getYtUrl()!=null) {
                ClipboardManager clipboard = (ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", discoverModel.getYtUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(con, "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
            }*/
                return true;
            });

            holder.mainLayout.setOnClickListener(v -> {
                if (discoverModel.isDisabled()) return;

                MusicService.nPlayModels.clear();
                ArrayList<String> yturls = new ArrayList<>();

                for (DiscoverModel dModel : dataSet) {

                    MetaModel metaModel = new MetaModel(
                            YTutils.getVideoID(dModel.getYtUrl()),
                            dModel.getTitle(),
                            dModel.getAuthor(),
                            dModel.getImgUrl()
                    );
                    NPlayModel nPlayModel = new NPlayModel(dModel.getYtUrl(),new YTMeta(metaModel),false);

                    MusicService.nPlayModels.add(nPlayModel);

                    yturls.add(dModel.getYtUrl());
                }
                MainActivity.PlayVideo(YTutils.convertListToArrayMethod(yturls),listPosition);

                ((InputMethodManager) con.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(holder.mainLayout.getWindowToken(), 0);
            });

            if (listPosition % 15 == 0 && listPosition !=0 && AppSettings.showAds) {
                // Load ads on 5,15,25...
                Log.e("ShowingAds","pos: "+listPosition);
                holder.adLayout.setVisibility(VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
                holder.adView.loadAd(adRequest);
            }else {
                holder.adLayout.setVisibility(GONE);
            }
        }

       /* if (O_PLAYLIST) {
            holder.mainLayout.setTag(listPosition);
            holder.mainLayout.setOnClickListener(playlistListener);
        }*/


        if (CP_ADAPTER) {
            holder.removeButton.setVisibility(View.GONE);
            holder.mainLayout.setTag(listPosition);
            holder.mainLayout.setOnClickListener(cplaylistener);
        }
    }


    public class layoutListener extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private Activity activity;
        private DiscoverModel discoverModel;
        private String videoId;

        public layoutListener(Activity activity, DiscoverModel model) {
            this.activity = activity;
            this.discoverModel = model;
            dialog = new ProgressDialog(activity);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Parsing Url...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            RunLink("https://www.youtube.com/watch?v="+videoId,activity);
            super.onPostExecute(aVoid);
        }

        protected Void doInBackground(Void... voids) {
            String search_text = discoverModel.getTitle()
                    + "+by+" + discoverModel.getAuthor();

            YTSearch ytSearch = new YTSearch(search_text);

            videoId = ytSearch.getVideoIDs().get(0);

            return null;
        }
    }

    void RunLink(String link, Activity activity) {
       /* Intent intent = new Intent(con,PlayerActivity.class);
        intent.putExtra("youtubelink",new String[] {link});
        con.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);*/
        MainActivity.PlayVideo(new String[]{ link });
    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}
