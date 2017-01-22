package istd.eric.hacknroll;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowStep extends AppCompatActivity {

    ShowStep.MyClientTask client;
    String link = "http://ericteo50dot001xc7e.appspot.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_step);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = new ShowStep.MyClientTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // http://stackoverflow.com/questions/13647869
            client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // Need thread pool for concurrent async task, otherwise sequential.
        } else {
            client.execute();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        client.cancel(true);
    }


    public void move(View v){
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        int steps;
        boolean too_long;

        @Override
        protected Void doInBackground(Void[] arg0) {
            while (true) {
                if (this.isCancelled()){
                    return null;
                }
                try {
                    URL url = new URL(link+"/read");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        steps = Integer.parseInt( in.readLine() );
                        too_long = Boolean.parseBoolean( in.readLine() );

                        publishProgress();
                        try{ Thread.sleep(3000);}catch(InterruptedException e){ }

                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            TextView step_textview = (TextView) findViewById(R.id.stepsbig);
            step_textview.setText(String.valueOf(steps));
        }
    }
}
