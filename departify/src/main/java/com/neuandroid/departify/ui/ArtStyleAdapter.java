package com.neuandroid.departify.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.neuandroid.departify.R;
import com.neuandroid.departify.model.Style;
import com.neuandroid.departify.model.Styles;

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


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivStyle;
        private TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            ivStyle = (ImageView) itemView.findViewById(R.id.iv_style);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ivStyle == null) {
                        return;
                    }
                    mClickListener.onClick(mStyles.getStyles().get(getAdapterPosition()).getId());
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
        Style style = mStyles.getStyles().get(position);
        String imageUrl = style.getUrl();
        Glide.with(mContext).load(imageUrl).placeholder(R.mipmap.ic_launcher).centerCrop().crossFade().into(holder.ivStyle);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean shouldShowTitle = sharedPreferences.getBoolean("pref_title", false);
        if (shouldShowTitle) {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(style.getTitle());
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        int size = getItemCount();
        if (size % 2 == 1 && position == (size - 1)){
            return 2;
        }
        return 1;
    }


    @Override
    public int getItemCount() {
        return mStyles == null ? 0 : mStyles.getStyles().size();
    }

    public interface IClickListener {
        void onClick(String styleId);
    }

}
