package com.zu.customview;


import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.zu.customview.view.ChangeSizeTabIndicator;
import com.zu.customview.view.SimpleFragment;

import java.util.ArrayList;

public class ChangeSizeIndicatorActivity extends AppCompatActivity {

    private ChangeSizeTabIndicator indicator;
    private ViewPager viewPager;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MyFragmentPagerAdapter adapter;

    private ArrayList<String> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_size_indicator);
        indicator = findViewById(R.id.indicator);
        viewPager = findViewById(R.id.view_pager);
        for(int i = 1; i < 5; i++)
        {

            tags.add("Tag" + i);
            SimpleFragment simpleFragment = new SimpleFragment();
            simpleFragment.setTextView("" + i);
            fragments.add(simpleFragment);
        }

        indicator.setTags(tags);
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
        indicator.setOnIndicatorClickListener(new ChangeSizeTabIndicator.OnIndicatorClickListener() {
            @Override
            public void onClick(int index) {
                viewPager.setCurrentItem(index, true);
            }
        });



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
