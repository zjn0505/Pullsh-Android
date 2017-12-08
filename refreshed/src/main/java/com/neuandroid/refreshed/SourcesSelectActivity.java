package com.neuandroid.refreshed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static android.view.View.GONE;
import static com.neuandroid.refreshed.NetworkUtils.NEWS_API_SOURCES_URL;

/**
 * Created by Jienan on 2017/7/24.
 */

public class SourcesSelectActivity extends AppCompatActivity {

    private final static String TAG = SourcesSelectActivity.class.getSimpleName();

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private NewsSourceBean bean;
    private ProgressBar pbLoading;
    private RecyclerView rvSources;
    private Set<String> selectedSet;
    private SourcesAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_select);
        setTitle("Select Sources");

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        rvSources = (RecyclerView) findViewById(R.id.rv_source_list);

        sharedPreferences = getSharedPreferences("refreshed_source", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String jsonString = sharedPreferences.getString("sources", "");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvSources.getContext(),
                RecyclerView.VERTICAL);
        rvSources.addItemDecoration(dividerItemDecoration);
        mAdapter = new SourcesAdapter(null);
        rvSources.setLayoutManager(layoutManager);
        rvSources.setAdapter(mAdapter);
        if (!TextUtils.isEmpty(jsonString)) {
            selectedSet = sharedPreferences.getStringSet("selected_sources", null);
            bean = new Gson().fromJson(jsonString, NewsSourceBean.class);
            Log.d(TAG, "sources stored : " + jsonString);
            mAdapter.updateList(bean.getSources());
            pbLoading.setVisibility(GONE);
        } else {
            querySources();
        }
    }

    private void querySources() {
        URL url = NetworkUtils.buildUrl(NEWS_API_SOURCES_URL);
        new NewsQueryTask(new SourceListener(), NewsSourceBean.class).execute(url);
    }

    private class SourceListener implements NewsQueryTask.IAsyncTaskListener {

        @Override
        public void onPreExecute() {
        }

        @Override
        public void onPostExecute(Serializable result) {
            String json = new Gson().toJson(result);
            editor.putString("sources", json);
            editor.apply();
            if (result instanceof NewsSourceBean) {
                bean = (NewsSourceBean) result;
                pbLoading.setVisibility(GONE);
                mAdapter.updateList(bean.getSources());

            }
        }
    }

    private class SourcesAdapter extends RecyclerView.Adapter<SourcesAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final View mView;
            public final TextView mTvName;
            public final TextView mTvDescription;
            public final CheckBox mCkbSelect;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTvName = view.findViewById(R.id.tv_source_name);
                mTvDescription = view.findViewById(R.id.tv_source_description);
                mCkbSelect = view.findViewById(R.id.cbx_selected);
            }

        }

        private List<NewsSourceBean.SourcesBean> sourceList;

        public SourcesAdapter(List<NewsSourceBean.SourcesBean> list) {
            sourceList = list;
        }

        public void updateList(List<NewsSourceBean.SourcesBean> list) {
            sourceList = list;
            notifyDataSetChanged();
        }

        @Override
        public SourcesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(SourcesSelectActivity.this).inflate(R.layout.list_item_source, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SourcesAdapter.ViewHolder holder, int position) {
            final NewsSourceBean.SourcesBean bean = sourceList.get(position);
            holder.mTvName.setText(bean.getName());
            holder.mTvDescription.setText(bean.getDescription());
            holder.mCkbSelect.setChecked(false);
            if (selectedSet != null) {
                if (selectedSet.contains(bean.getId() + "|" + bean.getName()))
                    holder.mCkbSelect.setChecked(true);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String editSource = bean.getId() + "|" + bean.getName();
                    if (selectedSet != null) {
                        if (selectedSet.contains(editSource)) {
                            selectedSet.remove(editSource);
                            holder.mCkbSelect.setChecked(false);
                        } else {
                            selectedSet.add(editSource);
                            holder.mCkbSelect.setChecked(true);
                        }

                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return sourceList == null ? 0 : sourceList.size();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putStringSet("selected_sources", selectedSet);
        editor.commit();
    }
}
