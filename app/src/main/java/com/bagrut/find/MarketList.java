package com.bagrut.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class MarketList extends Fragment
{
    /**
     * product list
     */
    private RecyclerView recyclerView;
    /**
     * product list adapter
     */
    private CentralAdapter<Product> productAdapter;
    /**
     * has the popularity rank of last product
     */
    private int popularCount = 0;

    public MarketList()
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
        return inflater.inflate(R.layout.market_list, container, false);
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

        ArrayList<Product> products = new ArrayList<>();

        recyclerView = CentralAdapter.createRecycleView((RecyclerView) getView().findViewById(R.id.productList), getContext(), products, CentralAdapter.MARKET_PRODUCT_LIST);
        productAdapter = (CentralAdapter<Product>) recyclerView.getAdapter();

        DBConn.requestMostPopularProducts(0, new getProductListener()
        {
            @Override
            public void onProductRetrieved(ArrayList<Product> products)
            {
                changeDataSet(products);
            }

        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1))
                {
                    DBConn.requestMostPopularProducts(popularCount, new getProductListener()
                    {
                        @Override
                        public void onProductRetrieved(ArrayList<Product> products)
                        {
                            changeDataSet(products);
                        }
                    });
                }
            }
        });

    }

    private void changeDataSet(ArrayList<Product> products)
    {
        productAdapter.items = new ArrayList<>();
        popularCount+=products.size();
        for(Product product:productAdapter.items)
        {
            for(Product product1:products)
            {
                if(product1.key==product.key)
                {
                    product = product1;
                    products.remove(product1);
                }
            }
        }
        for(Product product:products)
        {
            productAdapter.items.add(product);
        }
        productAdapter.notifyDataSetChanged();
    }
}