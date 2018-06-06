package pl.f4.regatta;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static pl.f4.regatta.HttpClient.*;
import static pl.f4.regatta.HttpClient.website;

public class JoinedListRegattsActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener  {

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private Button button;
    private RecyclerView recyclerView;

    MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_list_regatts);

        //button = (Button) findViewById(R.id.button2);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

//        try {
//            cookie();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mAuthTask = new UserLoginTask("", "", this);
        mAuthTask.execute((Void) null);

        // data to populate the RecyclerView with
//        ArrayList<String> animalNames = new ArrayList<>();
//        animalNames.add("Horse");
//        animalNames.add("Cow");
//        animalNames.add("Camel");
//        animalNames.add("Sheep");
//        animalNames.add("Goat");
//
//        // set up the RecyclerView
//        RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new MyRecyclerViewAdapter(this, animalNames);
//        adapter.setClickListener(this);
//        recyclerView.setAdapter(adapter);
    }

    protected void cookie() throws IOException {
        //HttpResponse response = HttpClient.sendGet("/api/captain-events");
        HttpResponse response;
        DefaultHttpClient httpclient = getInstance();
        HttpGet httpget = new HttpGet(website+"/api/captain-events");

        httpget.setHeader("X-XSRF-TOKEN", HttpClient.getXSRFTOKEN());
        httpget.setHeader("JSESSIONID", HttpClient.getJSESSIONID());

        response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();


        System.out.println("Login form get: " + response.getStatusLine());
        if (entity != null) {
            entity.consumeContent();
        }

        System.out.println("Post logon cookies:");
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());

            }
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        //httpclient.getConnectionManager().shutdown();
        ///api/captain-events
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        finish();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        public String JSESSIONID;
        public String XSRFTOKEN;
        private JoinedListRegattsActivity activity;
        DefaultHttpClient httpclient;
        //String website = "http://vps485240.ovh.net:8080";
        String website = "http://192.168.0.150:8080";

        UserLoginTask(String email, String password, JoinedListRegattsActivity activity) {
            mEmail = email;
            mPassword = password;
            this.activity = activity;
        }

        protected void cookie() throws IOException {

            //HttpResponse response = HttpClient.sendGet("/api/captain-events");
            HttpResponse response;
            DefaultHttpClient httpclient = getInstance();
            HttpGet httpget = new HttpGet(website+"/api/captain-events");

            httpget.setHeader("X-XSRF-TOKEN", HttpClient.getXSRFTOKEN());
            httpget.setHeader("JSESSIONID", HttpClient.getJSESSIONID());

            ResponseHandler<String> handler = new BasicResponseHandler();
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            String body = handler.handleResponse(response);
            int code = response.getStatusLine().getStatusCode();

            ArrayList<String> eventsAttendedList = new ArrayList<>();;

            JSONObject fieldsJson = null;
            try {
                fieldsJson = new JSONObject(body);
                JSONArray keys = fieldsJson.names();
                for (int i = 1; i <= keys.length(); i++){
                    JSONObject jsonobject = fieldsJson.getJSONObject(String.valueOf(i));
                    String id = jsonobject.getString("id");
                    String name = jsonobject.getString("name");
                    eventsAttendedList.add(String.valueOf(i) + id + " " + name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            adapter = new MyRecyclerViewAdapter(activity, eventsAttendedList);
            adapter.setClickListener(activity);
            recyclerView.setAdapter(adapter);

            System.out.println("Login form get: " + response.getStatusLine());
            if (entity != null) {
                entity.consumeContent();
            }

            System.out.println("Post logon cookies:");
            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            if (cookies.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("- " + cookies.get(i).toString());

                }
            }
        }



        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                cookie();
                Thread.sleep(20);
                //launchMainActivity();
            } catch (InterruptedException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                //launchMainActivity();
                //finish();

            } else {
;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }
    }
}
