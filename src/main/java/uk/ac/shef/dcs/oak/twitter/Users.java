package uk.ac.shef.dcs.oak.twitter;

import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Handler for tweet streams
 * 
 * @author sat
 * 
 */
public class Users extends DefaultHandler
{
   /** List of users */
   private final Set<String> users;

   String text = "";

   /** The multiplier to convert random numbers into time */
   private static final long RANDOM_MULT = 100000L;

   /**
    * Constructor
    * 
    * @param toFill
    *           The array of tweets we wish to fill
    */
   public Users(TreeSet<String> users)
   {
      this.users = users;
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
      if ((localName + qName).equals("screen_name"))
         users.add(text);
   }

   public Set<String> getData()
   {
      return users;
   }

   @Override
   public final void startElement(final String uri, final String localName, final String qName,
         final Attributes attributes) throws SAXException
   {
      text = "";
   }

}
