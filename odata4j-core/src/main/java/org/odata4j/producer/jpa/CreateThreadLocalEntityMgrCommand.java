package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;


/**
 * Command to create the Entity manager and add it to the Entity manager thread local.
 * 
 * @author <a href="mailto:amit.jahagirdar@synerzip.com">amit.jahagirdar</a>
 * 
 */
public class CreateThreadLocalEntityMgrCommand implements Command {

  EntityManagerFactory factory = null;

  /**
   * Instantiates a new creates the thread local entity mgr command.
   * 
   * @param emf
   *            the emf
   */
  public CreateThreadLocalEntityMgrCommand(EntityManagerFactory emf) {
    this.factory = emf;
  }

  @Override
  public boolean execute(JPAContext context) {
    EntityManager em = this.factory.createEntityManager();
    em.setFlushMode(FlushModeType.COMMIT);
    EntityManagerThreadLocal.setEntityManager(em);
    context.setEntityManager(em);
    return false;
  }
}
