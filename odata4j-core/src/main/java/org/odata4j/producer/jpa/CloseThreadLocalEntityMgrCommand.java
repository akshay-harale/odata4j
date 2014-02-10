package org.odata4j.producer.jpa;


/**
 * The Class CloseEntityManagerCommand is used to close the thread local Entity Manager.
 * 
 * @author <a href="mailto:amit.jahagirdar@synerzip.com">amit.jahagirdar</a>
 */
public class CloseThreadLocalEntityMgrCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManagerThreadLocal.closeEntityManager();
    return false;
  }

}
