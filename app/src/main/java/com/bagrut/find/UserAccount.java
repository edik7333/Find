package com.bagrut.find;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAccount class holds all data about the user that is currently logged into the app
 */
@IgnoreExtraProperties
public class UserAccount extends DBObject
{
    /**
     * account id
     */
    public String accountId;
    /**
     * first name
     */
    public String fname;
    /**
     * last name
     */
    public String lname;
    /**
     * gender
     */
    public String gender;
    /**
     * balance
     */
    public double balance;
    /**
     * email
     */
    public String email;
    /**
     * phone
     */
    public String phone;
    /**
     * invested money
     */
    public double investedMoney;
    /**
     * last daily profit\loss
     */
    public double lastDailyPnL;
    /**
     * last monthly profit\loss
     */
    public double lastMonthPnL;
    /**
     * products in cart
     */
    private ArrayList<ProductSale> inCart;
    /**
     * favorite products
     */
    private ArrayList<String> favorite;
    /**
     * keys of companies owned by account
     */
    private ArrayList<String> companiesKeys;
    /**
     * upVoted reviews
     */
    private ArrayList<String> upVoteReviews;
    /**
     * downVoted reviews
     */
    private ArrayList<String> downVoteReviews;
    /**
     * keys of visited products
     */
    private ArrayList<String> visitedProducts;


    /**
     * constructor
     */
    public UserAccount()
    {
    }

    /**
     * constructor
     * @param uid user id
     * @param fname first name
     * @param lname last name
     * @param gender gender
     * @param email email
     * @param phone phone
     */
    public UserAccount(String uid, String fname, String lname, String gender, String email, String phone)
    {
        this.accountId = uid;
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
        this.balance = 0;
        this.email = email;
        this.phone = phone;
        this.investedMoney = 0;
        this.lastDailyPnL = 0;
        this.lastMonthPnL = 0;
        this.inCart = new ArrayList<>();
        this.favorite = new ArrayList<>();
        this.companiesKeys = new ArrayList<>();
        this.upVoteReviews = new ArrayList<>();
        this.downVoteReviews = new ArrayList<>();
        this.visitedProducts = new ArrayList<>();
    }


    public String getAccountId()
    {
        return accountId;
    }

    public void setAccountId(String accountId)
    {
        this.accountId = accountId;
    }

    public String getFname()
    {
        return fname;
    }

    public void setFname(String fname)
    {
        this.fname = fname;
    }

    public String getLname()
    {
        return lname;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public void setLname(String lname)
    {
        this.lname = lname;
    }

    public double getBalance()
    {
        return balance;
    }

    public void setBalance(double balance)
    {
        this.balance = balance;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public ArrayList<String> getVisitedProducts()
    {
        if(visitedProducts==null)
            visitedProducts = new ArrayList<>();
        return visitedProducts;
    }

    public void setVisitedProducts(ArrayList<String> visitedProducts)
    {
        this.visitedProducts = visitedProducts;
    }

    public void addFav(String productId)
    {
        if(favorite==null)
            favorite = new ArrayList<>();
        this.favorite.add(productId);
    }

    public void removeFav(String productID)
    {
        this.favorite.remove(productID);
    }

    public ArrayList<ProductSale> getInCart()
    {
        if(inCart==null)
            inCart = new ArrayList<>();
        return inCart;
    }

    public List<String> getFavorite()
    {
        return favorite;
    }

    public ArrayList<String> getCompaniesKeys()
    {
        if(companiesKeys==null)
            companiesKeys = new ArrayList<>();
        return companiesKeys;
    }

    public void setCompaniesKeys(ArrayList<String> companiesKeys)
    {
        this.companiesKeys = companiesKeys;
    }

    public double getInvestedMoney()
    {
        return investedMoney;
    }

    public void setInvestedMoney(double investedMoney)
    {
        this.investedMoney = investedMoney;
    }

    public double getLastDailyPnL()
    {
        return lastDailyPnL;
    }

    public void setLastDailyPnL(double lastDailyPnL)
    {
        this.lastDailyPnL = lastDailyPnL;
    }

    public double getLastMonthPnL()
    {
        return lastMonthPnL;
    }

    public void setLastMonthPnL(double lastMonthPnL)
    {
        this.lastMonthPnL = lastMonthPnL;
    }

    public void setFavorite(ArrayList<String> favorite)
    {
        this.favorite = favorite;
    }

    public void setInCart(ArrayList<ProductSale> inCart)
    {
        this.inCart = inCart;
    }

    public ArrayList<String> getUpVoteReviews()
    {
        if(upVoteReviews==null)
            upVoteReviews = new ArrayList<>();
        return upVoteReviews;
    }

    public void setUpVoteReviews(ArrayList<String> upVoteReviews)
    {
        this.upVoteReviews = upVoteReviews;
    }

    public ArrayList<String> getDownVoteReviews()
    {
        if(downVoteReviews==null)
            downVoteReviews = new ArrayList<>();
        return downVoteReviews;
    }

    public void setDownVoteReviews(ArrayList<String> downVoteReviews)
    {
        this.downVoteReviews = downVoteReviews;
    }
}
