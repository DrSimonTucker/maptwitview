package uk.ac.shef.dcs.oak.twitter.access;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.scribe.http.Request;
import org.scribe.http.Request.Verb;
import org.scribe.http.Response;
import org.scribe.oauth.Scribe;
import org.scribe.oauth.Token;

/**
 * My class for dealing with Twitter OAuth and related functionality
 * 
 * @author sat
 * 
 */
public class OAuthHandler
{
   /** The scribe singleton for handling OAuth */
   private static Scribe scribeSingleton;

   /**
    * Tester method
    * 
    * @param args
    *           Command line arguments
    * @throws Exception
    *            If something goes wrong
    */
   public static void main(final String[] args) throws Exception
   {
      OAuthHandler handler = new OAuthHandler();
      System.out.println(handler.getList("guardian", "music-staff", true, 1));
   }

   /**
    * Authenticates the user
    * 
    * @param optIn
    *           Flag indicating whether this person has opted in or not
    * @return The body of the authentication method
    * @throws IOException
    *            if something goes wrong on the network
    */
   public final String authenticate(final boolean optIn) throws IOException
   {
      return getBody("http://api.twitter.com/1/account/verify_credentials.xml", optIn);
   }

   /**
    * Creates the required property
    * 
    * @param propFile
    *           The file to store the properties in
    * @throws IOException
    *            If something goes wrong with writing the file
    */
   private void createPropsFile(final File propFile) throws IOException
   {
      Properties props = new Properties();

      // Get the consumer Key
      String consKey = "pBUj79RRk82JbO2eeKQ";
      String consSec = "DBuBIFPkzgZOdKWLnysCTUlNyOokBD1gJX7HtGFBdY";

      // Set the properties
      props.put("consumer.key", consKey);
      props.put("consumer.secret", consSec);
      props.put("request.token.verb", "POST");
      props.put("request.token.url", "http://twitter.com/oauth/request_token");
      props.put("access.token.verb", "POST");
      props.put("access.token.url", "http://twitter.com/oauth/access_token");
      props.put("callback.url", "oob");

      FileOutputStream fos = new FileOutputStream(propFile);
      props.storeToXML(fos, null);
      fos.close();
   }

   /**
    * Gets the access token for authentication
    * 
    * @return A valid Access Token
    * @throws IOException
    *            if the tokens cannot be found
    */
   private Token getAccessToken() throws IOException
   {
      File accessTokenFile = new File(System.getProperty("user.home") + File.separator
            + ".com262token");

      if (!accessTokenFile.exists())
         try
         {
            Scribe scribe = getScribe();
            Token tok = scribe.getRequestToken();

            // Take the user to the login page
            String url = "https://twitter.com/oauth/authenticate?oauth_token=" + tok.getToken();
            Desktop.getDesktop().browse(new URI(url));

            String verifier = JOptionPane.showInputDialog("Enter PIN Code:");
            Token aTok = scribe.getAccessToken(tok, verifier);

            // Store the access token
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(accessTokenFile));
            oos.writeObject(aTok);
            oos.close();
         }

         catch (URISyntaxException e)
         {
            e.printStackTrace();
         }

      try
      {
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(accessTokenFile));
         Token tok = (Token) ois.readObject();
         ois.close();
         return tok;
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      return null;
   }

   /**
    * Method to get all the public timeline statuses
    * 
    * @param optIn
    *           Flag indicated whether this person has opted in
    * @return A String of the response from Twitter of the public tweets
    * @throws IOException
    *            If something goes wrong with the network
    */
   public final String getAll(final boolean optIn) throws IOException
   {
      return getBody("http://api.twitter.com/1/statuses/public_timeline.xml", optIn);
   }

   /**
    * Gets the body of a url, using the relevant authentication methods
    * 
    * @param url
    *           The {@link String} of the URL to retrieve
    * @param optIn
    *           Flag inicating whether this person has opted in or not
    * @return A String of the data retrieved from Twitter
    * @throws IOException
    *            If something goes wrong with the network
    */
   private String getBody(final String url, final boolean optIn) throws IOException
   {
      // Deal with people that have opted out
      if (!optIn)
         return "";

      try
      {
         Token aToken = getAccessToken();
         Scribe scribe = getScribe();
         Request request = new Request(Verb.GET, url);
         scribe.signRequest(request, aToken);
         Response resp = request.send();
         return resp.getBody();
      }
      catch (RuntimeException e)
      {
         // Ignore
      }

      return "";
   }

   private String getBody(final String url)
   {
      Request request = new Request(Verb.GET, url);
      Response resp = request.send();
      return resp.getBody();
   }

   /**
    * Gets the status of all your friends on twitter
    * 
    * @param optIn
    *           Flag indicating this person has opted in to twitter
    * @return The body of the response from twitter for all your friends
    * @throws IOException
    *            If something goes wrong with the network
    */
   public final String getFriends(final boolean optIn, int page) throws IOException
   {
      return getBody("http://api.twitter.com/1/statuses/friends_timeline.xml?count=200&page="
            + page, optIn);
   }

   public final String getSolrData(int n) throws IOException
   {
      return getBody("http://nebula.dcs.shef.ac.uk/solr/select/?q=*%3A*&version=2.2&start=0&rows="
            + n + "&indent=on");
   }

   /**
    * Gets the status of all your personal tweets
    * 
    * @param optIn
    *           Flag indicating this person has opted in to using twitter
    * @return Twitter response of all your tweets
    * @throws IOException
    *            If something goes wrong with the network
    */
   public final String getHome(final boolean optIn) throws IOException
   {
      return getBody("http://api.twitter.com/1/statuses/user_timeline.xml", optIn);
   }

   public final String getList(String name, String list, boolean optIn, int page)
         throws IOException
   {
      return getBody("https://api.twitter.com/1/lists/statuses.xml?per_page=50&owner_screen_name="
            + name + "&slug=" + list + "&page=" + page, optIn);
   }

   public final String getPosts(String name, int page) throws IOException
   {
      return getBody(
            "https://api.twitter.com/1/statuses/user_timeline.xml?per_page=50&screen_name=" + name
                  + "&page=" + page, true);
   }

   /**
    * Get the properties stored for this used
    * 
    * @return A valid Properties mapping for this user
    * @throws IOException
    *            If the properties file can't be found or read
    */
   private Properties getProperties() throws IOException
   {
      String propertiesLocation = System.getProperty("user.home") + File.separator
            + ".com262-twitter.properties";
      File f = new File(propertiesLocation);
      if (!f.exists())
         createPropsFile(f);

      Properties props = new Properties();
      InputStream fis = new FileInputStream(f);
      props.loadFromXML(fis);
      fis.close();

      return props;
   }

   /**
    * Helper method to get the scribe singleton
    * 
    * @return {@link Scribe} object
    * @throws IOException
    *            If something goes wrong with Scribe
    */
   private Scribe getScribe() throws IOException
   {
      if (scribeSingleton == null)
         scribeSingleton = new Scribe(getProperties());
      return scribeSingleton;
   }

   public final String getSubscribers(final String owner, final String list) throws IOException
   {
      return getBody("http://api.twitter.com/1/lists/members.xml?slug=" + list
            + "&owner_screen_name=" + owner, true);
   }

   /**
    * Get the home timeline response from Twitter
    * 
    * @param optIn
    *           Flag indicating that this user has opted in
    * @return The XML string of the response from Twitter
    * @throws IOException
    *            if something goes wrong with the network
    */
   public final String getTimelineString(final boolean optIn) throws IOException
   {
      return getBody("http://api.twitter.com/1/statuses/home_timeline.xml", optIn);
   }
}
