package uk.ac.shef.dcs.oak.twitter.model;

import uk.ac.shef.dcs.oak.twitter.SocialPost;

public class KeywordFilter implements Filter
{
   @Override
   public boolean filter(SocialPost post)
   {
      return post.getText().contains(keyword);
   }

   @Override
   public double getFuzzy()
   {
      return 1.0;
   }

   String keyword;

   public KeywordFilter(String word)
   {
      keyword = word;
   }

}
