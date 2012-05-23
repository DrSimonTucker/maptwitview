package uk.ac.shef.dcs.oak.twitter.model;

import java.util.LinkedList;
import java.util.List;

import uk.ac.shef.dcs.oak.twitter.SocialPost;
import uk.ac.shef.dcs.oak.twitter.access.TwitterProxy;

public class TwitMapModel
{
   List<SocialPost> posts = new LinkedList<SocialPost>();
   List<ModelListener> listeners = new LinkedList<ModelListener>();
   List<Filter> filters = new LinkedList<Filter>();

   public TwitMapModel()
   {
      Thread updateThread = new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            update();
         }
      });
      updateThread.start();
   }

   private void update()
   {
      for (SocialPost post : TwitterProxy.getSolrData(100, true))
         if (post != null && !posts.contains(post))
            posts.add(post);
      System.out.println(posts.size() + " read");

      updateListeners();
   }

   public void addListener(ModelListener listener)
   {
      listeners.add(listener);
      listener.modelUpdated(this);
   }

   private void updateListeners()
   {
      for (ModelListener listener : listeners)
         listener.modelUpdated(this);
   }

   public List<SocialPost> getGeoPosts()
   {
      List<SocialPost> geoPosts = new LinkedList<SocialPost>();
      for (SocialPost post : posts)
         if (post.getLat() > 0)
            geoPosts.add(post);

      System.out.println(geoPosts.size() + " geo");
      return geoPosts;
   }
}
