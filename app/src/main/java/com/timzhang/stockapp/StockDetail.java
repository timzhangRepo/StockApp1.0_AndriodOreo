package com.timzhang.stockapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



public class StockDetail extends AppCompatActivity {
    String ticker = "";
    String company = "";
    Double price = 0.0;
    SharedPreferences sharedPreferences;
    Set<String> favorites = new HashSet<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Button tradeButton;

    //Loading screen
    LoadingDialog loadingDialog;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.detailmenu, menu);
        Log.i("set", favorites.toString());
        if(favorites.contains(ticker+"$"+company)){
            MenuItem item = menu.findItem(R.id.star);
            item.setIcon(R.drawable.ic_baseline_star_24);
            item.setTitle("a");
        }
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * Handling star click event,
     * Swap star,
     * add to permennent memory set.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().charAt(0) == 'b') {
            favorites.add(ticker+"$"+company);
            //股票不在收藏中
            sharedPreferences.edit().putStringSet("favorites", favorites).apply();
            item.setIcon(R.drawable.ic_baseline_star_24);
            item.setTitle("a");
            Toast.makeText(this, "\"" + ticker + "\"" + " was added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            //股票在收藏中
            favorites.remove(ticker+"$"+company);
            sharedPreferences.edit().putStringSet("favorites", favorites).apply();
            favorites.remove(ticker);
            item.setIcon(R.drawable.ic_baseline_star_border_24);
            item.setTitle("b");
            Toast.makeText(this, "\"" + ticker + "\"" + " was removed to favorites", Toast.LENGTH_SHORT).show();
        }
        Log.i("memeory", favorites.toString());
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        //Webview
        createWebView();

        //购买，抛售股票在这里进行。
        tradeButton = findViewById(R.id.tradeButton);
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTradeDialog();
            }
        });


        super.onStart();
    }

    //HighCharts part
    public void createWebView(){
        WebView webview;
        webview = (WebView) findViewById(R.id.highChart);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.clearCache(true);
        settings.setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("http://10.0.2.2:4200/webview/"+ticker);
    }

    public void openTradeDialog(){
        Bundle bundle = new Bundle();

        //给Dialog传数据
        bundle.putString("ticker", this.ticker);
        bundle.putString("name", this.company);
        bundle.putDouble("last", this.price);

        TradeDialog tradeDialog = new TradeDialog();
        tradeDialog.setArguments(bundle);
        tradeDialog.show(getSupportFragmentManager(), "trade dialog");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Stocks");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        Intent intent = getIntent();
        //Assign value to Ticker
        ticker = intent.getStringExtra("ticker");
        company = intent.getStringExtra("company");

        //Loading
        loadingDialog = new LoadingDialog(StockDetail.this);
        loadingDialog.startLoadingDialog();
        //本地储存
        sharedPreferences = this.getSharedPreferences("com.timzhang.stockapp", Context.MODE_PRIVATE);
        favorites = sharedPreferences.getStringSet("favorites", new HashSet<>());
        download task = new download();

        mRecyclerView = findViewById(R.id.news_RecyclerView);
        task.execute("http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/daily/" + ticker, "http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/price/" + ticker, "http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/news/" + ticker);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);

    }

    /**
     * @param s
     * @throws Exception
     */
    public void populateNews(String s) throws Exception {
        JSONObject newsDump = new JSONObject(s);
        JSONArray articlesDump = newsDump.getJSONArray("articles");
        ArrayList<NewsItem> articles = new ArrayList<>();
        //第一条写在这儿，图片大点
        for (int i = 0; i < articlesDump.length(); i++) {
            JSONObject article = articlesDump.getJSONObject(i);
            JSONObject source = article.getJSONObject("source");
            String title = article.getString("title");
            if (title.length() > 100) {
                title = title.substring(0, 100) + "...";
            }
            articles.add(new NewsItem(
                    article.getString("urlToImage"),
                    article.getString("url"),
                    source.getString("name"),
                    title
            ));
        }
        Picasso.get().load(articles.get(0).getmURLtoImage()).into((ImageView) findViewById(R.id.firstnewsImage));
        TextView textView = (TextView) findViewById(R.id.textView15);
        textView.setText(articles.get(0).getmText1());
        textView = findViewById(R.id.textView16);
        textView.setText(articles.get(0).getmText2());
        articles.remove(0);
        mAdapter = new NewsAdapter(articles);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        loadingDialog.dismissDialog();
    }

    public void populateTitleAndInfo(String[] s) throws Exception {
        //Set Ticker, Company Name,
        JSONObject dailyObject = new JSONObject(s[0]);
        //设置Stock Ticker
        TextView textView = (TextView) findViewById(R.id.stock_ticker);
        textView.setText(dailyObject.getString("ticker"));
        //设置公司名称
        textView = (TextView) findViewById(R.id.stock_company);
        textView.setText(dailyObject.getString("name"));
        //记录公司名称
        this.company = dailyObject.getString("name");
        //设置About
        textView = (TextView) findViewById(R.id.AboutContent);
        textView.setText(dailyObject.getString("description"));


        //Set price, and difference
        JSONArray priceObjects = new JSONArray(s[1]);
        JSONObject priceObject = (JSONObject) priceObjects.get(0);
        textView = (TextView) findViewById(R.id.stock_price);
        //set Price.
        textView.setText("$" + String.valueOf(priceObject.getDouble("last")));
        price = priceObject.getDouble("last");
        //set PrevClose-Last
        textView = (TextView) findViewById(R.id.stock_change);
        double dif = priceObject.getDouble("last") - priceObject.getDouble("prevClose");
        dif = Math.round(dif * 100.0) / 100.0;
        if (dif >= 0) {
            textView.setTextColor(Color.parseColor("#77C73F"));
        } else {
            textView.setTextColor(Color.RED);
        }
        textView.setText(String.valueOf(dif));
        //Current Price
        textView = (TextView) findViewById(R.id.textView7);
        textView.setText("Current Price:" + String.valueOf(priceObject.getDouble("last")));
        //Low
        textView = (TextView) findViewById(R.id.textView8);
        textView.setText("Low: " + String.valueOf(priceObject.getDouble("low")));
        //Bid Pirce
        textView = (TextView) findViewById(R.id.textView9);
        if (priceObject.isNull("bidPrice")) {
            textView.setText("Bid Price: " + "0.0");
        } else {
            textView.setText("Bid Price: " + priceObject.getDouble("bidPrice"));
        }
        //Open Price
        textView = (TextView) findViewById(R.id.textView10);
        textView.setText("Open Price: " + String.valueOf(priceObject.getDouble("open")));
        //Mid
        textView = (TextView) findViewById(R.id.textView11);
        if (priceObject.isNull("mid")) {
            textView.setText("Mid : " + "0.0");
        }else{
            textView.setText("Mid: " + priceObject.getDouble("mid"));
        }
        //High
        textView = (TextView) findViewById(R.id.textView12);
        if(priceObject.isNull("high")){
            textView.setText("High : " + "0.0");
        }else{
            textView.setText("High: " + String.valueOf(priceObject.getDouble("high")));
        }
        //Volume
        textView = (TextView) findViewById(R.id.textView13);
        if(priceObject.isNull("volume")){
            textView.setText("Volume : " + "0");
        }else{
            textView.setText("Volume: " + String.valueOf(priceObject.getInt("volume")));
        }

        textView = (TextView) findViewById(R.id.textView2);
        JSONObject object = new JSONObject(sharedPreferences.getString("purchased", ""));
        if(object.isNull(ticker)){
           textView.setText("You have 0 shares of "+ticker+" \n Start trading now!");
        }else{
            textView.setText("You have "+object.get(ticker)+" shares of "+ticker+"\n Start trading now!");
        }

    }

    public class download extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... urls) {
            String[] res = new String[urls.length];
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                for (int i = 0; i < res.length; i++) {
                    url = new URL(urls[i]);
                    String stream = "";
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    int data = reader.read();
                    while (data != -1) {
                        char current = (char) data;
                        stream += current;
                        data = reader.read();
                    }
                    res[i] = stream;
                }
                return res;
            } catch (Exception e) {
                e.printStackTrace();
                return res;
            }
        }

        @Override
        protected void onPostExecute(String[] s) {
            //s format
            // 0 = daily
            // 1 = price
            // 2 = news
            super.onPostExecute(s);
            try {
                //populate title
                populateTitleAndInfo(s);
                //populate news article
                populateNews(s[2]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}