package uk.ac.shef.dcs.oak.twitter.model;

import java.util.LinkedList;
import java.util.List;

import uk.ac.shef.dcs.oak.twitter.SocialPost;

public class TwitMapModel
{
   List<SocialPost> posts = new LinkedList<SocialPost>();

   public TwitMapModel(SocialPost[] allPosts)
   {
      for (SocialPost post : allPosts)
         if (post != null)
            posts.add(post);
      System.out.println(posts.size() + " read");
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
