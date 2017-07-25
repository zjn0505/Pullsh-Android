/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neuandroid.news.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.neuandroid.news.model.NewsListBean;
import com.neuandroid.news.util.NewsNetworkUtils;
import com.neuandroid.refreshed.R;
import com.neuandroid.util.DownloadTask;
import com.neuandroid.util.TimeUtils;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class NewsListFragment extends Fragment
        implements DownloadTask.IAsyncTaskListener, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout refreshLayout;
    private String newsSource;
    private NewsAdapter mAdapter;
    private ProgressBar pbLoading;


    public void setSource(String source) {
        newsSource = source;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_news, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout layout = (FrameLayout)  inflater.inflate(
                R.layout.fragment_news_list, container, false);
        if (savedInstanceState != null) {
            newsSource = savedInstanceState.getString("source");
        }
        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recyclerview);
        setupRecyclerView(rv);

        refreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_refresh_list);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setEnabled(false);
        pbLoading = (ProgressBar) layout.findViewById(R.id.pb_loading);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                refreshLayout.setRefreshing(true);
                loadData();
            }
        });

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("source", newsSource);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("NewsListFragment", "created, loading data");
        loadData();
        pbLoading.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAdapter = new NewsAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        URL url = NewsNetworkUtils.buildUrl(newsSource, "top");
        new DownloadTask(this, new NewsListBean()).execute(url);
    }



    @Override
    public void onPreExecute() {

    }

    @Override
    public void onPostExecute(Serializable result) {
        pbLoading.setVisibility(View.GONE);
        refreshLayout.setEnabled(true);
        refreshLayout.setRefreshing(false);
        if (result instanceof NewsListBean) {
            NewsListBean newsList = (NewsListBean) result;
            if ("ok".equals(newsList.getStatus())) {
                List<NewsListBean.ArticlesBean> articles = newsList.getArticles();
                if (articles != null && articles.size() > 0) {
                    mAdapter.updateList(newsList.getArticles());
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        //Log.d("NewsListFragment", "onRefresh called");
        refreshLayout.setRefreshing(true);
        loadData();
    }

    @Override
    public void onDetach() {
        //Log.d("NewsListFragment", "onDetach called");
        refreshLayout.setRefreshing(false);
        super.onDetach();
    }


    private static class NewsAdapter
            extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<NewsListBean.ArticlesBean> mArticles;

        private final int ARTICLE = 0;
        private final int ATTRIBUTION = 1;

        static class ViewHolder extends RecyclerView.ViewHolder {

            final View mView;
            final ImageView mIvThumbnail;
            final TextView mTvTitle;
            final TextView mTvDescription;
            final TextView mTvPublishTime;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mIvThumbnail = (ImageView) view.findViewById(R.id.iv_thumbnails);
                mTvTitle = (TextView) view.findViewById(R.id.tv_title);
                mTvDescription = (TextView) view.findViewById(R.id.tv_description);
                mTvPublishTime = (TextView) view.findViewById(R.id.tv_publish_time);
            }

        }

        public NewsAdapter(Context context, List<NewsListBean.ArticlesBean> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mArticles = items;
        }

        public void updateList(List<NewsListBean.ArticlesBean> articles) {
            mArticles = articles;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch(viewType){
                case ARTICLE:
                    view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);
                    view.setBackgroundResource(mBackground);
                    break;
                case ATTRIBUTION:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_attribution, parent, false);
                    view.setBackgroundResource(mBackground);
                    break;
                default:
                    view = null;
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if(position == mArticles.size()){
                return;
            }
            final NewsListBean.ArticlesBean article = mArticles.get(position);
            holder.mTvTitle.setText(article.getTitle());
            holder.mTvDescription.setText(article.getDescription());
            holder.mTvPublishTime.setText(TimeUtils.convertTimeToString(article.getPublishedAt()));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
                    context.startActivity(browserIntent);
                }
            });

            String imgUrl = article.getUrlToImage();
            if (TextUtils.isEmpty(imgUrl)) {
                holder.mIvThumbnail.setVisibility(View.GONE);
            } else {
                Glide.with(holder.mIvThumbnail.getContext())
                        .load(article.getUrlToImage())
                        .into(holder.mIvThumbnail);
            }
        }

        @Override
        public int getItemCount() {
            return mArticles == null ? 0 : mArticles.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if(position < mArticles.size()){
                return ARTICLE;
            } else {
                return ATTRIBUTION;
            }
        }
    }
}
