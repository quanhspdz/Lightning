package com.example.lightning.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.lightning.R;

public class WalletActivity extends AppCompatActivity {

    RelativeLayout relativeAddMoney, relativeHistory;
    FrameLayout frameAddMoney, frameHistory;

    boolean addMoneyIsChosen, historyIsChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        init();
        listener();
    }

    private void init() {
        relativeAddMoney = findViewById(R.id.relativeAddMoney);
        relativeHistory = findViewById(R.id.relativeHistory);
        frameAddMoney = findViewById(R.id.frame_add_money_bot);
        frameHistory = findViewById(R.id.frame_history_bot);

        addMoneyIsChosen = true;
        historyIsChosen = false;
    }

    private void listener() {
        relativeAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameAddMoney.setVisibility(View.VISIBLE);
                frameHistory.setVisibility(View.GONE);
                addMoneyIsChosen = true;
                historyIsChosen = false;
            }
        });

        relativeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameHistory.setVisibility(View.VISIBLE);
                frameAddMoney.setVisibility(View.GONE);
                addMoneyIsChosen = false;
                historyIsChosen = true;
            }
        });
    }
}