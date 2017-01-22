package istd.eric.hacknroll;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainScreen extends AppCompatActivity {

    MainScreen.MyClientTask client;
    String link = "http://ericteo50dot001xc7e.appspot.com";
    FoodStruct[] unlockfood = {
        new FoodStruct("salad",1000),
        new FoodStruct("egg",2000),
        new FoodStruct("porridge",3000),
        new FoodStruct("ccf",4000),
        new FoodStruct("pizza",5000),
        new FoodStruct("wtnoodle",6000),
        new FoodStruct("burger",7000),
        new FoodStruct("grilledcheese",8000),
        new FoodStruct("chickenrice",9000),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = new MainScreen.MyClientTask();
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

    public void reset(View v){
        ResetTask resettask = new ResetTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {    // http://stackoverflow.com/questions/13647869
            resettask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // Need thread pool for concurrent async task, otherwise sequential.
        } else {
            resettask.execute();
        }
    }

    public class ResetTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void[] arg0) {
            try {
                URL url = new URL(link+"/init");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.getInputStream();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            TextView step_textview = (TextView) findViewById(R.id.step_textview);
            step_textview.setText("0");

            TextView calories_textview = (TextView) findViewById(R.id.calories_textview);
            calories_textview.setText("0");

            TextView too_long_textview = (TextView) findViewById(R.id.too_long_textview);
            too_long_textview.setText("");

            for (FoodStruct food : unlockfood) {
                int viewId = getResources().getIdentifier(food.food, "id", getPackageName());
                ImageView food_image = (ImageView) findViewById(viewId);
                food_image.setAlpha((float) 0.2);
            }
            ImageView special_food = (ImageView) findViewById(R.id.nasi);
            special_food.setImageDrawable(getDrawable(R.drawable.qmark));
            special_food.setAlpha((float) 0.2);
        }
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
            TextView step_textview = (TextView) findViewById(R.id.step_textview);
            step_textview.setText(String.valueOf(steps));

            TextView calories_textview = (TextView) findViewById(R.id.calories_textview);
            calories_textview.setText(String.format( "%.2f", steps*0.044 ));

            TextView too_long_textview = (TextView) findViewById(R.id.too_long_textview);
            if (too_long){
                too_long_textview.setText("You've been standing for too long! Exercise your legs!");
            } else {
                too_long_textview.setText("");
            }

            for (FoodStruct food : unlockfood) {
                int viewId = getResources().getIdentifier(food.food, "id", getPackageName());
                ImageView food_image = (ImageView) findViewById(viewId);
                if (steps >= food.unlock){
                    food_image.setAlpha((float) 1.0);
                } else {
                    food_image.setAlpha((float) 0.2);
                }
            }
            ImageView special_food = (ImageView) findViewById(R.id.nasi);
            if (steps >= 30000){
                special_food.setImageDrawable(getDrawable(R.drawable.nasibiryanic));
                special_food.setAlpha((float) 1.0);
            } else {
                special_food.setImageDrawable(getDrawable(R.drawable.qmark));
                special_food.setAlpha((float) 0.2);
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Disable for now
        // getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class FoodStruct {
    public String food;
    public int unlock;

    public FoodStruct(String food, int unlock){
        this.food = food;
        this.unlock = unlock;
    }
}