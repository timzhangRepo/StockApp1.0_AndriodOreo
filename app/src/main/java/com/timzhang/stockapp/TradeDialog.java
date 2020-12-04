package com.timzhang.stockapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class TradeDialog extends AppCompatDialogFragment {
    SharedPreferences sharedPreferences;
    private TextView title;
    private TextView amount;
    private TextView unit;

    private TextView total;
    private TextView cashLeft;
    private EditText userinput;

    private int sharesHave = 0;
    private Double cashBalance;
    private JSONObject stockRepository;

    private Button buy;
    private Button sell;

    String ticker = "";
    String name = "";
    Double last = 0.0;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view =  inflater.inflate(R.layout.trade_dialog, null);
        builder.setView(view);

        title = view.findViewById(R.id.tradeDialog_title);
        amount = view.findViewById(R.id.tradeDialog_amount);
        amount.setText("0");
        unit = view.findViewById(R.id.tradeDialog_unit);
        total = view.findViewById(R.id.tradeDialog_total2);

        cashLeft = view.findViewById(R.id.tradeDialog_cashLeft);
        userinput = view.findViewById(R.id.textInputEditText_amount);

        buy = view.findViewById(R.id.tradeDialog_button_buy);
        sell = view.findViewById(R.id.tradeDialog_button_sell);

        /** Get JSON string stroing stock info from the local meme**/
        sharedPreferences = getContext().getSharedPreferences("com.timzhang.stockapp", Context.MODE_PRIVATE);
        String response  = sharedPreferences.getString("purchased","{}");
        try {
            stockRepository = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadData();
        return builder.create();
    }

    public void loadData() {
        DecimalFormat df = new DecimalFormat("0.00");
        Bundle bundle = getArguments();
        ticker = bundle.getString("ticker");
        ticker = ticker.toUpperCase();
        name = bundle.getString("name");
        last = bundle.getDouble("last");
        String temp = " x $"+last+"/"+"share"+" = ";
        this.unit.setText(temp);

        //call JSON here
        sharesHave = getShareHaveFromStockRepo();
        Log.i("Share Have", ticker +" "+ String.valueOf(sharesHave));

        title.setText("Trade"+" "+name+" "+"shares");

        userinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                    String userinputvalue = userinput.getText().toString();
                    if(!TextUtils.isEmpty(userinputvalue) && userinputvalue.length()<6){
                        amount.setText(userinputvalue);
                        long num_shares = Long.valueOf(userinput.getText().toString());
                        double cost = last*num_shares;
                        total.setText(df.format(cost));
                    }else{
                        amount.setText("0");
                        total.setText("0");
                    }
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicktobuy();

            }
        });
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sell();
            }
        });
    }

    /**
     * when make a purchase
     */
    public void clicktobuy(){
        int num_stocks_purchased = Integer.valueOf(amount.getText().toString());
        if(num_stocks_purchased<=0){return;}
        sharesHave = getShareHaveFromStockRepo();
        sharesHave += num_stocks_purchased;
        stockRepository.remove(ticker);
        try {
            stockRepository.put(ticker,sharesHave);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Repository", stockRepository.toString());
        updateMemStockRepo();
    }
    /**
     * when sell owned stocks
     */
    public void sell(){
        int num_stock_sold = Integer.valueOf(amount.getText().toString());
        if(num_stock_sold<=0){return;}
        sharesHave = getShareHaveFromStockRepo();
        if(num_stock_sold>sharesHave){
            return;
        }
        else if(num_stock_sold == sharesHave){
            stockRepository.remove(ticker);
            updateMemStockRepo();
        }
        else{
            sharesHave -= num_stock_sold;
            stockRepository.remove(ticker);
            try {
                stockRepository.put(ticker, sharesHave);
                updateMemStockRepo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Repository", stockRepository.toString());
    }

    public void updateMemStockRepo(){
        sharedPreferences.edit().putString("purchased", stockRepository.toString()).commit();
        dismiss();

    }
    public Integer getShareHaveFromStockRepo(){
        if(!stockRepository.isNull(ticker)){
            try {
                return stockRepository.getInt(ticker);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}