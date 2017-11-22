package xyz.jienan.pushpull.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import xyz.jienan.pushpull.DateUtils;
import xyz.jienan.pushpull.R;
import xyz.jienan.pushpull.ToastUtils;
import xyz.jienan.pushpull.network.MemoEntity;

/**
 * Created by Jienan on 2017/11/1.
 */

public class PushPullAdapter extends RecyclerView.Adapter<PushPullAdapter.ViewHolder> implements TouchHelperAdapter {

    private final static String MEMO_KEY = "memo_key";
    private ClipboardManager clipboard;
    private List<MemoEntity> mList;
    private Context mContext;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private RecyclerView recyclerView;
    private ItemInteractionCallback mCallback;

    PushPullAdapter(Context context, ItemInteractionCallback itemInteractionCallback) {
        mList = new LinkedList<MemoEntity>();
        mContext = context;
        if (clipboard == null) {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        gson = new Gson();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Memo", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(MEMO_KEY, null);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String item = a.optString(i);
                    MemoEntity entity = gson.fromJson(item, MemoEntity.class);
                    if (entity != null)
                        mList.add(entity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        editor = sharedPreferences.edit();
        mCallback = itemInteractionCallback;
    }

    public void onItemMove(int from, int to) {
        Collections.swap(mList, from, to);
        notifyItemMoved(from, to);
        saveToPreference();
    }

    @Override
    public void onItemDismiss(int position) {
        cache = mList.get(position);
        mList.remove(position);
        notifyItemRemoved(position);
        saveToPreference();
        mCallback.onDismiss(position);
    }

    private MemoEntity cache;

    public void undoItemDismiss(int position) {
        mList.add(position, cache);
        notifyItemInserted(position);
        saveToPreference();
    }

    public void expireItems() {
        Iterator iterator = mList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            MemoEntity entity = (MemoEntity) iterator.next();
            if (entity.hasExpired && !entity.createdFromPush) {
                iterator.remove();
                i++;
            }
        }
        if (i == 1) {
            ToastUtils.showToast(mContext, "One item has been removed from list");
        } else if (i > 1) {
            ToastUtils.showToast(mContext, i + " items have been removed from list");
        }

        notifyDataSetChanged();
        saveToPreference();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvId;
        final TextView tvMsg;
        final ImageView ivAction;
        final ImageView ivNotice;
        final View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tvId = itemView.findViewById(R.id.tv_push_short_id);
            tvMsg = itemView.findViewById(R.id.tv_push_content);
            ivAction = itemView.findViewById(R.id.iv_action);
            ivNotice = itemView.findViewById(R.id.iv_notice);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MemoEntity memo = mList.get(position);
        holder.tvId.setText(memo.getId());
        holder.tvMsg.setText(memo.getMsg());
        long lifeSpan = 0;
        if (!TextUtils.isEmpty(memo.getExpiredOn())) {
            lifeSpan = DateUtils.getTimeDiffFromNow(memo.getExpiredOn());
            if (lifeSpan < 0) {
                memo.hasExpired = true;
            }
        }
        holder.ivAction.setEnabled(!memo.hasExpired);
        holder.ivAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("id",memo.getId());
                clipboard.setPrimaryClip(clip);
                ToastUtils.showToast(mContext, "id copied to clipboard");
            }
        });

        if (!memo.createdFromPush && memo.hasExpired) {
            setItemExpiredView(holder);
        }

        if (lifeSpan > 0 && lifeSpan <= 1000 * 60 * 5){
            holder.ivNotice.setVisibility(View.VISIBLE);
        } else {
            holder.ivNotice.setVisibility(View.GONE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long lifeSpanInClick = 0;
                if (!TextUtils.isEmpty(memo.getExpiredOn())) {
                    lifeSpanInClick = DateUtils.getTimeDiffFromNow(memo.getExpiredOn());
                    if (lifeSpanInClick < 0) {
                        memo.hasExpired = true;
                    }
                }
                if (!memo.createdFromPush && memo.hasExpired) {
                    ToastUtils.showToast(mContext, "This memo has been expired");
                    setItemExpiredView(holder);
                } else {
                    mCallback.onClick(memo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addMemo(MemoEntity memo) {
        if (mList.contains(memo)) {
            int index = mList.indexOf(memo);
            Collections.swap(mList, index, 0);
            this.notifyItemMoved(index, 0);
            ToastUtils.showToast(mContext, "item already in list");
        } else {
            mList.add(0, memo);
            notifyItemInserted(0);
        }
        recyclerView.scrollToPosition(0);
        saveToPreference();
    }

    public boolean checkExistMeme(String id) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getId().equals(id)) {
                ToastUtils.showToast(mContext, "item already in list");
                recyclerView.scrollToPosition(i);
                return true;
            }
        }
        return false;
    }

    private void saveToPreference() {
        JSONArray a = new JSONArray();
        for (MemoEntity entity : mList) {
            a.put(gson.toJson(entity));
        }

        editor.putString(MEMO_KEY, a.toString());
        editor.commit();
    }

    private void setItemExpiredView(ViewHolder holder) {
        holder.ivNotice.setVisibility(View.GONE);
        holder.ivAction.setEnabled(false);
        holder.tvMsg.setPaintFlags(holder.tvMsg.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
