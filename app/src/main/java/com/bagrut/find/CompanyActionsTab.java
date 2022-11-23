package com.bagrut.find;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageMetadata;

import java.net.URI;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class CompanyActionsTab extends Fragment
{
    /**
     * seek bar to change payout ratio
     */
    private SeekBar payoutRatioSlider;
    /**
     * current selected payout percentage
     */
    private TextView payoutRatioPercent;
    /**
     * selected payout difference from current payout ratio
     */
    private TextView payoutRatioChange;
    /**
     * amount of shares to publish
     */
    private TextView sharesAmount;
    /**
     * how much each published share will cost
     */
    private TextView sharesPrice;
    /**
     * publish shares
     */
    private Button publishShares;
    /**
     * save and upload selected payout ratio to firebase
     */
    private Button savePayoutRatio;
    /**
     * set product list as visible or invisible
     */
    private Button openProductsList;
    /**
     * opens add product dialog
     */
    private Button addProduct;
    /**
     * product list
     */
    private RecyclerView productsOfCompanyList;
    /**
     * current payout ratio in firebase
     */
    private int defaultPayoutRatio;
    /**
     * product image
     */
    private Uri productImage;

    /**
     * constructor
     */
    public CompanyActionsTab()
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
        return inflater.inflate(R.layout.fragment_company_actions_tab, container, false);
    }

    /**
     * initializes fragment views
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //initialization
        payoutRatioSlider = view.findViewById(R.id.payout_seek_bar);
        payoutRatioPercent = view.findViewById(R.id.payout_ratio_percent);
        payoutRatioChange = view.findViewById(R.id.payout_ratio_change);
        savePayoutRatio = view.findViewById(R.id.save_payout_ratio);
        sharesAmount = view.findViewById(R.id.shares_amount);
        sharesPrice = view.findViewById(R.id.share_price);
        publishShares = view.findViewById(R.id.publish_shares);
        openProductsList = view.findViewById(R.id.open_products_list);
        addProduct = view.findViewById(R.id.add_product_button);
        productsOfCompanyList = view.findViewById(R.id.products_of_company_list);
        CentralAdapter.createRecycleView(productsOfCompanyList, getContext(), CompanyActivity.companyProducts, CentralAdapter.PRODUCTS_OF_COMPANY_LIST);
        productsOfCompanyList.setVisibility(View.GONE);

        //shares sell
        publishShares.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharesAmount.getText().toString().isEmpty() || sharesPrice.getText().toString().isEmpty())
                    return;
                DBConn.requestShareByCompanyId(CompanyActivity.company.key, new getShareListener() {
                    @Override
                    public void onShareRetrieved(ArrayList<Share> shares) {
                        if(shares.isEmpty())
                        {
                            DBConn.pushToDB(new Share("", Integer.parseInt(sharesAmount.getText().toString()), Double.parseDouble(sharesPrice.getText().toString()), CompanyActivity.company.key), "shares");
                            return;
                        }
                        Share share = shares.get(0);
                        share.setSharesAmount(Integer.parseInt(sharesAmount.getText().toString()));
                        share.setPrice(Double.parseDouble(sharesPrice.getText().toString()));
                        DBConn.updateShare(share);

                        Toast.makeText(payoutRatioChange.getContext(), "shares published", Toast.LENGTH_LONG).show();
                        sharesAmount.setText("");
                        sharesPrice.setText("");
                    }
                });

            }
        });

        //payout ratio seekbar
        payoutRatioSlider.setProgress(CompanyActivity.company.getPayoutRatio());
        defaultPayoutRatio = CompanyActivity.company.getPayoutRatio();
        payoutRatioPercent.setText(defaultPayoutRatio+"");
        payoutRatioSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                payoutRatioPercent.setText(i+"");
                if(i-defaultPayoutRatio>0)
                {
                    payoutRatioChange.setText("+"+(i-defaultPayoutRatio)+"%");
                    payoutRatioChange.setTextColor(Color.rgb(0,200, 50));
                }
                else if(i-defaultPayoutRatio==0)
                {
                    payoutRatioChange.setText("+"+(i-defaultPayoutRatio)+"%");
                    payoutRatioChange.setTextColor(Color.rgb(150,150, 150));
                }
                else
                {
                    payoutRatioChange.setText((i-defaultPayoutRatio)+"%");
                    payoutRatioChange.setTextColor(Color.rgb(180,0, 0));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        savePayoutRatio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CompanyActivity.company.setPayoutRatio(payoutRatioSlider.getProgress());
                DBConn.updateCompanyToDB(CompanyActivity.company);
                payoutRatioChange.setTextColor(Color.rgb(150,150, 150));
            }
        });

        openProductsList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(productsOfCompanyList.getVisibility()==View.GONE)
                {
                    productsOfCompanyList.setVisibility(View.VISIBLE);
                }
                else
                {
                    productsOfCompanyList.setVisibility(View.GONE);
                }
            }
        });

        addProduct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //build dialog
                final Dialog d = new Dialog(getContext());
                d.setContentView(R.layout.add_product_dialog);
                d.setTitle("add product");
                d.setCancelable(true);

                final EditText name = d.findViewById(R.id.product_name);
                final EditText description = d.findViewById(R.id.product_description);
                final EditText price = d.findViewById(R.id.product_price);
                Button publish = d.findViewById(R.id.publish_product);
                Button addPicture = d.findViewById(R.id.add_picture);

                addPicture.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        pickImage();
                    }
                });



                publish.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(price.getText().toString().isEmpty()||name.getText().toString().isEmpty()||description.getText().toString().isEmpty())
                        {
                            Toast.makeText(view.getContext(), "empty fields", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Product p = new Product(name.getText().toString(), description.getText().toString(), CompanyActivity.company.key, CompanyActivity.company.getName(), Double.parseDouble(price.getText().toString()), 0);
                        DatabaseReference ref = DBConn.pushToDB(p, "products");
                        if(CompanyActivity.company.getProductsKeys()==null)
                            CompanyActivity.company.setProductsKeys(new ArrayList<String>());
                        CompanyActivity.company.getProductsKeys().add(ref.getKey());
                        DBConn.updateCompanyToDB(CompanyActivity.company);
                        DBConn.uploadProductImage(ref.getKey(), productImage);
                        Toast.makeText(view.getContext(), "product published", Toast.LENGTH_LONG);
                        d.cancel();
                    }
                });
                d.show();
            }
        });

        DBConn.setOnCompaniesListener(new getCompanyListener()
        {
            @Override
            public void onCompanyRetrieved(Company company)
            {
                payoutRatioSlider.setProgress(company.getPayoutRatio());
                defaultPayoutRatio = company.getPayoutRatio();
                payoutRatioPercent.setText(defaultPayoutRatio+"");
                payoutRatioChange.setText("+0%");
                productsOfCompanyList.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
            {

            }
        });
    }

    /**
     * opens gallery to select picture from
     */
    private void pickImage()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    /**
     * receives selected image from gallery
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100)
        {
            productImage = data.getData();

        }
    }
}