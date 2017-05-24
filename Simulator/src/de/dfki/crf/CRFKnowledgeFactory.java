package de.dfki.crf;

import allow.simulator.knowledge.Experience;
import allow.simulator.knowledge.IExchangeStrategy;

public final class CRFKnowledgeFactory {

  private final DBConnector dbConnector;
  private final String tablePrefix;
  
  private CRFKnowledgeFactory(DBConnector dbConnector, String tablePrefix) {
    this.dbConnector = dbConnector;
    this.tablePrefix = tablePrefix;
  }
   
  public IKnowledgeModel<Experience> createLocalCRFModel(String instanceId) {
    return new CRFLocalKnowledge(tablePrefix + instanceId, dbConnector);
  }
  
  public IKnowledgeModel<Experience> createGlobalCRFModel(String instanceId) {
    return new CRFGlobalKnowledge(tablePrefix + instanceId, dbConnector);
  }
  
  public IKnowledgeModel<Experience> createRegionalCRFModel(String instanceId) {
    return new CRFRegionalKnowledge(tablePrefix + instanceId, dbConnector);
  }
  
  public IKnowledgeModel<Experience> createIdentityCRFModel() {
    return CRFNoKnowledge.getInstance();
  }
  
  public IExchangeStrategy<IKnowledgeModel<Experience>> createLocalCRFStrategy() {
    return new LocalExchangeStrategy(dbConnector);
  }
  
  public static CRFKnowledgeFactory create(String modelPath, String modelName, 
      String userName, String password, String tablePrefix) throws ClassNotFoundException {
    DBConnector dbConnector = new DBConnector(modelPath, modelName, userName, password);
    dbConnector.init(tablePrefix);
    return new CRFKnowledgeFactory(dbConnector, tablePrefix);
  }
}
