package com.mc.onmusic_relase.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mc.onmusic_relase.R;
import com.mc.onmusic_relase.helper.ItemTouchHelperAdapter;
import com.mc.onmusic_relase.helper.ItemTouchHelperViewHolder;
import com.mc.onmusic_relase.helper.OnStartDragListener;
import com.mc.onmusic_relase.models.NPlayModel;
import com.mc.onmusic_relase.services.MusicService;
import com.mc.onmusic_relase.utils.EqualizerView;
import com.mc.onmusic_relase.utils.YTMeta;
import com.mc.onmusic_relase.utils.YTutils;

import java.util.ArrayList;
import java.util.Collections;

public class NPlayAdapter extends RecyclerView.Adapter<NPlayAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<NPlayModel> models;
    Context con;
    OnClickListener onClickListener;
    OnCheckBoxListener onCheckBoxListener;
    private OnStartDragListener mDragStartListener;
    int accentColor;
    private static final String TAG = "NPlayAdapter";

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
       try {
           Collections.swap(models, fromPosition, toPosition);
           notifyItemMoved(fromPosition, toPosition);
       }catch (Exception e){e.printStackTrace();}
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public boolean onItemMoved(int fromPosition, int toPosition) {
        Log.e("onItemMoved","true");
        MusicService.nPlayModels = new ArrayList<>(models);
        MusicService.yturls.clear();
        for (int i=0;i<models.size();i++){
            MusicService.yturls.add(models.get(i).getUrl());
            if (models.get(i).is_playing()) {
                MusicService.ytIndex = i;
                Log.e(TAG, "onItemMoved: Current Index: "+i);
            }
            models.get(i).set_selected(false);
        }
        notifyDataSetChanged();
        return true;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        TextView titleText, authorText;
        ImageButton moveButton;
        ConstraintLayout layout;
        CheckBox checkBox; EqualizerView equalizerView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.aTitle);
            this.authorText = itemView.findViewById(R.id.aAuthor);
            this.moveButton = itemView.findViewById(R.id.aMoveButton);
            this.layout = itemView.findViewById(R.id.mainlayout);
            this.checkBox = itemView.findViewById(R.id.aCheckBox);
            this.equalizerView = itemView.findViewById(R.id.equalizer);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onItemMoved(int from, int to) {
           // NPlaylistActivity.performSwap(from,to);
            Log.e("ItemSwappedComplete","from: "+from+", to: "+to);
        }

    }



    public NPlayAdapter(ArrayList<NPlayModel> data, Context context, OnStartDragListener dragStartListener) {
        this.con = context;
        this.models = data;
        this.mDragStartListener = dragStartListener;
        accentColor = ContextCompat.getColor(con,R.color.colorAccent);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nplaylist_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final NPlayModel nPlayModel = models.get(listPosition);

        if (nPlayModel.is_playing()) {
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.equalizerView.animateBars();
        }else {
            holder.equalizerView.setVisibility(View.INVISIBLE);
            holder.equalizerView.stopBars();
        }

        if (nPlayModel.is_selected()) {
            holder.checkBox.setChecked(true);
        }else holder.checkBox.setChecked(false);

        YTMeta meta = nPlayModel.getModel();
        if (meta.getVideMeta()!=null) {

            holder.moveButton.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                  try {
                      mDragStartListener.onStartDrag(holder);
                  }catch (Exception e){e.printStackTrace();}
                }
                return false;
            });

            holder.titleText.setText(YTutils.getVideoTitle(meta.getVideMeta().getTitle()));

            /** For local playback stuff */
            if (!MusicService.localPlayBack)
                holder.authorText.setText(YTutils.getChannelTitle(meta.getVideMeta().getTitle(),meta.getVideMeta().getAuthor()));
            else holder.authorText.setText(meta.getVideMeta().getAuthor());

            holder.checkBox.setOnClickListener(view -> {
                onCheckBoxListener.OnSingleClicked(holder.checkBox,listPosition,nPlayModel,holder);
            });

            holder.layout.setOnClickListener(v1->{
                onClickListener.OnSingleClicked(holder.layout,listPosition,nPlayModel,holder);
            });
        }

        if (listPosition+1==MusicService.yturls.size()) {
            MusicService.nPlayModels = new ArrayList<>(models);
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public interface OnClickListener {
        void OnSingleClicked(View view, int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
    }

    public interface OnCheckBoxListener {
        void OnSingleClicked(View view, int position, NPlayModel model, NPlayAdapter.MyViewHolder holder);
    }

    public void setOnCheckClickListener(OnCheckBoxListener listener) {
        onCheckBoxListener = listener;
    }

    public void setOnSingleClickListener(OnClickListener listener) {
        onClickListener = listener;
    }
}

