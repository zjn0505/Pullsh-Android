package com.neuandroid.news.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neuandroid.news.model.SourcesList;
import com.neuandroid.refreshed.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 19/07/2017.
 */

public class NewsFragment extends Fragment {

    private TabLayout tabLayout;
    private AppBarLayout appbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        int newsIndex = getArguments().getInt("newsIndex");
        viewPager.setCurrentItem(newsIndex);
        tabLayout.setVisibility(View.VISIBLE);

        appbar = (AppBarLayout) getActivity().findViewById(R.id.appbar);
        return view;


    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        for (String source : SourcesList.sources) {
            NewsListFragment fragment = new NewsListFragment();
            fragment.setSource(source);
            adapter.addFragment(fragment, source);
        }
        viewPager.setAdapter(adapter);
    }

    private class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        tabLayout.setVisibility(View.GONE);
        appbar.setExpanded(true, true);
    }
}