package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


/**
 * The Class StartBatchTransaction is used to create the batch transaction. We can't use the
 * {@link BeginTransactionCommand} command as it rolls back the transaction on the way back. <br>
 * <br>
 * 
 * We want to have more explicit control over the transaction boundary when we are dealing with changesets.
 * 
 * @author <a href="mailto:amit.jahagirdar@synerzip.com">amit.jahagirdar</a>
 */
public class StartBatchTransactionCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManager em = context.getEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    context.setEntityTransaction(tx);
    return false;
  }
}
