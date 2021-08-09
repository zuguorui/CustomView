package com.zu.customview;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.zu.customview.view.SimpleFragment;
import com.zu.customview.view.TabBtnIndicator;

import java.util.ArrayList;

public class TabBtnActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabBtnIndicator indicator;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MyFragmentPagerAdapter adapter;

    private ArrayList<TabBtnIndicator.DataEntity> data = new ArrayList<>();

    String[] tags = {"硬件连接", "耳鸣治疗", "试试音呗", "我的"};
    int[] drawableIdSel = {R.mipmap.hardware_blue, R.mipmap.tinnitus_blue, R.mipmap.try_blue, R.mipmap.me_blue};
    int[] drawableIdUnsel = {R.mipmap.hardware_gray, R.mipmap.tinnitus_gray, R.mipmap.try_gray, R.mipmap.me_gray};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_btn);
        initViews();
    }
    private void initViews()
    {
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        indicator = findViewById(R.id.tbi_indicator);
        for(int i = 0; i < 4; i++)
        {

            TabBtnIndicator.DataEntity dataEntity = new TabBtnIndicator.DataEntity(i, tags[i], getDrawable(drawableIdUnsel[i]), getDrawable(drawableIdSel[i]));
            data.add(dataEntity);
            SimpleFragment simpleFragment = new SimpleFragment();
            simpleFragment.setTextView("" + i);
            fragments.add(simpleFragment);


        }
        indicator.setData(data);
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
        indicator.setOnIndicatorClickListener(new TabBtnIndicator.OnIndicatorClickListener() {
            @Override
            public void onClick(int id) {
                viewPager.setCurrentItem(id, false);
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
