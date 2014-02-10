package org.odata4j.consumer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.core.ODataClientChangeSetResponse;

/**
 * An implementation of ODataClientChangeSetResponse.
 * 
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
public class ConsumerChangeSetResponseImpl implements ODataClientChangeSetResponse {
  List<ODataClientBatchResponse> results = new ArrayList<ODataClientBatchResponse>();
  int status = 200;

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientBatchResponse#getStatus()
   */
  @Override
  public int getStatus() {
    return status;
  }

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientBatchResponse#getEntity()
   */
  @Override
  public List<ODataClientBatchResponse> getEntity() {
    return results;
  }

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientResponse#getHeaders()
   */
  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientResponse#close()
   */
  @Override
  public void close() {

  }

  /* (non-Javadoc)
   * @see org.odata4j.core.ODataClientChangeSetResponse#add(org.odata4j.consumer.ODataClientBatchResponse)
   */
  @Override
  public void add(ODataClientBatchResponse result) {
    results.add(result);
  }

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientResponse#getEntityInputStream()
   */
  @Override
  public InputStream getEntityInputStream() {
    throw new UnsupportedOperationException("not supported by ConsumerChangeSetResponseImpl");
  }

  /* (non-Javadoc)
   * @see org.odata4j.consumer.ODataClientResponse#getMediaType()
   */
  @Override
  public MediaType getMediaType() {
    throw new UnsupportedOperationException("not supported by ConsumerChangeSetResponseImpl");
  }
}
