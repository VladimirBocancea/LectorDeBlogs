package es.carlostessier.lectordeblogs;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
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
    public static int NUMBER_OF_POSTS = 10;
    public static String TAG  = MainActivity.class.getSimpleName();
    public static String BLOG = "http://android-developers.blogspot.com.es/";
    public static String URL_JSON =BLOG+"/feeds/posts/default?alt=json&max-results="+NUMBER_OF_POSTS;
    private boolean networkAvailable;
    private JSONObject mBlogData;

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
        if (mBlogPostTitles != null)
            findViewById(R.id.empty).setVisibility(View.INVISIBLE);


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

    private class GetBlogPostsTask extends AsyncTask <Object, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(Object[] params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
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

                    jsonResponse = new JSONObject(responseData);



                }
                else Log.i(TAG, "Conexión fallida: " + responseCode);

            } catch (MalformedURLException e) {
                Log.e(TAG,"exception caught:",e);
            } catch (IOException e) {
                Log.e(TAG, "exception caught:", e);
            } catch (Exception e) {
                Log.e(TAG, "exception caught:", e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            mBlogData = result;
            updateList();
        }

    }

    private void updateList() {
        if (mBlogData == null) {
            // TODO: Manejar errores
        } else {
            // Log.d(TAG,mBlogData.toString());
            try {
                JSONObject jsonFeed = mBlogData.getJSONObject("feed");
                JSONArray jsonAentry = null;

                jsonAentry = jsonFeed.getJSONArray("entry");

                mBlogPostTitles = new String[jsonAentry.length()];

                for (int i = 0; i < jsonAentry.length(); i++) {
                    JSONObject jsonPost = (JSONObject) jsonAentry.get(i);
                    JSONObject jsonTitle = (JSONObject) jsonPost.get("title");

                    //String title = Html.escapeHtml(jsonTitle.getString("$t"));
                    String title =  Html.fromHtml(jsonTitle.getString("$t")).toString();

                    mBlogPostTitles[i] = title;

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_expandable_list_item_1,mBlogPostTitles);
                    setListAdapter(adapter);
                    findViewById(R.id.empty).setVisibility(View.INVISIBLE);

                }
            } catch (JSONException e) {
                Log.e(TAG, "exception caught:", e);
            }
        }
    }
}
