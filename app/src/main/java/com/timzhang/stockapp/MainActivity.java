package com.timzhang.stockapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.Edits;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Set<String> favorites;
    private HashMap<String, String> ticker_comapny = new HashMap<>();

    private JSONObject purchased;

    //Recycler view for favs
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView mRecyclerView2;
    private RecyclerView.Adapter mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AndroidNetworking.initialize(getApplicationContext());
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /**
                 *
                 *
                 * CALLING API HERE AWS!
                 *
                 *
                 */
                AndroidNetworking.get("http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/daily/"+query)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                   String name = (String)response.get("name");
                                    openStockDetail(query, name);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(ANError anError) {
                                Log.i("SEARCH_ERROR","STOCK DOESN'T EXIST");
                            }
                        });
                //用户输入完毕后确认搜索内容
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() >= 1){
                    //用于populate list
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    protected void onStart() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("MMMM, dd, yyyy");
        TextView date = findViewById(R.id.date);
        date.setText(format.format(currentTime));
        download task = new download();

        mRecyclerView = findViewById(R.id.favorites_list);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);


        mRecyclerView2 = findViewById(R.id.purchased_list);
        mRecyclerView2.setNestedScrollingEnabled(false);
        mRecyclerView2.setHasFixedSize(false);

        mLayoutManager2 = new LinearLayoutManager(this);

        if(!favorites.isEmpty()){
            task.execute("http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/price/"+buildAPI_CallList(), "http://nodejsapp-timzhang.us-east-1.elasticbeanstalk.com/price/"+buildAPI_CallList2());
        }

        super.onStart();
    }

    /**
     * Build the API call list,from the set
     * Example F$Ford - > F,
     * For tiingo API
     * Sort Alphabetically in Order
     * @return
     */
    private String buildAPI_CallList (){
        PriorityQueue<String> minHeap = new PriorityQueue<>((a,b)->b.compareTo(a));
        for(String s: favorites){
            String temp[] = s.split("\\$");
            ticker_comapny.put(temp[0], temp[1]);
            minHeap.offer(temp[0]+",");
        }
        StringBuilder sb = new StringBuilder();
        while(!minHeap.isEmpty()){
            sb.append(minHeap.poll());
        }
        return sb.toString();
    }
    /**
     * 这个是Purchased stock API call list，返回一个String
     * Purchased stock 要从本地Shared memory拿到JSON，
     * 然后把JSON 便利一遍后提出Ticker
     * 然后返回String
     */
    private String buildAPI_CallList2(){
        Iterator<String> iterator = purchased.keys();
        StringBuilder sb = new StringBuilder();
        while(iterator.hasNext()){
            sb.append(iterator.next());
            sb.append(",");
        }
        return sb.toString();
    }
    /**
     * 打开Stock Detail Activity
     */
    public void openStockDetail(String query, String name){
        if(name==null || name.length() <1){return;}
        Intent itent = new Intent(this, StockDetail.class);
        itent.putExtra("ticker",query);
        itent.putExtra("company", name);
        startActivity(itent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.timzhang.stockapp", Context.MODE_PRIVATE);

        favorites = sharedPreferences.getStringSet("favorites", new HashSet<>());
        //拿到purchased的数据
        String temp = sharedPreferences.getString("purchased", "{}");
        try {
            purchased = new JSONObject(temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param s
     * @param index 0 = favorites, 1 = purchased
     * @throws Exception
     */
    public void populateStocks (String[] s, int index) throws Exception{
        JSONArray jsonArray = new JSONArray(s[index]);
        ArrayList<StockItem> stockItems = new ArrayList<>();
        for(int i=jsonArray.length()-1; i>=0; i--){
            JSONObject obj = (JSONObject) jsonArray.get(i);
            double dif = obj.getDouble("last") - obj.getDouble("prevClose");
            dif = Math.round(dif * 100.0) / 100.0;
            stockItems.add(new StockItem(
                    obj.getString("ticker"),
                    ticker_comapny.get(obj.get("ticker")),
                    obj.getDouble("open"),
                    dif
            ));
        }
        if(index == 0){
            mAdapter = new StockItemAdapter(stockItems);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
        if(index == 1){
            mAdapter2 = new StockItemAdapter(stockItems);
            mRecyclerView2.setLayoutManager(mLayoutManager2);
            mRecyclerView2.setAdapter(mAdapter2);
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
            if(s==null || s.length==0) return;
            //s format
            // 0 = list of price for each company in local memeory
            super.onPostExecute(s);
            try {
                //populate favs


                //populate purchased write here
                populateStocks(s, 1);
                populateStocks(s, 0);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }





}