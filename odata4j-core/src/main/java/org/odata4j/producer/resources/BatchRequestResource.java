package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

/**
 * Add request resource to handle batch operation.
 * 
 * @author <a href="mailto:peng.chen@halliburton.com">Kevin Chen</a>
 *
 */
@Path("{batch: \\$}batch")
public class BatchRequestResource extends BaseResource {
  private static Logger logger = Logger.getLogger(BatchRequestResource.class.getName());

  @POST
  @Consumes(ODataBatchProvider.MULTIPART_MIXED)
  @Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8, ODataBatchProvider.MULTIPART_MIXED })
  public Response processBatch(@Context Providers providers,
      @Context HttpHeaders headers, @Context UriInfo uriInfo, @Context Request request,
      @QueryParam("$format") String format,
      @QueryParam("$callback") String callback,
      List<ODataBatchUnit> batchUnits) throws Exception {
    logger.log(Level.INFO, "get a POST request in BatchRequestResource");
    logger.log(Level.INFO, "the number of changesets/GET in the batch request is " + batchUnits.size());

    //Set the ThreadLocal variable to indicate that this is a batch request
    Response res = null;
    try {
      BatchProcessThreadLocal.setBatchProcessFlag(true);

      String batchBoundary = "batchresponse_" + Guid.randomGuid().toString();
      StringBuilder batchResponse = new StringBuilder();

      for (ODataBatchUnit unit : batchUnits) {
        Response response = null;
        try {
          response = unit.execute(headers, providers, uriInfo.getBaseUri());
          batchResponse.append("--").append(batchBoundary).append(createResponseBodyPart(unit, response));
        } catch (Exception e) {
          // if an excpetion occured while executing changeset/query, then just single response should be sent representing the failure.
          logger.log(Level.SEVERE, e.getMessage());
          batchResponse.append("\r\n--").append(batchBoundary).append(createErrorResponseBody(e.getMessage(), Status.INTERNAL_SERVER_ERROR));
        }
      }

      batchResponse.append("--").append(batchBoundary).append("--\r\n");
      String strValue = batchResponse.toString();
      res = Response
          .status(Status.ACCEPTED)
          .type(ODataBatchProvider.MULTIPART_MIXED + ";boundary=" + batchBoundary)
          .header(
              ODataConstants.Headers.DATA_SERVICE_VERSION,
              ODataConstants.DATA_SERVICE_VERSION_HEADER)
          .entity(strValue).build();

    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage());
    } finally {
      // Un-Set the ThreadLocal variable to indicate that batch processing is complete
      BatchProcessThreadLocal.setBatchProcessFlag(null);
    }
    return res;
  }

  /**
   * Get the entity, but the medial type will not be from httpHeaders which is the batch request content-type, instead it will
   * from each batch request header (the content-type for each part in the multi-parts body).
   * @param httpHeaders the top level batch request http headers
   * @param batchRequestHeader each operation's header
   * @param uriInfo the uriInfo of the top batch request
   * @param payload the content of the each operation part
   * @param metadata the meatadata 
   * @param entitySetName the entity set name to be retrieved
   * @param entityKey the entity's key
   * @return the OEntity
   */
  protected OEntity getRequestEntity(HttpHeaders httpHeaders, MultivaluedMap<String, String> batchRequestHeader, UriInfo uriInfo, String payload, EdmDataServices metadata, String entitySetName, OEntityKey entityKey, Boolean isResponse) {
    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
    String contentType = batchRequestHeader.getFirst(ODataConstants.Headers.CONTENT_TYPE);

    MediaType type = getMediaType(contentType);

    return convertFromString(payload, type, version, metadata, entitySetName, entityKey, isResponse);
  }

  // helper function to get string presentation of the response, which will be
  // included in batch response. 

  public static String createResponseBodyPart(ODataBatchUnit batchUnit, Response response) {
    final String CONTENT_ID = "Content-ID";
    StringBuilder body = new StringBuilder();

    body.append(batchUnit.getBatchUnitContentType());

    if (batchUnit instanceof ODataBatchSingleUnit) {
      // set http status for individual operation only, not for changeset
      body.append("\r\n\r\nHTTP/1.1 ");
      Status status = Response.Status.fromStatusCode(response.getStatus());
      body.append(status.getStatusCode());
      body.append(' ');
      body.append(status.getReasonPhrase());
      body.append("\r\n");
    }

    MultivaluedMap<String, String> unitHeader = null;
    if (batchUnit instanceof ODataBatchSingleUnit) {
      unitHeader = ((ODataBatchSingleUnit) batchUnit)
          .getResourceHeaders();
    }
    if (unitHeader != null && unitHeader.containsKey(CONTENT_ID)) {
      body.append(CONTENT_ID);
      body.append(": ");
      body.append(unitHeader.getFirst(CONTENT_ID));
      body.append("\r\n");
    }

    for (String key : response.getMetadata().keySet()) {
      body.append(key).append(": ");
      for (Object value : response.getMetadata().get(key)) {
        body.append(value);
      }
      body.append("\r\n");
    }

    body.append("\r\n");
    if (response.getEntity() != null) {
      body.append(response.getEntity().toString());
    }

    body.append("\r\n");
    return body.toString();
  }

  /**
   * Creates an error reponse body as per odata specs for both changeset failure and query failure<br>
   * Reference : <a href="http://msdn.microsoft.com/en-us/library/dd541456.aspx">MSDN</a>&nbsp;&nbsp;
   * <a href="http://www.odata.org/documentation/odata-v2-documentation/batch-processing/#4_Format_of_a_Batch_Response">Odata</a>
   *
   * @param errorMessage the error message
   * @return the string
   */
  public static String createErrorResponseBody(String errorMessage, Status status) {

    StringBuilder body = new StringBuilder();

    // set content-type and content-transfer-encoding headers
    body.append("\r\n");
    body.append(ODataConstants.Headers.CONTENT_TYPE).append(":").append(ODataConstants.APPLICATION_HTTP);
    body.append("\r\n");
    body.append(ODataConstants.Headers.CONTENT_TRANSFER_ENCODING).append(":").append(ODataConstants.BINARY);

    // set http headers and for a single individual response which will also contain the failure message
    body.append("\r\n\r\nHTTP/1.1 ");
    //Status status = Response.Status.fromStatusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    body.append(status.getStatusCode());
    body.append(' ');
    body.append(status.getReasonPhrase());
    body.append("\r\n");
    body.append(ODataConstants.Headers.CONTENT_TYPE).append(":").append(ODataConstants.APPLICATION_XML);

    if (errorMessage != null) {
      body.append("\r\n");
      body.append(ODataConstants.Headers.CONTENT_LENGTH).append(":").append(errorMessage.length());
      body.append("\r\n\r\n");
      body.append(errorMessage);
    }

    body.append("\r\n");
    return body.toString();
  }

  /**
   * create the MediaType instance based on content type
   * @return the media type
   */
  public static MediaType getMediaType(String contentType) {
    return MediaType.valueOf(contentType);
  }

  /**
   * Creates the response for batch.
   *
   * @param httpHeaders the http headers
   * @param uriInfo the uri info
   * @param producer the producer
   * @param entitySetName the entity set name
   * @param entity the entity
   * @return the response
   * @throws Exception the exception
   */
  protected Response createResponseForBatch(
      HttpHeaders httpHeaders,
      UriInfo uriInfo,
      ODataProducer producer,
      String entitySetName,
      OEntity entity) throws Exception {

    EntityResponse response = producer.createResponseForBatchPostOperation(entitySetName, entity);

    FormatWriter<EntityResponse> writer = FormatWriterFactory
        .getFormatWriter(EntityResponse.class, httpHeaders.getAcceptableMediaTypes(), null, null);
    StringWriter sw = new StringWriter();
    writer.write(uriInfo, sw, response);

    String relid = InternalUtil.getEntityRelId(response.getEntity());
    String entryId = uriInfo.getBaseUri().toString() + relid;

    String responseEntity = sw.toString();

    return Response
        .ok(responseEntity, writer.getContentType())
        .status(Status.CREATED)
        .location(URI.create(entryId))
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION,
            ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }
}