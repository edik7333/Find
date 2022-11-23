package com.bagrut.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class OrderedList extends Fragment
{
    /**
     * ordered products list
     */
    private RecyclerView ordered;
    /**
     * hides ordered list
     */
    private Button hideOrdered;
    /**
     * displays amount of ordered products
     */
    private TextView orderedCount;

    /**
     * constructor
     */
    public OrderedList()
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
        return inflater.inflate(R.layout.fragment_ordered_list, container, false);
    }

    /**
     * initialization
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        orderedCount = view.findViewById(R.id.count_pending);

        ordered = view.findViewById(R.id.pending);

        createLists();

        hideOrdered = view.findViewById(R.id.hide_pending);
        hideOrdered.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ordered.getVisibility()==View.VISIBLE)
                    ordered.setVisibility(View.GONE);
                else
                    ordered.setVisibility(View.VISIBLE);
            }
        });



    }

    /**
     * creates ordered list
     */
    private void createLists()
    {

        ArrayList<ProductSale> pendingList = new ArrayList<>();

        orderedCount.setText(Integer.toString(pendingList.size()));

        CentralAdapter.createRecycleView(this.ordered, getContext(), pendingList, CentralAdapter.ORDERED_PRODUCTS_LIST);
        updateList();

        //updates list if changes
        DBConn.setOnAccountUpdateListener(new AccountUpdateListener()
        {
            @Override
            public void onAccountUpdate(UserAccount userAccount)
            {
                updateList();
            }
        });

    }

    /**
     * updates order list
     */
    private void updateList()
    {
        ArrayList<ProductSale> pendingList = new ArrayList<>();

        for(ProductSale p: DBConn.tempOrderedProducts)
        {
            pendingList.add(p);
        }

        orderedCount.setText(Integer.toString(pendingList.size()));

        ((CentralAdapter) ordered.getAdapter()).items = pendingList;
        ordered.getAdapter().notifyDataSetChanged();
    }
}

