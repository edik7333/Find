package com.bagrut.find;

import android.os.Build;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.ServerValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * company class holds all data about a specific company
 */
public class Company extends DBObject
{
    /**
     * company name
     */
    private String name;
    /**
     * company id
     */
    private String companyId;
    /**
     * company description
     */
    private String description;
    /**
     * company budget
     */
    private double budget;
    /**
     * daily profit\loss
     */
    private double dailyPnL;
    /**
     * daily amount of bought products
     */
    private int dailySales;
    /**
     * daily revenue
     */
    private double dailyRevenue;
    /**
     * daily views
     */
    private double dailyViews;
    /**
     * yesterday profit\loss
     */
    private double previousDailyPnL;
    /**
     * payout ratio
     */
    private int payoutRatio;
    /**
     * products for sale
     */
    private ArrayList<Product> forSale;
    /**
     * product keys
     */
    private ArrayList<String> productsKeys;
    /**
     * owners of company
     */
    private ArrayList<Share> owners;
    /**
     * id of sold products
     */
    private ArrayList<String> soldProductsId;
    /**
     * messages of company
     */
    private ArrayList<Inbox> inbox;
    /**
     * the last date when a product purchase
     */
    private String lastPaymentDate = "";

    /**
     * constructor
     */
    public Company()
    {

    }

    /**
     * constructor
     * @param name name of company
     * @param description description of company
     * @param budget company budget
     */
    public Company(String name, String description, double budget)
    {
        this.name = name;
        this.description = description;
        this.budget = budget;
        this.forSale = new ArrayList<>();
        this.productsKeys = new ArrayList<>();
        this.owners = new ArrayList<>();
        this.soldProductsId = new ArrayList<>();
        this.inbox = new ArrayList<>();
        this.dailyPnL = 0;
        this.dailySales = 0;
        this.dailyRevenue = 0;
        this.dailyViews = 0;
        this.previousDailyPnL = 0;
        this.payoutRatio = 0;
    }

    /**
     * resets daily data when new day starts
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resetDailyData()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if(!lastPaymentDate.equals(LocalDateTime.now().format(formatter)))
        {
            previousDailyPnL = dailyPnL;
            setDailyPnL(0);
            setLastPaymentDate(LocalDateTime.now().format(formatter));
            DBConn.updateCompanyToDB(this);
        }
    }

    /**
     * resets daily data when new day starts
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resetDailyData(ArrayList<Product> companyProducts)
    {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if(!lastPaymentDate.equals(LocalDateTime.now().format(formatter)))
        {
            previousDailyPnL = dailyPnL;
            setDailyPnL(0);
            setDailyViews(0);
            setDailySales(0);
            for(Product product:companyProducts)
            {
                product.setDailyPnL(0);
                product.setDailySales(0);
                product.setDailyViews(0);
                DBConn.updateProductToDB(product);
            }
            setLastPaymentDate(LocalDateTime.now().format(formatter));
            DBConn.updateCompanyToDB(this);
        }
    }

    /**
     * buys shares of company
     * @param price the share price
     * @param amount the amount of shares
     * @param accountId the account to buy for
     * @param listener listens to response if shares bought
     */
    public void buyShares(final double price, final int amount, String accountId, final getShareListener listener)
    {
        if(DBConn.account.balance<price)
            return;

        final Company c = this;

        DBConn.requestShareByCompanyId(this.key, new getShareListener()
        {
            @Override
            public void onShareRetrieved(ArrayList<Share> shares)
            {
                Share share = shares.get(0);
                if(share.getSharesAmount()<amount)
                    return;
                share.setSharesAmount(share.getSharesAmount()-amount);

                boolean exists = false;
                for(Share shareI : owners)
                {
                    if(shareI.getOwnerID().equals(DBConn.account.accountId))
                    {
                        shareI.setSharesAmount(shareI.getSharesAmount()+amount);
                        exists = true;
                        break;
                    }
                }
                if(!exists)
                {
                    owners.add(new Share(DBConn.account.accountId, amount, "INVESTOR"));
                }

                budget+=price*amount;
                DBConn.account.balance-=price*amount;

                boolean keyExists = false;
                for(String key:DBConn.account.getCompaniesKeys())
                {
                    if(key.equals(key))
                    {
                        keyExists = true;
                    }
                }
                if(!keyExists)
                    DBConn.account.getCompaniesKeys().add(key);
                DBConn.account.setInvestedMoney(DBConn.account.getInvestedMoney()+amount*price);
                c.getInbox().add(new Inbox(amount+" shares were sold at a price of "+price+"$"));

                DBConn.updateShare(share);
                DBConn.updateAccountToDB();
                DBConn.updateCompanyToDB(c);
                listener.onShareRetrieved(null);
            }
        });


    }

    public ArrayList<Inbox> getInbox()
    {
        if(inbox == null)
            inbox = new ArrayList<>();
        return inbox;
    }

    public void setInbox(ArrayList<Inbox> inbox)
    {
        this.inbox = inbox;
    }

    public int getPayoutRatio()
    {
        return payoutRatio;
    }

    public void setPayoutRatio(int payoutRatio)
    {
        this.payoutRatio = payoutRatio;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(String companyId)
    {
        this.companyId = companyId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public double getBudget()
    {
        return budget;
    }

    public void setBudget(double budget)
    {
        this.budget = budget;
    }

    public double getDailyPnL()
    {
        return dailyPnL;
    }

    public void setPreviousDailyPnL(double previousDailyPnL)
    {
        this.previousDailyPnL = previousDailyPnL;
    }

    public ArrayList<Product> getForSale()
    {
        return forSale;
    }

    public void setForSale(ArrayList<Product> forSale)
    {
        this.forSale = forSale;
    }

    public ArrayList<Share> getOwners()
    {
        return owners;
    }

    public void setOwners(ArrayList<Share> owners)
    {
        this.owners = owners;
    }

    public ArrayList<String> getSoldProducts()
    {
        if(soldProductsId==null)
            soldProductsId = new ArrayList<>();
        return soldProductsId;
    }

    public void setSoldProducts(ArrayList<String> soldProducts)
    {
        this.soldProductsId = soldProducts;
    }

    public int getDailySales()
    {
        return dailySales;
    }

    public void setDailySales(int dailySales)
    {
        this.dailySales = dailySales;
    }

    public double getDailyRevenue()
    {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue)
    {
        this.dailyRevenue = dailyRevenue;
    }

    public double getDailyViews()
    {
        return dailyViews;
    }

    public void setDailyViews(double dailyViews)
    {
        this.dailyViews = dailyViews;
    }

    public String getLastPaymentDate()
    {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(String lastPaymentDate)
    {
        this.lastPaymentDate = lastPaymentDate;
    }

    public ArrayList<String> getProductsKeys()
    {
        if(productsKeys==null)
            productsKeys = new ArrayList<>();
        return productsKeys;
    }

    public void setProductsKeys(ArrayList<String> productsKeys)
    {
        this.productsKeys = productsKeys;
    }

    public void setDailyPnL(double dailyPnL)
    {
        this.dailyPnL = dailyPnL;
    }

    public double getPreviousDailyPnL()
    {
        return previousDailyPnL;
    }

    public Share getShareByOwnerID(String ownerID)
    {
        for(Share s:owners)
        {
            if(s.getOwnerID().equals(ownerID))
                return s;
        }
        return null;
    }

    public int getTotalShares()
    {
        int count = 0;
        for(Share s:owners)
        {
            count+=s.getSharesAmount();
        }
        return count;
    }

}

/**
 * a inbox message of company
 */
class Inbox extends DBObject
{
    /**
     * the content of the message
     */
    private String msg;

    /**
     * constructor
     */
    public Inbox()
    {
    }

    /**
     * constructor
     * @param msg the content of the message
     */
    public Inbox(String msg)
    {
        this.msg = msg;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
}

class Share extends DBObject
{
    /**
     * id of account who owns the share
     */
    private String ownerID;
    /**
     * amount of shares owned
     */
    private int sharesAmount;
    /**
     * the role of the owner example: ceo, coo, investor etc...
     */
    private Role role;
    /**
     * daily profit\loss of share
     */
    private double dailyPersonalPnL;
    /**
     * price per share
     */
    private double price;
    /**
     * id of company share
     */
    private String companyId;

    /**
     * constructor
     */
    public Share()
    {
    }

    /**
     * constructor
     * @param ownerID owners id
     * @param sharesAmount amount of shares
     * @param price price per share
     * @param companyId id of company
     */
    public Share(String ownerID, int sharesAmount, double price, String companyId)
    {
        this.ownerID = ownerID;
        this.sharesAmount = sharesAmount;
        this.price = price;
        role = Role.INVESTOR;
        this.companyId = companyId;
    }

    /**
     * constructor
     * @param ownerID id of owner
     * @param sharesAmount amount of shares
     * @param role role of owner
     */
    public Share(String ownerID, int sharesAmount, String role)
    {
        this.ownerID = ownerID;
        this.sharesAmount = sharesAmount;
        setRole(role);
    }

    public String getOwnerID()
    {
        return ownerID;
    }

    public void setOwnerID(String ownerID)
    {
        this.ownerID = ownerID;
    }

    public int getSharesAmount()
    {
        return sharesAmount;
    }

    public void setSharesAmount(int sharesAmount)
    {
        this.sharesAmount = sharesAmount;
    }

    public double getDailyPersonalPnL()
    {
        return dailyPersonalPnL;
    }

    public void setDailyPersonalPnL(double dailyPersonalPnL)
    {
        this.dailyPersonalPnL = dailyPersonalPnL;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRole()
    {
        switch (role)
        {
            case CEO:
                return "CEO";
            case CFO:
                return "CFO";
            case COO:
                return "COO";
            case DIRECTOR:
                return "DIRECTOR";
            case INVESTOR:
                return "INVESTOR";
            default:
                return null;
        }
    }

    public void setRole(String role)
    {
        switch (role)
        {
            case "CEO":
                this.role = Role.CEO;
            case "CFO":
                this.role = Role.CFO;
            case "COO":
                this.role = Role.COO;
            case "DIRECTOR":
                this.role = Role.DIRECTOR;
            case "INVESTOR":
                this.role = Role.INVESTOR;
            default:

        }
    }
}
