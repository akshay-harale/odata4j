package org.odata4j.producer.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * The Class ODataBatchDelUnit handles DELETE operation from batch request.
 *
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 */
public class ODataBatchDelUnit extends ODataBatchSingleUnit {

  protected ODataBatchDelUnit(HttpHeaders hHeaders, UriInfo uriInfo, String uri, String contents, MultivaluedMap<String, String> headers) throws URISyntaxException {
    super(uriInfo, uri, contents, headers);
  }

  @Override
  protected Response delegate(HttpHeaders httpHeaders, URI baseUri, Providers providers) throws Exception {
    Response response = new EntityRequestResource().deleteEntity(httpHeaders,
        getUriInfo(), providers,
        null, getQueryStringsMap().getFirst("$format"),
        getQueryStringsMap().getFirst("$callback"),
        getEnitySetName(),
        getEntityKey());

    return response;
  }

}
