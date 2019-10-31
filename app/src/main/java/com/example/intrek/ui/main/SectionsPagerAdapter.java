package com.example.intrek.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<String> titles = new ArrayList<>() ;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        // TODO: use pictures instead of string
        titles.add("Profile");
        titles.add("New recording");
        titles.add("History");

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ProfileFragment.newInstance();
            case 1:
                return NewRecordingFragment.newInstance();
            default:
                return HistoryFragment.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.titles.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}
