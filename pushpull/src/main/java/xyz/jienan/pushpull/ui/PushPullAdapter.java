package xyz.jienan.pushpull.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import xyz.jienan.pushpull.DateUtils;
import xyz.jienan.pushpull.R;
import xyz.jienan.pushpull.ToastUtils;
import xyz.jienan.pushpull.network.MemoEntity;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_COPY;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PULLSH_HOST;

/**
 * Created by Jienan on 2017/11/1.
 */

public class PushPullAdapter extends RecyclerView.Adapter<PushPullAdapter.ViewHolder> implements TouchHelperAdapter {


    private ClipboardManager clipboard;
    private List<MemoEntity> mList;
    private Context mContext;
    private RecyclerView recyclerView;
    private ItemInteractionCallback mCallback;
    private Typeface fontMonaco;
    private SharedPreferences sharedPref;
    private List<MemoEntity> queryList = new LinkedList<MemoEntity>();

    private boolean inQueryMode = false;
    private Realm realm;


    PushPullAdapter(Context context, ItemInteractionCallback itemInteractionCallback) {
        mList = new LinkedList<MemoEntity>();
        mContext = context;
        if (clipboard == null) {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        if (fontMonaco == null) {
            fontMonaco = Typeface.createFromAsset(mContext.getAssets(), "Monaco.ttf");
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<MemoEntity> query = realm.where(MemoEntity.class);
        RealmResults<MemoEntity> results = query.sort("index").findAll();
        mList.addAll(results);
        mCallback = itemInteractionCallback;
    }

    @Override
    public boolean isActivate() {
        return !inQueryMode;
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
        final MemoEntity memo = inQueryMode ? queryList.get(position) : mList.get(position);
        holder.tvId.setText(memo.getId());
        holder.tvId.setTypeface(fontMonaco, Typeface.BOLD);
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
                ClipData clip;
                if (sharedPref.getBoolean(PREF_KEY_COPY, true)) {
                    String host = sharedPref.getString(PREF_KEY_PULLSH_HOST, "https://pullsh.me/");
                    clip = ClipData.newPlainText("url", host + memo.getId());
                    ToastUtils.showToast(mContext, "Share link copied to clipboard");
                } else {
                    clip = ClipData.newPlainText("id", memo.getId());
                    ToastUtils.showToast(mContext, "id copied to clipboard");
                }
                clipboard.setPrimaryClip(clip);
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
        Log.e("Memo", memo.getId() + " " + memo.index.get());
    }

    @Override
    public int getItemCount() {
        return inQueryMode ? queryList.size() : mList.size();
    }

    public void showQueryResult(List<MemoEntity> queryList) {
        this.queryList = queryList;
        inQueryMode = true;
        notifyDataSetChanged();
    }

    public void enterQueryMode() {
        this.inQueryMode = true;
    }

    public void leaveQueryMode() {
        inQueryMode = false;
        notifyDataSetChanged();
    }

    public void addMemo(MemoEntity memo) {
        if (mList.contains(memo)) {
            int index = mList.indexOf(memo);
            Collections.swap(mList, index, 0);
            this.notifyItemMoved(index, 0);
            ToastUtils.showToast(mContext, mContext.getString(R.string.toast_item_already_in_list));
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
                ToastUtils.showToast(mContext, mContext.getString(R.string.toast_item_already_in_list));
                recyclerView.scrollToPosition(i);
                return true;
            }
        }
        return false;
    }

    private void saveToPreference() {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmList<MemoEntity> realmList = new RealmList<MemoEntity>();
                int size = mList.size();
                for (int i = 0; i< size; i++) {
                    mList.get(i).index.set(i);
                }
                realmList.addAll(mList);
                realm.insertOrUpdate(realmList);
            }
        });
    }

    private void setItemExpiredView(ViewHolder holder) {
        holder.ivNotice.setVisibility(View.GONE);
        holder.ivAction.setEnabled(false);
        holder.tvMsg.setPaintFlags(holder.tvMsg.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
