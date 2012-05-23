package uk.ac.shef.dcs.oak.twitter.view;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import uk.ac.shef.dcs.oak.twitter.SocialPost;
import uk.ac.shef.dcs.oak.twitter.model.ModelListener;
import uk.ac.shef.dcs.oak.twitter.model.TwitMapModel;

/**
 * Tweet list shows the top five tweets
 * 
 * @author sat
 * 
 */
public class TweetList extends JPanel implements ModelListener
{
   JTextArea[] labels = new JTextArea[5];

   public TweetList()
   {
      initUI();
   }

   @Override
   public void modelUpdated(TwitMapModel newModel)
   {
      List<SocialPost> posts = newModel.getGeoPosts().subList(0,
            Math.min(newModel.getGeoPosts().size(), labels.length));
      for (int i = 0; i < posts.size(); i++)
         labels[i].setText(posts.get(i).getText());
   }

   private void initUI()
   {
      this.setLayout(new GridLayout(5, 1));
      for (int i = 0; i < labels.length; i++)
      {
         labels[i] = new JTextArea();
         labels[i].setLineWrap(true);
         this.add(labels[i], i);
      }
   }
}
