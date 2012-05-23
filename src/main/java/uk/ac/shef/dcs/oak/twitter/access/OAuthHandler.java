package uk.ac.shef.dcs.oak.twitter.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * My class for dealing with Twitter OAuth and related functionality
 * 
 * @author sat
 * 
 */
public class OAuthHandler
{

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
         String text = "";
         System.out.println("Getting " + url);
         BufferedReader reader = new BufferedReader(
               new InputStreamReader(new URL(url).openStream()));
         for (String line = reader.readLine(); line != null; line = reader.readLine())
            text += line;
         reader.close();

         return text;
      }
      catch (IOException e)
      {
         // Ignore
      }

      return "";
   }

   private String getBody(final String url) throws IOException
   {
      return getBody(url, true);
   }

   private String getBody(File f) throws IOException
   {
      String lines = "";
      BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
            new FileInputStream(f))));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
         lines += line;
      reader.close();
      return lines;
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
      String url = "http://nebula.dcs.shef.ac.uk/solr/select/?q=*%3A*&version=2.2&start=0&rows="
            + n + "&indent=on";
      File rF = new File("tmp/" + n + ".txt.gz");
      if (rF.exists())
         return getBody(rF);

      String body = getBody(url);
      try
      {
         rF.createNewFile();
         PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(rF)));
         ps.print(body);
         ps.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return body;
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
