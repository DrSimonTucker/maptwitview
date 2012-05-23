package uk.ac.shef.dcs.oak.twitter.model;

/**
 * Interface for listening to updates to the model
 * 
 * @author sat
 * 
 */
public interface ModelListener
{
   /**
    * Method indicating that the model has been updated
    * 
    * @param newModel
    *           The new model details
    */
   void modelUpdated(TwitMapModel newModel);
}
