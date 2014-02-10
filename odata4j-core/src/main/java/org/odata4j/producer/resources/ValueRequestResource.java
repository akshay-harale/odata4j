package org.odata4j.producer.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataContextImpl;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;
import org.odata4j.producer.OMediaLinkExtensions;

public class ValueRequestResource extends BaseResource {

  @GET
  public Response get(
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context Providers providers,
      @Context SecurityContext securityContext,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @QueryParam("$expand") String expand,
      @QueryParam("$select") String select) {
    ODataProducer producer = getODataProducer(providers);
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);

    if (entitySet != null && entitySet.getType().getHasStream()) {
      ODataContext odataContext = ODataContextImpl.builder()
          .aspect(httpHeaders)
          .aspect(securityContext)
          .aspect(producer)
          .aspect(entitySet)
          .aspect(uriInfo)
          .build();

      return getStreamResponse(httpHeaders, uriInfo, producer, entitySet, id, new EntityQueryInfo(
          null,
          OptionsQueryParser.parseCustomOptions(uriInfo),
          OptionsQueryParser.parseExpand(expand),
          OptionsQueryParser.parseSelect(select)),
          securityContext,
          odataContext);
    }
    throw new NotFoundException();
  }

  protected Response getStreamResponse(HttpHeaders httpHeaders, UriInfo uriInfo, ODataProducer producer, EdmEntitySet entitySet, String entityId, EntityQueryInfo queryInfo,
      SecurityContext securityContext, ODataContext odataContext) {

    // this is from new odata4j 0.8, 
     OMediaLinkExtension mediaLinkExtension = this.getMediaLinkExtension(httpHeaders, uriInfo, entitySet, producer, odataContext);
//    OMediaLinkExtension mediaLinkExtension = producer.findExtension(OMediaLinkExtension.class);
    if (mediaLinkExtension != null) {

    EntityResponse entityResponse = producer.getEntity(odataContext,
        entitySet.getName(), OEntityKey.parse(entityId), queryInfo);
    InputStream entityStream = mediaLinkExtension.getInputStreamForMediaLinkEntry(odataContext, entityResponse.getEntity(), null, queryInfo);
    StreamingOutput outputStream = getOutputStreamFromInputStream(entityStream);
    String contentType = mediaLinkExtension.getMediaLinkContentType(odataContext, entityResponse.getEntity());
    String contentDisposition = mediaLinkExtension.getMediaLinkContentDisposition(odataContext, entityResponse.getEntity());

    // this is from latest odata4j code, why we choose outputStream?
    //return Response.ok(entityStream, contentType).header("Content-Disposition", contentDisposition).build();

    return Response.ok(outputStream, contentType).header("Content-Disposition", contentDisposition).build();
    }  else {
      throw new NotImplementedException();
    }
  }

  /**
   * Gets the output stream from input stream which will only be called when the client starts reading the stream.
   *
   * @param inputStream the input stream
   * @return the output stream from input stream
   */
  private StreamingOutput getOutputStreamFromInputStream(
      final InputStream inputStream) {
    final StreamingOutput outputStream = new StreamingOutput() {
      public void write(OutputStream out) throws IOException,
          WebApplicationException {
        try {
          InternalUtil.copyInputToOutput(inputStream, out);
        } catch (IOException e) {
          // do nothing
        } finally {
          // close output stream which was flushed earlier
          out.close();
          // close the input stream for media column
          inputStream.close();
        }
      }
    };
    return outputStream;
  }

}