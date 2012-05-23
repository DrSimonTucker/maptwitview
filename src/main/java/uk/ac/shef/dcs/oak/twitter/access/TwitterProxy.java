package uk.ac.shef.dcs.oak.twitter.access;

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

import uk.ac.shef.dcs.oak.twitter.SocialPost;

/**
 * Proxy for accessing twitter
 * 
 * @author sat
 * 
 */
public final class TwitterProxy
{
   private static OAuthHandler handler = new OAuthHandler();
   /** Flag to indicate we have opted in to twitter */
   private static boolean optIn = true;

   private static final int NUMBER_OF_POSTS = 1000;

   /**
    * Get the Tweets from your friends
    * 
    * @param n
    *           The number of tweets to collect
    * @return An array of the available tweets
    */
   public static SocialPost[] getSolrData(final int n, boolean geo)
   {
      SocialPost[] tweetArr = new SocialPost[n];
      int read = 0;
      int page = 1000;
      try
      {
         while (read < n)
         {
            System.out.println("Reading " + page + " rows");
            String xmlString = handler.getSolrData(page);
            int readNumber = parse(xmlString, new SPSolrHandler(tweetArr, geo));
            read = readNumber;
            page *= 2;
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
      SocialPost[] data = TwitterProxy.getSolrData(100, true);
      System.out.println(data.length);
      System.out.println(data[0]);
      System.out.println(data[0].getText());
      System.out.println(data[0].getLat());
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
   private static int parse(final String str, final SPSolrHandler handlerIn) throws IOException
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
