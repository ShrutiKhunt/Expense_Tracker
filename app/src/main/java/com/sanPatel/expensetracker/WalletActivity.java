package com.sanPatel.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sanPatel.expensetracker.Adapter.MyExpenseRecyclerViewAdapter;
import com.sanPatel.expensetracker.AsyncTask.MyAsyncTask;
import com.sanPatel.expensetracker.Database.SqliteDatabase.SqliteDatabaseHelper;
import com.sanPatel.expensetracker.Datas.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class WalletActivity extends AppCompatActivity {

    // widgets
    private RecyclerView recyclerViewTranscation;
    private Toolbar toolbar;
    private TextView tvAvailableBal, tvTotal, tvSpend;

    // instance variable
    private static final String TAG = "WalletActivity";
    private int walletID;
    private ArrayList<Expense> expenseList;
    private MyExpenseRecyclerViewAdapter adapter;
    private double initialBal;

    public void addEntry(View view) {
        // this method open AddExpenseActivity.
        Intent intent = new Intent(getApplicationContext(), AddExpenseActivity.class);
        intent.putExtra("Activity","wallet");
        intent.putExtra("Wallet_id", walletID);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        initializeWidgets();
        getIntentData();

        recyclerViewTranscation.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewTranscation.setHasFixedSize(true);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransaction();
        setWalletBal();
    }

    private void initializeWidgets() {
        // this method will initialize all the widgets.
        recyclerViewTranscation = findViewById(R.id.recycler_view_transactions);
        toolbar = findViewById(R.id.toolbar_wallet);
        tvAvailableBal = findViewById(R.id.text_view_avialable_amount_value);
        tvTotal = findViewById(R.id.text_view_total_amount_value);
        tvSpend = findViewById(R.id.text_view_spend_amount_value);
    }

    private void getIntentData() {
        // this method will get all the data passed by the intent.
        walletID = getIntent().getIntExtra("Wallet_id",-1);
        toolbar.setTitle(getIntent().getStringExtra("Wallet_name"));
    }

    private void fetchTransaction() {
        // this method will fetch all the transaction belonging to the wallet from the database.
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setAsyncTaskListener(new MyAsyncTask.AsyncTaskListener() {
            @Override
            public void setBackgroundTask() {
                SqliteDatabaseHelper databaseHelper = new SqliteDatabaseHelper(getApplicationContext());
                Cursor cursor = databaseHelper.getExpenseForWallet(walletID);
                if (cursor.getCount() > 0) {
                    // expense for walletId is available.
                    Log.d(TAG, "setBackgroundTask: cursor: "+cursor.getCount());
                    expenseList = new ArrayList<>();
                    try {
                        while (cursor.moveToNext()) {
                            Expense expense = new Expense();
                            expense.setExpense_id(cursor.getInt(0));
                            expense.setExpense_title(cursor.getString(1));
                            expense.setExpense_description(cursor.getString(2));
                            expense.setExpense_amount(cursor.getDouble(3));
                            expense.setTime(cursor.getString(5));
                            expense.setExpense_type(cursor.getInt(6));
                            expense.setExpense_date(new SimpleDateFormat("dd-MM-yyyy").parse(cursor.getString(4)));
                            expense.setSync(cursor.getInt(7));
                            expense.setWalletID(cursor.getInt(8));

                            expenseList.add(expense);
                        }
                    } catch (Exception e) {

                    }

                } else {
                    // there is no expense for walletId.
                    Log.d(TAG, "setBackgroundTask: Nothing");
                }
            }

            @Override
            public void setPostExecuteTask() {
                if (expenseList.size() > 0) {
                    adapter = new MyExpenseRecyclerViewAdapter(expenseList, getApplicationContext());
                    recyclerViewTranscation.setAdapter(adapter);
                }
            }
        });

        myAsyncTask.execute();
    }

    public void getWalletDetails() {
        // this method will get all the details of wallet.
        SqliteDatabaseHelper databaseHelper = new SqliteDatabaseHelper(getApplicationContext());
        Cursor cursor = databaseHelper.getWallet(walletID);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            toolbar.setTitle(cursor.getString(1));
            initialBal = cursor.getDouble(2);
        }
    }

    public void setWalletBal() {
        // this method will set the wallet available balance, total, spend.
        getWalletDetails();
        tvTotal.setText(Double.toString(initialBal));
    }
}