package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;


/**
 * The Class SetEntityManagerForBatch is used to set the entity manager into context from the EntityManager thread
 * local.
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public class SetEntityManagerForBatch implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManager em = EntityManagerThreadLocal.getEntityManager();
    if (null != em) {
      context.setEntityManager(em);
      context.setEntityTransaction(em.getTransaction());
      if (!context.getEntityTransaction().isActive()) {
        context.getEntityTransaction().begin();
      }

    } else {
      throw new RuntimeException("Entity Manager in the EntityManagerThreadLocal is not set for batch operation.");
    }
    return false;

  }
}
