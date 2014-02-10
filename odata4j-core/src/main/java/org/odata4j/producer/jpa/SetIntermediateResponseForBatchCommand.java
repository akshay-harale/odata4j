package org.odata4j.producer.jpa;

import org.odata4j.producer.jpa.JPAContext.EntityAccessor;

/**
 * The Class DummyResponseForBatchCommand.
 * 
 * This class is used to set the Entity which will be send as intermediate response for batch with create operation. We
 * have separate command chain for create within batch; we build the changeset response after changeset is committed. So
 * that the system-generated keys will be part of the batch response. <br />
 * So in order to build a partial response representing an created entity, we just set the JPA entity as a result which
 * will parsed in SetResponseCommand. Later once changeset is committed, this response is overwritten to include
 * system-genrated values.
 * 
 * @author <a href="mailto:rajni.kumari@synerzip.com">rajni.kumari</a>
 */
public class SetIntermediateResponseForBatchCommand implements Command {
  protected EntityAccessor accessor;

  /**
   * Instantiates a new sets the generated value abstract command.
   */
  public SetIntermediateResponseForBatchCommand() {
    this(EntityAccessor.ENTITY);
  }

  /**
   * Instantiates a new sets the generated value abstract command.
   * 
   * @param accessor
   *            the accessor
   */
  public SetIntermediateResponseForBatchCommand(EntityAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public boolean execute(JPAContext context) {
    context.setResult(JPAResults.entity(accessor.getEntity(context).getJpaEntity()));
    return false;
  }
}
