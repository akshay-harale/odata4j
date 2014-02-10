package org.odata4j.producer.jpa;

import javax.persistence.EntityManager;

/**
 * 
 * Thread Local class to maintain entity manager and variable to differentiate between Batch request and normal
 * request(non-batch).
 * 
 * @author <a href="mailto:amit.jahagirdar@synerzip.com">amit.jahagirdarr</a>
 * 
 */
public class EntityManagerThreadLocal {

	/** The Constant ENTITY_MANAGER_THREAD_LOCAL. */
	protected static final ThreadLocal<EntityManager> ENTITY_MANAGER_THREAD_LOCAL = new ThreadLocal<EntityManager>();

	/**
	 * Gets the entity manager.
	 * 
	 * @return the entity manager
	 */
	public static EntityManager getEntityManager() {

		return ENTITY_MANAGER_THREAD_LOCAL.get();
	}

	/**
	 * Sets the entity manager.
	 * 
	 * @param em
	 *            the new entity manager
	 */
	public static void setEntityManager(EntityManager em) {

		ENTITY_MANAGER_THREAD_LOCAL.set(em);
	}

	/**
	 * Close entity manager and remove it from the ThreadLocal association.
	 */
	public static void closeEntityManager() {
		if (ENTITY_MANAGER_THREAD_LOCAL.get() != null) {
			EntityManager entityManager = ENTITY_MANAGER_THREAD_LOCAL.get();
			if (entityManager.isOpen()) {
				entityManager.close();
				entityManager = null;
			}
			ENTITY_MANAGER_THREAD_LOCAL.set(null);
		}
	}
}
