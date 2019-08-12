package com.example.fetchip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private DownloadWebTask downloadWebTask;
    public static final String QUERY = "https://api.ipify.org?format=json";

    private ArrayList<String> history;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.history = new ArrayList<>();
        this.arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, history);

        this.listView = findViewById(R.id.listView_history_ip);
        this.listView.setAdapter(arrayAdapter);
    }

    public void myButtonHandler(View view) {
        /* Notify us the state of the network */
        if (isConnected()) {
            Toast.makeText(this, "Successfully Connected", Toast.LENGTH_SHORT).show();
            this.downloadWebTask = new DownloadWebTask();
            downloadWebTask.execute(QUERY);
        } else {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void ClearHandler(View view) {
        if (this.arrayAdapter == null) {
            Toast.makeText(this, "Failed to Clear the list", Toast.LENGTH_SHORT).show();
        } else if (this.arrayAdapter.isEmpty()) {
            Toast.makeText(this, "The list is Empty", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Successfully Cleared", Toast.LENGTH_SHORT).show();
            this.arrayAdapter.clear();
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private class DownloadWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("Query", "Query => " + params[0]);

                /* Allow to create the URL */
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(2000);
                conn.setConnectTimeout(2500);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                /* Collect data > InputStream */
                InputStream is = conn.getInputStream();

                /* Allow to read the InputStream */
                char[] buffer = new char[100];
                StringBuilder sb = new StringBuilder();
                Reader in = new InputStreamReader(is, "UTF-8");
                for (; ; ) {
                    int result = in.read(buffer, 0, buffer.length);
                    if (result < 0) break;
                    sb.append(buffer, 0, result);
                }

                Log.i("Resultat", "Resultat => " + sb.toString());
                return sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {

            try {
                JSONObject json = new JSONObject(data);
                String ip = json.getString("ip");

                // Display the data in UI
                Date currentTime = Calendar.getInstance().getTime();

                String input = ("Time : [ " + currentTime.getTime() + " ]  IP : [ " + ip + " ]");
                arrayAdapter.add(input);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
