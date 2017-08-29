package com.neuandroid.departify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.deeparteffects.sdk.android.model.Styles;

/**
 * Created by jienanzhang on 12/07/2017.
 */

public class ArtStyleAdapter extends RecyclerView.Adapter<ArtStyleAdapter.ViewHolder> {

    private Styles mStyles;
    private Context mContext;
    private IClickListener mClickListener;

    public ArtStyleAdapter(Context context, Styles styles, IClickListener clickListener) {
        mContext = context;
        mStyles = styles;
        mClickListener = clickListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivStyle;

        public ViewHolder(View itemView) {
            super(itemView);
            ivStyle = (ImageView) itemView.findViewById(R.id.iv_style);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ivStyle == null) {
                        return;
                    }
                    mClickListener.onClick(mStyles.get(getAdapterPosition()).getId());
                }
            });
        }
    }

    public void updateStyles(Styles styles) {
        this.mStyles = styles;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_style, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imageUrl = mStyles.get(position).getUrl();
        Glide.with(mContext).load(imageUrl).centerCrop().crossFade().into(holder.ivStyle);

    }


    @Override
    public int getItemViewType(int position) {
        int size = getItemCount();
        if (size % 2 == 1 && position == 1){
            return 1;
        }
        return super.getItemViewType(position);
    }


    @Override
    public int getItemCount() {
        return mStyles == null ? 0 : mStyles.size();
    }

    public interface IClickListener {
        void onClick(String styleId);
    }

}
