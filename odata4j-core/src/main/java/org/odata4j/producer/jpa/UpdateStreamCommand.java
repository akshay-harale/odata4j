package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.odata4j.core.OEntity;
import org.odata4j.util.OdataHelper;

/**
 * The Class UpdateStreamCommand is a command, which provides updating media stream for an entity.
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public class UpdateStreamCommand implements Command {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lgc.dsl.producer.jpa.commands.Command#execute(com.lgc.dsl.producer.jpa.base.DsdsJPAContext)
	 */
	@Override
	public boolean execute(JPAContext context) {
		EntityManager entityManager = context.getEntityManager();
		EntityType<?> jpaEntityType = context.getEntity().getJPAEntityType();
		OEntity entity = context.getEntity().getOEntity();
		Object jpaEntity = context.getEntity().getJpaEntity();
		OdataHelper.applyMediaProperties(entityManager, jpaEntityType, entity, jpaEntity, context.getMetadata());
		entityManager.merge(jpaEntity);
		return false;
	}

}
