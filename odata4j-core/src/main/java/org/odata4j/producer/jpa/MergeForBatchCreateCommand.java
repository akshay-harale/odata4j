package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;


/**
 * This command is used to make detached object managed.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">rajni.kumari</a>
 * 
 */
public class MergeForBatchCreateCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManager em = context.getEntityManager();
    // Merged JpaEntity is not getting refreshed in the context since for Java everything is pass by value.
    // so we get the merged JpaEnity and set it to the context.
    Object mergedEntity = em.merge(context.getEntity().getJpaEntity());
    context.getEntity().setJpaEntity(mergedEntity);
    return false;
  }

}
