package org.odata4j.producer.resources;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

/**
 * The Class ODataBatchUnit.
 *
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 */
public abstract class ODataBatchUnit {

  /**
   * Execution for batch unit.
   *
   * @param httpHeaders the http headers
   * @param providers the providers
   * @param baseUrI the base ur i
   * @return the response
   * @throws Exception the exception
   */
  public abstract Response execute(HttpHeaders httpHeaders, Providers providers, URI baseUrI) throws Exception;

  /**
   * Gets the batch unit content type.
   *
   * @return the batch unit content type
   */
  public abstract String getBatchUnitContentType();
}
