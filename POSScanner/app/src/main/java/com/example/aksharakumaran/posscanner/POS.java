package com.example.aksharakumaran.posscanner;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class POS extends AppCompatActivity implements OnClickListener {
    String value;
    String splitValue="\n\n\n";
    private Button scanBtn;
    private TextView formatTxt, contentTxt;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);
        scanBtn = (Button) findViewById(R.id.scan_button);
//        formatTxt = (TextView) findViewById(R.id.scan_format);
//        contentTxt = (TextView) findViewById(R.id.scan_content);
        scanBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.scan_button) {
//            TextView txt1= (TextView) findViewById(R.id.textView3);
//            txt1.setText("");
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
//            formatTxt.setText("FORMAT: " + scanFormat);
//            contentTxt.setText("CONTENT: " + scanContent);
            Log.d("Scan Format  ", scanFormat);
            Log.d("Scan Content ", scanContent);
            final TextView textView = (TextView) findViewById(R.id.textView1);
            textView.setText(" Coupons Clipped For " + scanContent);
// web service call
        String urlString = "http://192.168.43.2:8080/CrunchifyRESTJerseyExample/crunchify/getDeptCoupons";
        new CallAPI().execute(urlString);

        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //Log.d("WS Data ", client.getConnectionResult(AppIndex).toString());
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
        private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            InputStream in = null;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                value="";
                value = readInputStreamToString(urlConnection);
                Log.i("String Value", value);
                String delimiter = ";";
                StringTokenizer stringTokenizer=new StringTokenizer(value,delimiter);
                splitValue ="\n\n\n";
                while(stringTokenizer.hasMoreElements()){
                    splitValue=splitValue+stringTokenizer.nextToken()+"\n";
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
            return value;
        }
            private String readInputStreamToString(HttpURLConnection connection)
            {
                String result = null;
                StringBuffer sb = new StringBuffer();
                InputStream is = null;

                try {
                    is = new BufferedInputStream(connection.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String inputLine = "";
                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    result = sb.toString();
                }
                catch (Exception e) {
                    Log.i("Exception 1", "Error reading InputStream");
                    result = null;
                }
                finally {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (IOException e) {
                            Log.i("Excep 2", "Error closing InputStream");
                        }
                    }
                }

                return result;
            }

            protected void onPostExecute(String result) {
                TextView txt1= (TextView) findViewById(R.id.textView3);
                txt1.setText("");
                final TextView textViewCoupon = (TextView) findViewById(R.id.textView3);
                System.out.println("value Content before "+ splitValue);
                textViewCoupon.setText(splitValue);
        }
    } // end CallAPI

}