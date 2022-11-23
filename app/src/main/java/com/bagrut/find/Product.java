package com.bagrut.find;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * a class that holds all data about a specific product
 */
public class Product extends DBObject
{
    /**
     * company name
     */
    private String name;
    /**
     * company description
     */
    private String description;
    /**
     * sellers id
     */
    private String sellerId;
    /**
     * sellers name
     */
    private String sellerName;
    /**
     * product in stock
     */
    private int inStock;
    /**
     * price per unit of product
     */
    private double price;
    /**
     * daily amount of bought product
     */
    private int dailySales;
    /**
     * total amount of bought product
     */
    private int timesSoled;
    /**
     * product reviews
     */
    private ArrayList<Review> reviews;
    /**
     * product picture name
     */
    private String picName;
    /**
     * daily profit\loss
     */
    private double dailyPnL;
    /**
     * daily revenue
     */
    private double dailyRevenue;
    /**
     * daily views
     */
    private int dailyViews;
    /**
     * total views
     */
    private int totalViews;

    /**
     * constructor
     */
    public Product()
    {
    }

    /**
     * constructor
     * @param name product name
     * @param description product description
     * @param sellerId seller id of product
     * @param sellerName seller name
     * @param price price per product unit
     * @param inStock amount of product in stock
     */
    public Product(String name, String description, String sellerId, String sellerName, double price, int inStock)
    {
        this.name = name;
        this.description = description;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.inStock = inStock;
        this.price = price;
        this.timesSoled = 0;
        this.reviews = new ArrayList<>();
        this.dailyPnL = 0;
        this.dailyViews = 0;
        this.totalViews = 0;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getSellerId()
    {
        return sellerId;
    }

    public String getSellerName()
    {
        return sellerName;
    }

    public int getInStock()
    {
        return inStock;
    }

    public void setInStock(int inStock)
    {
        this.inStock = inStock;
    }

    public double getPrice()
    {
        return price;
    }

    public int getTotalViews()
    {
        return totalViews;
    }

    public void setTotalViews(int totalViews)
    {
        this.totalViews = totalViews;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setSellerId(String sellerId)
    {
        this.sellerId = sellerId;
    }

    public void setSellerName(String sellerName)
    {
        this.sellerName = sellerName;
    }

    public int getDailySales()
    {
        return dailySales;
    }

    public void setDailySales(int dailySales)
    {
        this.dailySales = dailySales;
    }

    public void setTimesSoled(int timesSoled)
    {
        this.timesSoled = timesSoled;
    }

    public double getDailyRevenue()
    {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue)
    {
        this.dailyRevenue = dailyRevenue;
    }

    public int getDailyViews()
    {
        return dailyViews;
    }

    public void setDailyViews(int dailyViews)
    {
        this.dailyViews = dailyViews;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public int getTimesSoled()
    {
        return timesSoled;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String sell(final int amount)
    {
        final Product product = this;
        if(DBConn.account.getBalance()>=this.price && amount <= inStock)
        {
            DBConn.account.setBalance(DBConn.account.getBalance()-this.price*amount);
            timesSoled++;
            dailySales++;
            inStock -= amount;
            dailyPnL+= this.price*amount;
            DBConn.updateProductToDB(this);
            ProductSale ps = new ProductSale(DBConn.account.accountId, this.getSellerName(), this.getSellerId(), this.getName(), this.key, amount, this.getPrice());
            ps.transactionTime = LocalDateTime.now();
            final DatabaseReference orderRef = DBConn.pushToDB(ps, "ordered");
            DBConn.updateAccountToDB();
            DBConn.requestCompanyById(sellerId, new ArrayList<Product>(), new getCompanyListener()
            {
                @Override
                public void onCompanyRetrieved(Company company)
                {
                    company.resetDailyData();
                    company.getSoldProducts().add(orderRef.getKey());
                    company.setBudget(company.getBudget()+price*amount);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    company.setLastPaymentDate(LocalDateTime.now().format(formatter));
                    company.getInbox().add(new Inbox(amount+" "+product.name+" was sold for "+price+"$"));
                    DBConn.updateCompanyToDB(company);
                }

                @Override
                public void onCompanyRetrievedAndProducts(Company company, ArrayList<Product> companyProductss)
                {

                }
            });
            return "success";
        }
        else if(DBConn.account.getBalance()<this.price)
        {
            return "noFunds";
        }
        else
            return "noStock";
    }

    public ArrayList<Review> getReviews()
    {
        if(reviews==null)
            reviews = new ArrayList<>();
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews)
    {
        this.reviews = reviews;
    }

    public String getPicName()
    {
        return picName;
    }

    public void setPicName(String picName)
    {
        this.picName = picName;
    }

    public double getDailyPnL()
    {
        return dailyPnL;
    }

    public void setDailyPnL(double dailyPnL)
    {
        this.dailyPnL = dailyPnL;
    }

}
