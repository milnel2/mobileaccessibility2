/**
 * Steven Kwan | Malik Bouchet
 * University of Washington
 * 
 * This code has been adapted from  WordSnap OCR to perform 
 * general get requests from a php script and post requests.
 */

package cs.washington.mobileaccessibility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public final class PhpScriptAccess  {
    private static final String TAG = PhpScriptAccess.class.getSimpleName();

    private static final int HTTP_TIMEOUT = 6000; // in msec

    private static final String USER_AGENT_STRING = "LinkUp";
    
    private String mEndpoint;  // Store the URL of the php script
    private DefaultHttpClient mHttpClient; // Executes the requests

    public PhpScriptAccess (String endpoint) {
        mEndpoint = endpoint;
        
        // Set up the parameters for the HTTP GET Request
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setUserAgent(params, USER_AGENT_STRING);
        HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
     
        // Initialize the Client
        mHttpClient = new DefaultHttpClient(params);
    }
    
    /** 
     * getQuery
     * Performs a GET Request
     * LinkUp specific - Gets the list of locations
     * @return String array of the php request results
     * @throws IOException
     */
    
    public String[] getQuery(double longitude, double latitude) throws IOException {
    	String mEndpoint2 = mEndpoint  + "?latitude=" + latitude + "&longitude=" + longitude;
    	
    	Log.i(TAG, "Sending info request to " + mEndpoint2);
    	
    	// USED WITH FRIENDS
        // This get request sends the current location to calculate distances.
        // HttpGet get = new HttpGet(mEndpoint + "?latitude=" + latitude + "&longitude=" + longitude);
    	HttpGet get = new HttpGet(mEndpoint2);
    	
        // Send request and obtain response
        // Send request and obtain response
        return getAndProcessResults(get);
    }
    
    public String[] getQuery() throws IOException {
        Log.i(TAG, "Sending info request to " + mEndpoint);
        
        // This get request sends the current location to calculate distances.
        HttpGet get = new HttpGet(mEndpoint);

        // Send request and obtain response
        return getAndProcessResults(get);
    }
    
    /**
     * postQuery
     * Post information to a php script   
     * @param values - parameters for the post request
     * @return boolean representing success or fail
     * @throws IOException
     */
    public boolean postQuery(List<NameValuePair> values) throws IOException {
    	HttpPost httppost = new HttpPost(mEndpoint); 

        try {
                httppost.setEntity(new UrlEncodedFormEntity(values));
                Log.d(TAG, "execute");
                try {
                    HttpResponse response = mHttpClient.execute(httppost);
                    //return response.equals(o)
                    Log.d("myapp", "response " + response.getEntity());
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true; // currently always returning true.  not sure how to interpret the sql response from the php script            
    }
   
    /**
     * Process the results from the php script and return it in a String array
     * @param get - the connection
     * @return String array of the results
     * @throws IOException
     */
    private String[] getAndProcessResults(HttpGet get) throws IOException {
    	BufferedReader r = null;
        try {
            HttpResponse resp = mHttpClient.execute(get);
            r = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "utf-8"));
            Log.i(TAG, "Client has data.");
        } catch (NullPointerException npe) {
            Log.e(TAG, "Null entity?", npe);
            throw new IOException("HTTP request failed");
        } catch (HttpResponseException re) {
            Log.e(TAG, "HTTP response exception", re);
            throw new IOException("HTTP request failed");
        }

    	// prepare the number of results and results to send back to caller
        // Get the array size first
        String line = r.readLine(); // First number read from this should be the size of the array
        int size =  Integer.parseInt(line);
        
        // Get the results and create an array of them to be sent back to the caller
        String[] results = new String[size];
        int index = 0;
        
        line = r.readLine(); // get the next line which should be data
        while(index < size) { 
        	results[index] = line;
            line = r.readLine();
            index++;
        }
    	
        return results; 
    }
}
