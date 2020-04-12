package com.mc.onmusic_relase.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.mc.onmusic_relase.utils.YTutils;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;

public class DiscoverAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<DiscoverModel> discoverModels;
    Context con; String csvString,intentTitle;View.OnLongClickListener longClickListener;
    private int totalItems;
    ArrayList<Integer> adNumbers = new ArrayList<>();
    private boolean loading;

    public DiscoverAdapter(Context context, List<DiscoverModel> list, RecyclerView recyclerView, View.OnLongClickListener longClickListener,
                           String data,String title,int maxSize) {
        discoverModels = list;
        this.con = context;
        this.csvString = data;
        this.intentTitle = title;
        this.longClickListener = longClickListener;
        this.totalItems = maxSize;
        /*if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    {
                        isScrolling = true;
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    currentItems = linearLayoutManager.getChildCount();
                    scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();
                    totalItems = linearLayoutManager.getItemCount();
                    if(!loading && isScrolling && (currentItems + scrollOutItems == totalItems))
                    {
                        isScrolling = false;
                        if (onLoadMoreListener != null) {
                         try {
                             onLoadMoreListener.onLoadMore();
                         }catch (Exception e) {e.printStackTrace();}
                        }
                        loading = true;
                    }

                   *//* LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                    if (!loading) {
                        if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() ==
                                discoverModels.size() - 1) {
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }*//*
                }
            });
        }*/
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return discoverModels.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        }catch (Exception e) {
            e.printStackTrace();
            return VIEW_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.history_item, parent, false);

        vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            final DiscoverModel model = discoverModels.get(position);

            final MyViewHolder viewHolder = ((MyViewHolder) holder);

            if (position==discoverModels.size()-1 && position!=totalItems-1) {
                viewHolder.progressBar.setVisibility(VISIBLE);
            }else viewHolder.progressBar.setVisibility(GONE);

            viewHolder.authorText.setText(model.getAuthor()+"");
            viewHolder.titleText.setText(model.getTitle());
            viewHolder.rate_percent.setText("#"+(position+1));

            Glide.with(con).asBitmap().load(model.getImgUrl()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    viewHolder.imageView.setImageBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });

            /*Glide.with(con).load(model.getImgUrl()).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    viewHolder.imageView.setImageDrawable(resource);
                    return true;
                }
            }).into(viewHolder.imageView);*/

            viewHolder.addPlaylist.setOnClickListener(v -> {
                Activity activity = (Activity) con;
                new getData(activity,model).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            });

            Object[] objects = new Object[5];
            objects[0]=position; objects[1]=model.getTitle();objects[2]=model.getYtUrl();objects[3]=model.getAuthor();
            objects[4]=model.getImgUrl();
            viewHolder.mainCard.setTag(objects);
            viewHolder.mainCard.setOnLongClickListener(longClickListener);


            viewHolder.mainCard.setOnClickListener(v -> {

                // A quick smart hack... to set Playlist models directly during loading...

                MusicService.nPlayModels.clear();
                String[] yturls = new String[discoverModels.size()];
                for (int i=0;i<yturls.length;i++) {
                    MetaModel metaModel = new MetaModel(
                            YTutils.getVideoID(discoverModels.get(i).getYtUrl()),
                            discoverModels.get(i).getTitle(),
                            discoverModels.get(i).getAuthor(),
                            discoverModels.get(i).getImgUrl()
                    );
                    NPlayModel nPlayModel = new NPlayModel(
                            discoverModels.get(i).getYtUrl(),
                            new YTMeta(metaModel),false);
                    MusicService.nPlayModels.add(nPlayModel);
                    yturls[i] = discoverModels.get(i).getYtUrl();
                }
                MainActivity.PlayVideo(yturls,position);
            });

            if (position%5==0 && position!=0 && position%10!=0 && AppSettings.showAds) {
                // Load ads on 5,15,25...
                Log.e("ShowingAds","pos: "+position);
                viewHolder.adLayout.setVisibility(VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
                viewHolder.adView.loadAd(adRequest);
            }else {
                viewHolder.adLayout.setVisibility(GONE);
            }
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    class getData extends AsyncTask<Void,Void,Void> {
        DiscoverModel model;
        long seconds; Activity activity; ProgressDialog dialog;
        public getData(Activity activity,DiscoverModel model) {
            this.activity = activity;
            this.model = model;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Parsing your playlist...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            YTutils.addToPlayList(activity,YTutils.getVideoID(model.getYtUrl())
                    ,model.getTitle(),model.getAuthor(),model.getImgUrl(),seconds);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            YTLength ytLength = new YTLength(YTutils.getVideoID(model.getYtUrl()));
            seconds = ytLength.getSeconds();
            return null;
        }
    }

    public void setLoaded() {
        loading = false;
    }

    public void setLoading() {
        loading = true;
    }

    public boolean getLoaded() {
        return loading;
    }

    @Override
    public int getItemCount() {
        return discoverModels.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView rate_percent;
        TextView titleText;
        TextView authorText;
        TextView dateText;
        ImageView imageView;
        LinearLayout dateLayout;
        LinearLayout addPlaylist;
        CardView mainCard;
        LinearLayout adLayout;
        AdView adView;
        ProgressBar progressBar;

        public MyViewHolder(View v) {
            super(v);
            this.rate_percent = itemView.findViewById(R.id.hRate_percent);
            this.titleText = itemView.findViewById(R.id.hTitle);
            this.authorText = itemView.findViewById(R.id.hAuthor);
            this.dateText = itemView.findViewById(R.id.hDate);
            this.imageView = itemView.findViewById(R.id.hImage);
            this.dateLayout = itemView.findViewById(R.id.hDate_layout);
            this.addPlaylist = itemView.findViewById(R.id.hAdd_playlist);
            this.mainCard = itemView.findViewById(R.id.cardView);
            this.adLayout = itemView.findViewById(R.id.adViewLayout);
            this.adView = itemView.findViewById(R.id.adView);
            this.progressBar = itemView.findViewById(R.id.LprogressBar);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }
}