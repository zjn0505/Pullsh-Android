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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

import static xyz.jienan.pushpull.base.Const.PREF_KEY_COPY_ICON;
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
        RealmResults<MemoEntity> results = query.sort("index").equalTo("createdFromPush", true)
                .or().beginGroup().equalTo("hasExpired", false).equalTo("createdFromPush", false).endGroup()
                .findAll();
        mList.addAll(results);
        mCallback = itemInteractionCallback;
    }

    @Override
    public boolean isActivate() {
        return !inQueryMode;
    }

    public void onItemMove(int from, int to) {
        MemoEntity memo = mList.get(from);
        mList.remove(from);
        mList.add(to, memo);
        notifyItemMoved(from, to);
        saveToPreference();
    }

    @Override
    public void onItemDismiss(int position) {
        MemoEntity entity = mList.get(position);
        cache = new MemoEntityCache(entity);
        mList.remove(position);
        notifyItemRemoved(position);
        removeFromPreference(entity.getId());
        mCallback.onDismiss(position);
    }

    private void fullClone(MemoEntity memoEntity) {

    }

    private MemoEntityCache cache;

    private class MemoEntityCache {

        String _id;
        String msg;
        int access_count;
        String created_date;
        int max_access_count;
        String expired_on;
        boolean hasExpired = false;
        boolean createdFromPush = false;
        String note;

        MemoEntityCache(MemoEntity memoEntity) {
            this._id = memoEntity.getId();
            this.msg = memoEntity.getMsg();
            this.access_count = memoEntity.getAccessCount();
            this.created_date = memoEntity.getCreatedDate();
            this.max_access_count = memoEntity.getMaxAccessCount();
            this.expired_on = memoEntity.getExpiredOn();
            this.hasExpired = memoEntity.hasExpired;
            this.createdFromPush = memoEntity.createdFromPush;
            this.note = memoEntity.getNote();
        }

        MemoEntity dump() {
            MemoEntity entity = new MemoEntity();
            entity.setId(_id);
            entity.setMsg(msg);
            entity.setAccessCount(access_count);
            entity.setCreatedDate(created_date);
            entity.setMaxAccessCount(max_access_count);
            entity.setExpiredOn(expired_on);
            entity.hasExpired = hasExpired;
            entity.createdFromPush = createdFromPush;
            entity.setNote(note);
            return entity;
        }
    }

    public void undoItemDismiss(int position) {
        MemoEntity entity = cache.dump();
        mList.add(position, entity);
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
                removeFromPreference(entity.getId());
                i++;
            }
        }
        if (i == 1) {
            ToastUtils.showToast(mContext, "One item has been removed from list");
        } else if (i > 1) {
            ToastUtils.showToast(mContext, i + " items have been removed from list");
        }

        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvId;
        final TextView tvMsg;
        final ImageView ivAction;
        final ImageView ivNotice;
        final TextView tvNote;
        final View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tvId = itemView.findViewById(R.id.tv_push_short_id);
            tvMsg = itemView.findViewById(R.id.tv_push_content);
            ivAction = itemView.findViewById(R.id.iv_action);
            ivNotice = itemView.findViewById(R.id.iv_notice);
            tvNote = itemView.findViewById(R.id.tv_memo_note);
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
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        final MemoEntity memo =
                inQueryMode ? queryList.get(position) : realm.where(MemoEntity.class).equalTo("_id", mList.get(position).getId()).findFirst();
        holder.tvId.setText(memo.getId());
        holder.tvId.setTypeface(fontMonaco, Typeface.BOLD);
        holder.tvMsg.setText(memo.getMsg());
        long lifeSpan = 0;
        if (!TextUtils.isEmpty(memo.getExpiredOn())) {
            lifeSpan = DateUtils.getTimeDiffFromNow(memo.getExpiredOn());
            if (lifeSpan < 0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        memo.hasExpired = true;
                        realm.insertOrUpdate(memo);
                    }
                });
            }
        }
        holder.ivAction.setEnabled(!memo.hasExpired);
        holder.ivAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip;
                if (sharedPref.getBoolean(PREF_KEY_COPY_ICON, true)) {
                    String host = sharedPref.getString(PREF_KEY_PULLSH_HOST, "https://pullsh.me/");
                    clip = ClipData.newPlainText("url", host + memo.getId());
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.toast_memo_link_copied));
                } else {
                    clip = ClipData.newPlainText("id", memo.getId());
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.toast_memo_id_copied));
                }
                clipboard.setPrimaryClip(clip);
            }
        });

        if (!memo.createdFromPush && memo.hasExpired) {
            setItemExpiredView(holder, true);
        } else {
            setItemExpiredView(holder, false);
        }

        if (lifeSpan > 0 && lifeSpan <= 1000 * 60 * 5){
            holder.ivNotice.setVisibility(View.VISIBLE);
            holder.ivNotice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double min = Math.ceil(DateUtils.getTimeDiffFromNow(memo.getExpiredOn()) / 1000.0 / 60);
                    min = min < 0 ?  1 : min;
                    ToastUtils.showToast(mContext, mContext.getResources().getQuantityString(R.plurals.toast_expire_notice, 1, (int)min));
                }
            });
        } else {
            holder.ivNotice.setVisibility(View.GONE);
        }
        String note = memo.getNote();
        if (!TextUtils.isEmpty(note)) {
            holder.tvNote.setText(String.format("- %s", note));
        } else {
            holder.tvNote.setText("");
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long lifeSpanInClick = 0;
                if (!TextUtils.isEmpty(memo.getExpiredOn())) {
                    lifeSpanInClick = DateUtils.getTimeDiffFromNow(memo.getExpiredOn());
                    if (lifeSpanInClick < 0 && !memo.hasExpired) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                memo.hasExpired = true;
                                realm.insertOrUpdate(memo);
                            }
                        });
                    }
                }
                if (!memo.createdFromPush && memo.hasExpired) {
                    ToastUtils.showToast(mContext, mContext.getString(R.string.toast_expired_notice));
                    setItemExpiredView(holder, true);
                } else {
                    mCallback.onClick(memo);
                }
            }
        });
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

    private void removeFromPreference(final String memoId) {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                MemoEntity result = realm.where(MemoEntity.class).equalTo("_id", memoId).findFirst();
                if (result != null) {
                    result.deleteFromRealm();
                }
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

    private void setItemExpiredView(ViewHolder holder, boolean expired) {
        if (expired) {
            holder.ivNotice.setVisibility(View.GONE);
            holder.ivAction.setEnabled(false);
            holder.tvMsg.setPaintFlags(holder.tvMsg.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.ivNotice.setVisibility(View.VISIBLE);
            holder.ivAction.setEnabled(true);
            holder.tvMsg.setPaintFlags(0);
        }

    }
}
