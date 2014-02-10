package org.odata4j.producer.jpa;

import javax.persistence.EntityTransaction;


/**
 * The Class is used to rollback transactions for transactional requests. It will be used to rollback the changeset if
 * any operation in the changeset failed.
 * 
 * @author <a href="mailto:anil.allewar@synerzip.com">anil.allewar</a>
 */
public class RollbackTransactionCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityTransaction tx = context.getEntityManager().getTransaction();
    if (tx.isActive()) {
      tx.rollback();
    }
    context.setEntityTransaction(null);

    return false;
  }

}
