package pl.f4.regatta;

import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HttpClient {

    static DefaultHttpClient instance;
    static MainActivity mainActivity;

    //static String website = "http://vps485240.ovh.net:8080";
    static String website = "http://192.168.0.150:8080";

    static String JSESSIONID;
    static String XSRFTOKEN;

    public static DefaultHttpClient getInstance(){
        if(instance == null){
            instance = new DefaultHttpClient ();
        }

        return instance;
    }

    public static HttpResponse sendGet(String api) throws IOException{
        HttpGet httpget = new HttpGet(website + api);

        if(XSRFTOKEN != null)
            httpget.setHeader("X-XSRF-TOKEN", XSRFTOKEN);
        if(XSRFTOKEN != null)
            httpget.setHeader("JSESSIONID", JSESSIONID);

        HttpResponse response = getInstance().execute(httpget);

        return response;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static String getJSESSIONID() {
        return JSESSIONID;
    }

    public static void setJSESSIONID(String token) {
        if(JSESSIONID == null)
            JSESSIONID = token;
    }

    public static String getXSRFTOKEN() {
        return XSRFTOKEN;
    }

    public static void setXSRFTOKEN(String token) {
        if(XSRFTOKEN == null)
            XSRFTOKEN = token;
    }

    protected void finalize ()  {
        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        instance.getConnectionManager().shutdown();
    }

    public static class SendPositionTask extends AsyncTask<Void, Void, Boolean> {
        private final String shortTime;

        //private MainActivity activity;

        //private Long eventId;
        //private Long id;
        private Double lat;
        private Double lng;
        private String time;
        private Long teamId;
        //String website = "http://vps485240.ovh.net:8080";
        String website = "http://192.168.0.150:8080";
        String api = "/api/positions";


        public SendPositionTask(Long teamId, Double lat, Double lng, String time) {
            this.teamId = teamId;
            this.lat = lat;
            this.lng = lng;
            this.shortTime = time;
            if(time.contains("PM")) {
                this.time = "2018-06-06" + "T1" + time.replace("PM","").replace(" ","") + "+02:00[Europe/Warsaw]";
            } else {
                this.time =  "2018-06-06" + "T0" + time.replace("AM","").replace(" ","") + "+02:00[Europe/Warsaw]";
            }
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

            try {
                JSONArray jsonarray = new JSONArray(body);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    String id = jsonobject.getString("id");
                    String name = jsonobject.getString("name");
                    eventsAttendedList.add(id + " " + name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Thread.sleep(20);
                sendLocation();
            } catch (InterruptedException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        private void sendLocation() throws IOException {
            HttpPost httpost = new HttpPost(website + api);

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();

            //nvps.add(new BasicNameValuePair("eventId", eventId.toString())); //            private Long eventId;
//            nvps.add(new BasicNameValuePair("lat", lat.toString()));//            private Double lat;
//            nvps.add(new BasicNameValuePair("lng", lng.toString()));//            private Double lng;
//            nvps.add(new BasicNameValuePair("time", time));//            private String time;
//            nvps.add(new BasicNameValuePair("teamId", teamId.toString())); //private Long teamId;
//            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));


            HashMap<String,String> mapa = new HashMap<String,String>();
            mapa.put("lat",lat.toString());
            mapa.put("lng", lng.toString());
            //mapa.put("time", "2018-06-06" + "T" + time.replace("PM","").replace("AM","").replace(" ","") + "+02:00[Europe/Warsaw]");
            mapa.put("time", time);
            mapa.put("teamId", teamId.toString());

            JSONObject json = new JSONObject(mapa);
            StringEntity entityJson = new StringEntity(json.toString());
            httpost.setEntity(entityJson);

            UrlEncodedFormEntity dd = new UrlEncodedFormEntity(nvps, "UTF-8");
            httpost.setHeader("X-XSRF-TOKEN", HttpClient.getXSRFTOKEN());
            httpost.setHeader("JSESSIONID", HttpClient.getJSESSIONID());
            httpost.setHeader("Content-Type", "application/json");



            HttpResponse response = null;
            response = getInstance().execute(httpost);

            HttpEntity entity = response.getEntity();
            int code = response.getStatusLine().getStatusCode();

            if (entity != null) {
                entity.consumeContent();
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //mAuthTask = null;
            if (success) {
                //launchMainActivity();
                //finish();
                Toast.makeText(
                        HttpClient.getMainActivity(),
                        "Send position Lat:" + lat.toString() +
                            ", Lng:" + lng.toString() +
                            ", Time:" + shortTime,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HttpClient.getMainActivity(), "Not send position", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;

        }
    }



}
