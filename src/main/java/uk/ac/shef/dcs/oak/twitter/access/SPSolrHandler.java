package uk.ac.shef.dcs.oak.twitter.access;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.shef.dcs.oak.twitter.SocialPost;

/**
 * XML Handler for tweet streams
 * 
 * @author sat
 * 
 */
public class SPSolrHandler extends DefaultHandler
{
   /** The text of the tweet */
   private String tweetText;

   /** The user creating the tweet */
   private String user;

   /** The URL of the tweet */
   private String imageURL;

   /** The time the tweet was created */
   private Long time;

   /** Date format for parsing date information from the stream */
   private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

   /** Flag to say we're in the user block */
   private final boolean inUser = false;

   /** Pointer into the tweet array */
   private int pointer = 0;

   /** Currently read text from the XML stream */
   private String text = "";

   /** Array of tweets to be recorded */
   private final SocialPost[] tweetArr;

   /** The multiplier to convert random numbers into time */
   private static final long RANDOM_MULT = 100000L;

   /** Location details */
   private double lat = 0, lon = 0;

   /** THe number we last read */
   private int lastRead;

   public void reset()
   {
      lastRead = 0;
   }

   public int getLastRead()
   {
      return lastRead;
   }

   /**
    * Constructor
    * 
    * @param toFill
    *           The array of tweets we wish to fill
    */
   public SPSolrHandler(final SocialPost[] toFill)
   {
      tweetArr = toFill;

      // Adjust the pointer to the first non-null entry
      while (toFill[pointer] != null)
         pointer++;
   }

   @Override
   public final void characters(final char[] ch, final int start, final int length)
         throws SAXException
   {
      text += new String(ch, start, length);
   }

   @Override
   public final void endElement(final String uri, final String localName, final String qName)
         throws SAXException
   {
      if (named.equals("localisation_ll") && text.trim().length() > 0)
      {
         String[] elems = text.trim().split(",");
         lat = Double.parseDouble(elems[0]);
         lon = Double.parseDouble(elems[1]);
      }
      if ((localName + qName).equals("doc"))
      {
         if (pointer < tweetArr.length)
            tweetArr[pointer++] = new SocialPost(tweetText, user, time, imageURL, lat, lon);
         lat = 0;
         lon = 0;
         lastRead += 1;
      }
      else if (named.equals("text_t"))
         tweetText = text;
      else if (named.equals("username_s"))
         user = text;
      else if (named.equals("date_creation_tdt"))
         try
         {
            time = (df.parse(text).getTime());
         }
         catch (ParseException e)
         {
            throw new SAXException(e);
         }

   }

   /**
    * External method for filling the tweet array
    */
   public final void fill()
   {
      // Fill our array with random tweets
      for (int i = 0; i < tweetArr.length; i++)
         if (i % 3 == 0)
            tweetArr[i] = new SocialPost("Hello", "SimonTucker",
                  (long) (Math.random() * RANDOM_MULT), "", -1, -1);
         else if (i % 3 == 1)
            tweetArr[i] = new SocialPost("RT Retweet example", "SimonTucker",
                  (long) (Math.random() * RANDOM_MULT), "", 0, 0);
         else
            tweetArr[i] = new SocialPost("@SomeoneElse Hello someone else", "SimonTucker",
                  (long) (Math.random() * RANDOM_MULT), "", 0, 0);
   }

   String named = "";

   @Override
   public final void startElement(final String uri, final String localName, final String qName,
         final Attributes attributes) throws SAXException
   {
      text = "";
      if (attributes.getValue("name") != null)
         named = attributes.getValue("name");
   }
}
