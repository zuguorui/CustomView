package com.zu.customview;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.zu.customview.view.SimpleFragment;
import com.zu.customview.view.TextViewPagerIndicator;

import java.util.ArrayList;

public class ViewPagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TextViewPagerIndicator indicator;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> tags = new ArrayList<>();
    private MyFragmentPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        initViews();
    }

    private void initViews()
    {
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        indicator = (TextViewPagerIndicator)findViewById(R.id.view_pager_indicator);
        for(int i = 1; i < 10; i++)
        {

            tags.add(getTag(i));
            indicator.addTag(getTag(i));
            SimpleFragment simpleFragment = new SimpleFragment();
            simpleFragment.setTextView("" + i);
            fragments.add(simpleFragment);

        }
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                indicator.listen(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        indicator.addOnTagClickedListener(new TextViewPagerIndicator.OnTagClickedListener() {
            @Override
            public void onTagClicked(int position) {
                viewPager.setCurrentItem(position, true);
            }
        });
    }

    private String getTag(int x)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < x; i++)
        {
            sb.append("" + i);
        }
        return sb.toString();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        public MyFragmentPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragments)
        {
            super(fragmentManager);
            this.fragments = fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
