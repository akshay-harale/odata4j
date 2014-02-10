package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.odata4j.core.OEntity;
import org.odata4j.util.OdataHelper;

public class MergeEntityCommand implements Command {

  @Override
  public boolean execute(JPAContext context) {
    EntityManager em = context.getEntityManager();
    EntityType<?> jpaEntityType = context.getEntity()
        .getJPAEntityType();
    Object jpaEntity = context.getEntity().getJpaEntity();
    OEntity entity = context.getEntity().getOEntity();
    // add properties other than Stream and Primary Keys 
    OdataHelper.applyOPropertiesExcludingPrimaryKeys(jpaEntityType, entity.getProperties(),
        jpaEntity, context.getMetadata());
    em.merge(jpaEntity);
    JPAProducer.applyOLinks(em, jpaEntityType, entity.getLinks(),
        jpaEntity);

    return false;
  }
}