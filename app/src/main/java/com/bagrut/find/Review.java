package com.bagrut.find;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * a class that holds all data about a specific review
 */
public class Review extends DBObject
{
    /**
     * review content
     */
    private String review;
    /**
     * account id of reviewer
     */
    private String reviewerID;
    /**
     * reviewer name
     */
    private String reviewerName;
    /**
     * published date
     */
    private String publishedDate;
    /**
     * review votes
     */
    private int votes;

    /**
     * constructor
     */
    public Review()
    {
    }

    /**
     * constructor
     * @param review review content
     * @param reviewerID reviewer id
     * @param reviewerName reviewer name
     * @param publishedDate published date
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Review(String review, String reviewerID, String reviewerName, LocalDateTime publishedDate)
    {
        this.review = review;
        this.reviewerID = reviewerID;
        this.reviewerName = reviewerName;
        this.publishedDate = publishedDate.format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
        this.votes = 0;
    }

    public String getReview()
    {
        return review;
    }

    public void setReview(String review)
    {
        this.review = review;
    }

    public String getReviewerID()
    {
        return reviewerID;
    }

    public void setReviewerID(String reviewer)
    {
        this.reviewerID = reviewer;
    }

    public String getReviewerName()
    {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName)
    {
        this.reviewerName = reviewerName;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDateTime getPublishedDateToLDT()
    {
        return LocalDateTime.parse(publishedDate, DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
    }

    public String getPublishedDate()
    {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate)
    {
        this.publishedDate = publishedDate;
    }

    public int getVotes()
    {
        return votes;
    }

    public void setVotes(int votes)
    {
        this.votes = votes;
    }
}
