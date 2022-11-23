package com.bagrut.find;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class StockList extends AppCompatActivity
{
    /**
     * list of public shares
     */
    private RecyclerView sharesList;
    /**
     * loading
     */
    private ProgressBar progressBar;

    /**
     * initialization
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        sharesList = findViewById(R.id.shars_list);
        progressBar = findViewById(R.id.progressBar);

        DBConn.requestMostPopularShares(5, new getShareListener()
        {
            @Override
            public void onShareRetrieved(ArrayList<Share> shares)
            {
                progressBar.setVisibility(View.INVISIBLE);
                sharesList = CentralAdapter.createRecycleView(sharesList, sharesList.getContext(), shares, CentralAdapter.SHARES_LIST);
            }
        });
    }
}