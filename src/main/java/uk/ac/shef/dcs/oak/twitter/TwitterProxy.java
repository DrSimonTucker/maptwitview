package uk.ac.shef.dcs.oak.twitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Proxy for accessing twitter
 * 
 * @author sat
 * 
 */
public final class TwitterProxy
{

   /** The Authentication handler */
   private static OAuthHandler handler = new OAuthHandler();

   /** Flag to indicate we have opted in to twitter */
   private static boolean optIn = true;

   private static final int NUMBER_OF_POSTS = 1000;

   public static SocialPost[] getFeedList(String provider, String list)
   {
      SocialPost[] tweetArr = new SocialPost[NUMBER_OF_POSTS];
      try
      {
         String xmlString = handler.getList(provider, list, optIn, 10);
         parse(xmlString, new Tweets(tweetArr));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      for (SocialPost post : tweetArr)
         if (post != null)
            post.setSource(list);

      return tweetArr;
   }

   public static SocialPost[] getFeedList(String provider, String list, int numberOfPosts)
   {
      SocialPost[] tweetArr = new SocialPost[numberOfPosts];
      try
      {
         int page = 1;
         while (tweetArr[tweetArr.length - 1] == null)
         {
            // System.out.println("Processing page " + page);
            String xmlString = handler.getList(provider, list, optIn, page++);
            // System.out.println(xmlString);
            parse(xmlString, new Tweets(tweetArr));
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      for (SocialPost post : tweetArr)
         if (post != null)
            post.setSource(list);

      return tweetArr;
   }

   public static TreeSet<String> getFeedUsers(String owner, String list)
   {
      TreeSet<String> users = new TreeSet<String>();
      try
      {
         String xmlString = handler.getSubscribers(owner, list);
         parse(xmlString, users);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return users;
   }

   /**
    * Get the Tweets from your friends
    * 
    * @param n
    *           The number of tweets to collect
    * @return An array of the available tweets
    */
   public static SocialPost[] getFriendsTweets(final int n)
   {
      SocialPost[] tweetArr = new SocialPost[n];
      int read = 0;
      int page = 1;
      try
      {
         while (read < n)
         {
            String xmlString = handler.getFriends(optIn, page++);
            int readNumber = parse(xmlString, new Tweets(tweetArr));
            if (readNumber == 0)
               break;
            else
               read += readNumber;
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return tweetArr;
   }

   /**
    * Get the latest public tweets
    * 
    * @param n
    *           The number of tweets to collect
    * @return An array of {@link SocialPost}
    */
   public static SocialPost[] getLatestPublicTweets(final int n)
   {
      SocialPost[] tweetArr = new SocialPost[n];

      try
      {
         String xmlString = handler.getAll(optIn);
         parse(xmlString, new Tweets(tweetArr));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return tweetArr;
   }

   public static void main(String[] args)
   {
      SocialPost[] posts = TwitterProxy.getFriendsTweets(100);
      System.out.println(posts.length);
      int geoCount = 0;
      for (SocialPost post : posts)
         if (post != null)
            if (post.getLat() > 0)
               geoCount++;
      System.out.println(geoCount);
   }

   /**
    * Get your latest Tweets
    * 
    * @param n
    *           The number of tweets to collect
    * @return An array of {@link SocialPost}
    */
   public static SocialPost[] getLatestTweets(final int n)
   {
      SocialPost[] tweetArr = new SocialPost[n];

      try
      {
         String xmlString = handler.getHome(optIn);
         parse(xmlString, new Tweets(tweetArr));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      List<SocialPost> posts = new LinkedList<SocialPost>();
      for (int i = 0; i < tweetArr.length; i++)
         if (tweetArr[i] != null)
            posts.add(tweetArr[i]);
      Collections.shuffle(posts);

      return posts.toArray(new SocialPost[0]);
   }

   public static SocialPost[] getPosts(String user, int numberOfPosts)
   {
      SocialPost[] tweetArr = new SocialPost[numberOfPosts];
      try
      {
         int page = 1;
         while (tweetArr[tweetArr.length - 1] == null && page < 10)
         {
            // System.out.println("Processing page " + page);
            String xmlString = handler.getPosts(user, page++);
            // System.out.println(xmlString);
            parse(xmlString, new Tweets(tweetArr));
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return tweetArr;
   }

   /**
    * Opts you out of using twitter
    */
   public static void optOut()
   {
      optIn = false;
   }

   private static Set<String> parse(final String str, TreeSet<String> users) throws IOException
   {
      Users handlerIn = new Users(users);
      try
      {
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
         parser.parse(new InputSource(new StringReader(str)), handlerIn);
      }
      catch (SAXException e)
      {
         throw new IOException(e);
      }
      catch (ParserConfigurationException e)
      {
         throw new IOException(e);
      }
      return handlerIn.getData();
   }

   /**
    * Helper method for parsing the XML stream
    * 
    * @param str
    *           The String to parse
    * @param handlerIn
    *           The handler to use
    * @return THe handler, having processed the stream
    * @throws IOException
    *            If something goes wrong with the network
    */
   private static int parse(final String str, final Tweets handlerIn) throws IOException
   {
      // Check we haven't opted out
      if (!optIn)
         handlerIn.fill();
      else
         try
         {
            handlerIn.reset();
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new InputSource(new StringReader(str)), handlerIn);
         }
         catch (SAXException e)
         {
            throw new IOException(e);
         }
         catch (ParserConfigurationException e)
         {
            throw new IOException(e);
         }
      return handlerIn.getLastRead();
   }

   /**
    * Blocking constructor
    */
   private TwitterProxy()
   {

   }
}
