package uk.ac.shef.dcs.oak.twitter.view;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import uk.ac.shef.dcs.oak.twitter.model.ModelListener;
import uk.ac.shef.dcs.oak.twitter.model.TwitMapModel;

public class WordCloud extends JPanel implements ModelListener
{
   @Override
   public void modelUpdated(TwitMapModel newModel)
   {
      // Get the top
      List<String> topWords = newModel.getTopKeyWords(numberOfWords);
      System.out.println("Got words = " + topWords.size());
      for (int i = 0; i < Math.min(buttons.length,topWords.size()); i++)
         buttons[i].setText(topWords.get(i));
   }

   JButton[] buttons;

   int numberOfWords = 10;

   public WordCloud()
   {
      buttons = new JButton[10];
      initGUI();
   }
   
   private void initGUI()
   {
	   for(int i = 0 ; i < buttons.length ; i++)
	   {
		   buttons[i] = new JButton();
		   this.add(buttons[i]);
	   }
   }

}
