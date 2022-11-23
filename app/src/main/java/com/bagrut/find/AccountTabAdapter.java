package com.bagrut.find;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * adapter for tabs in FragmentAccountOverview
 */
public class AccountTabAdapter extends FragmentStateAdapter
{
    /**
     * tabs in tab adapter
     */
    public ArrayList<Fragment> tabs = new ArrayList<>();

    /**
     * constructor
     * @param fragment
     */
    public AccountTabAdapter(@NonNull Fragment fragment)
    {
        super(fragment);
        tabs.add(new AccountOverview());
        tabs.add(new AccountSettings());
        tabs.add(new OrderedList());
    }

    /**
     * creates a fragment
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
     * returns tabs count
     * @return
     */
    @Override
    public int getItemCount()
    {
        return tabs.size();
    }
}