package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.odata4j.core.OEntity;
import org.odata4j.util.OdataHelper;

/**
 * The Class ApplyMediaPropertiesCommand is used to add stream for the BLOB type of columns to the JPA Entity while
 * creation of an entity.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">shantanu</a>
 */
public class ApplyMediaPropertiesCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManager entityManager = context.getEntityManager();
    EntityType<?> jpaManagedType = context.getEntity().getJPAEntityType();
    OEntity entity = context.getEntity().getOEntity();
    Object jpaEntity = context.getEntity().getJpaEntity();
    OdataHelper
        .applyMediaProperties(entityManager, jpaManagedType, entity, jpaEntity, context.getMetadata());
    return false;
  }
}
