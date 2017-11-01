package xyz.jienan.pushpull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.jienan.pushpull.network.MemoEntity;

/**
 * Created by Jienan on 2017/11/1.
 */

public class PushPullAdapter extends RecyclerView.Adapter<PushPullAdapter.ViewHolder> {


    private ClipboardManager clipboard;
    private List<MemoEntity> mList;
    private Context mContext;

    PushPullAdapter(Context context) {
        mList = new ArrayList<MemoEntity>();
        mContext = context;
        if (clipboard == null) {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvId;
        public final TextView tvMsg;
        public final ImageView ivCopy;

        public ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_push_short_id);
            tvMsg = itemView.findViewById(R.id.tv_push_content);
            ivCopy = itemView.findViewById(R.id.iv_copy);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MemoEntity memo = mList.get(position);
        holder.tvId.setText(memo.getId());
        holder.tvMsg.setText(memo.getMsg());
        holder.ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("id",memo.getId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "id copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addMemo(MemoEntity memo) {
        mList.add(memo);
        notifyDataSetChanged();
    }
}
