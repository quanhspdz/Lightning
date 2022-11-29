package com.example.lightning.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightning.R;
import com.example.lightning.tools.Tool;

public class WalletActivity extends AppCompatActivity {

    RelativeLayout relativeAddMoney, relativeHistory, relativeShowAddMoney, relativeShowHistory;
    FrameLayout frameAddMoney, frameHistory;
    EditText edtAmount;
    AppCompatButton buttonOk, buttonConfirm;
    TextView textFormattedMoney;

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
        edtAmount = findViewById(R.id.edt_money);
        buttonOk = findViewById(R.id.buttonOk);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        textFormattedMoney = findViewById(R.id.text_formatted_money);
        relativeShowAddMoney = findViewById(R.id.relativeShowAddMoney);
        relativeShowHistory = findViewById(R.id.relativeShowHistory);

        addMoneyIsChosen = true;
        historyIsChosen = false;
    }

    private void listener() {
        relativeAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameAddMoney.setVisibility(View.VISIBLE);
                frameHistory.setVisibility(View.GONE);
                relativeShowAddMoney.setVisibility(View.VISIBLE);
                relativeShowHistory.setVisibility(View.GONE);
                edtAmount.setText("");
                textFormattedMoney.setText("");
                addMoneyIsChosen = true;
                historyIsChosen = false;
            }
        });

        relativeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameHistory.setVisibility(View.VISIBLE);
                frameAddMoney.setVisibility(View.GONE);
                relativeShowAddMoney.setVisibility(View.GONE);
                relativeShowHistory.setVisibility(View.VISIBLE);
                edtAmount.setText("");
                textFormattedMoney.setText("");
                addMoneyIsChosen = false;
                historyIsChosen = true;
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = edtAmount.getText().toString().trim();
                if (!amount.isEmpty()) {
                    textFormattedMoney.setText(Tool.getCurrencyFormat(Double.parseDouble(amount)));
                    Tool.hideSoftKeyboard(WalletActivity.this);
                } else {
                    Toast.makeText(WalletActivity.this, "Please enter the amount of money!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}