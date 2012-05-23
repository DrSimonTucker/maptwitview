package uk.ac.shef.dcs.oak.twitter.model;

import uk.ac.shef.dcs.oak.twitter.SocialPost;

/**
 * Filter is some method of limiting the social posts
 * 
 * @author sat
 * 
 */
public interface Filter
{
   public double filter(SocialPost post);

   public double getFuzzy();
}
