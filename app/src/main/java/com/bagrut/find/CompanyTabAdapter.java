package com.bagrut.find;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class CompanyTabAdapter extends FragmentStateAdapter
{
    /**
     * tab items
     */
    ArrayList<Fragment> tabs = new ArrayList<>();

    /**
     * constructor
     * @param fragmentManager
     * @param lifecycle
     */
    public CompanyTabAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle)
    {
        super(fragmentManager, lifecycle);
        tabs.add(new CompanyInfoTab());
        tabs.add(new CompanyActionsTab());
        tabs.add(new CompanyInboxTab());
    }

    /**
     * creates a tab fragment
     * @param position
     * @return
     */
    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        return tabs.get(position);
    }

    /**
     * returns amount of tabs
     * @return
     */
    @Override
    public int getItemCount()
    {
        return tabs.size();
    }
}
