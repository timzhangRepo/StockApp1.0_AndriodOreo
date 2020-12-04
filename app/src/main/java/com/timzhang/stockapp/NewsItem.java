package com.timzhang.stockapp;

public class NewsItem {
    private String mURLtoImage;
    private String mURL;
    private String mText1;
    private String mText2;
    public NewsItem(String mURLtoImage, String mURL, String mText1, String mText2){
        this.mURLtoImage = mURLtoImage;
        this.mURL = mURL;
        this.mText1 = mText1;
        this.mText2 = mText2;
    }
    public String getmURLtoImage(){
        return mURLtoImage;
    }
    public String getmURL() {
        return mURL;
    }

    public String getmText1() {
        return mText1;
    }

    public String getmText2() {
        return mText2;
    }
}
