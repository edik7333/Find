package com.bagrut.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CompanyInboxTab extends Fragment
{
    /**
     * list of company messages
     */
    RecyclerView inboxList;
    /**
     * the messages data
     */
    ArrayList<Inbox> inbox;

    /**
     * constructor
     */
    public CompanyInboxTab()
    {
        // Required empty public constructor
    }

    /**
     * on create fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    /**
     * on create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_company_inbox_tab, container, false);
    }

    /**
     * initializes views in fragment
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //initialization
        inbox = CompanyActivity.company.getInbox();
        inboxList = view.findViewById(R.id.inbox_list);
        CentralAdapter.createRecycleView(inboxList, view.getContext(), inbox, CentralAdapter.INBOX_LIST);

        DBConn.setOnCompaniesListener(new getCompanyListener()
        {
            @Override
            public void onCompanyRetrieved(Company company)
            {
                inbox = company.getInbox();
                inboxList.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
            {

            }
        });

    }
}