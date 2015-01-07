package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class SomToday_login {
	  /**
     * 1) login through login form
     * 2) grab the JSESSIONID-cookie
     * 3) use this cookie to grab photo
     * */
	
	
	final static String mAfkorting = "csgliudger"; //my school
	
	public static String getLoggedInCookie(String username, String password){
		String result = "";

		try{
			String JSESSIONID_COOKIE = "";
			
	
	        DefaultHttpClient httpclient = new DefaultHttpClient();
	
	        HttpGet httpget = new HttpGet("https://"+ mAfkorting +".somtoday.nl");
	
	        HttpResponse response = httpclient.execute(httpget);
	        HttpEntity entity = response.getEntity();
	
	        System.out.println("Initial set of cookies:");
	        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
	      
	        JSESSIONID_COOKIE = getJSESSIONIDfromList(cookies);
	
	
	        System.out.println("JSESSIONID_before_login = "+JSESSIONID_COOKIE);
	        
	        /**
	         * Now we have the cookie, which is assigned to you when you first open the page, no idea if we need it, but we have it.
	         * Now we are posting to a url, faking we are logging in.
	         * */
	        
	        String url = "https://"+ mAfkorting +".somtoday.nl/?-1.IFormSubmitListener-content-upp-signInForm";

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // The jsessionid we grabbed earlier
            conn.addRequestProperty("Cookie" , "JSESSIONID="+JSESSIONID_COOKIE);

            //The post values
            List <NameValuePair> formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("id2_hf_0" , ""));
            formData.add(new BasicNameValuePair("loginLink", "Submit+Query"));
            formData.add(new BasicNameValuePair("usernameFieldPanel:usernameFieldPanel_body:usernameField", username));
            formData.add(new BasicNameValuePair("passwordFieldPanel:passwordFieldPanel_body:passwordField", password));

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
            writer.write( getQuery(formData) );
            writer.flush();
            writer.close();
            os.close();
            
            
            if(conn.getResponseCode() == 200) {

                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String html = builder.toString();
                
                
                /**
                 * On this HTML file is a Form, and if you press that button it sends off a POST request to 'http://www.csgliudger.somtoday.nl/auth'
                 * With 1 post value ; SAMLResponse.
                 * This 'Token' is a hidden input in the form.
                 * 
                 * So we grab the token, POST a request to /auth and grab the cookie from that :)
                 * */
                                
                String SAMLResponse = html.split("<input type=\"hidden\" name=\"SAMLResponse\"")[1].split("value=\"")[1].split("\"")[0];
                
                System.out.println(SAMLResponse);

                
                DefaultHttpClient finalClient = new DefaultHttpClient();
                HttpPost httpPostGetJSESSIONID = new HttpPost("https://" + mAfkorting + "-elo.somtoday.nl/auth");

                formData.clear();
                formData.add(new BasicNameValuePair("SAMLResponse", SAMLResponse));

                httpPostGetJSESSIONID.setEntity(new UrlEncodedFormEntity(formData));

                HttpResponse reponseGetJSESSIONID = finalClient.execute(httpPostGetJSESSIONID);

                List<Cookie> cookieArray = finalClient.getCookieStore().getCookies();
                
                String loggedInJSESSIONID = getJSESSIONIDfromList(cookieArray);
                
                /**
                 * Now we have sucesfully grabbed the logged in JSESSIONID, now we can do what we want with it.
                 * */
                
                return loggedInJSESSIONID;
                
                
                
            }else{
            	System.out.println("No status code 200, code "+ conn.getResponseCode() +" instead.");
            }
            
	        
	        
		}catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}
	
	private static String getJSESSIONIDfromList(List<Cookie> c){
		if (c.isEmpty()) {
            System.out.println("No cookies");
            return null;
        } else {
            for (int i = 0; i < c.size(); i++) {

                System.out.println(c.get(i).toString());

                if(c.get(i).getName().equals("JSESSIONID")){
                    String jsessionID = c.get(i).getValue();
                    System.out.println("Found JSESSIONID cookie : " + jsessionID);
                    
                    return jsessionID;
                }
            }
        }
		
		return null;
	}
	
	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		 StringBuilder result = new StringBuilder();
		 boolean first = true;

		 for (NameValuePair pair : params){
			 if (first){
				 first = false;
			 }else{
				 result.append("&");
			 }

			 result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			 result.append("=");
			 result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	            
		 }

		 return result.toString();
	 }
	
}
