package es.carlostessier.lectordeblogs;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ListActivity {

    protected String[] mBlogPostTitles;
    public static int NUMBER_OF_POSTS = 5;
    public static String TAG  = MainActivity.class.getSimpleName();
    public static String URL_JSON ="http://itvocationalteacher.blogspot.com/feeds/posts/default?alt=json&max-results="+NUMBER_OF_POSTS;
    private boolean networkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isNetworkAvailable()) {
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        }
        else {
            Toast.makeText(this, R.string.no_connection_message, Toast.LENGTH_LONG).show();
        }
        if (mBlogPostTitles != null) findViewById(R.id.empty).setVisibility(View.INVISIBLE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public boolean isNetworkAvailable() {

        boolean isAvailable = false;

        ConnectivityManager  manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isAvailable()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private class GetBlogPostsTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            int responseCode = -1;
            try {
                URL blogFeedUrl = new URL(URL_JSON);
                HttpURLConnection connection  = (HttpURLConnection) blogFeedUrl.openConnection();
                responseCode = connection.getResponseCode();

                if(responseCode==HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                /*    Reader reader = new InputStreamReader(inputStream);

                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);

                     String responseData = new String(charArray);

*/
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);

                    String responseData = responseStrBuilder.toString();

                    JSONObject jsonResponse = new JSONObject(responseData);

                    JSONObject jsonFeed = jsonResponse.getJSONObject("feed");
                    JSONArray jsonAentry = jsonFeed.getJSONArray("entry");

                    for (int i = 0; i < jsonAentry.length(); i++) {
                        JSONObject jsonPost = (JSONObject) jsonAentry.get(i);
                        JSONObject jsonTitle = (JSONObject) jsonPost.get("title");
                        String title = jsonTitle.getString("$t");
                        Log.v(TAG, title);
                    }


                }
                else Log.i(TAG, "Conexión fallida: " + responseCode);

            } catch (MalformedURLException e) {
                Log.e(TAG,"exception caught:",e);
            } catch (IOException e) {
                Log.e(TAG, "exception caught:", e);
            } catch (Exception e) {
                Log.e(TAG, "exception caught:", e);
            }

            return "Code: " + responseCode;
        }
    }
}
