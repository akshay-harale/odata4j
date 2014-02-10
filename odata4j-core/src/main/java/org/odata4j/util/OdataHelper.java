package org.odata4j.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type.PersistenceType;

import org.core4j.CoreUtils;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.jpa.JPAMember;
import org.odata4j.producer.jpa.JPAProducer;

/**
 * The Class OdataHelper.
 *
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public final class OdataHelper {

  public static void applyOPropertiesExcludingPrimaryKeys(ManagedType<?> jpaManagedType,
      Collection<OProperty<?>> properties, Object jpaEntity, EdmDataServices metadata) {

    EdmEntitySet edmEntitySet = metadata.getEdmEntitySet(((EntityType<?>) jpaManagedType).getName());

    for (OProperty<?> prop : properties) {
      // check if property is not of type media and then add it to JPA Entity
      if (!prop.getType().equals(EdmSimpleType.STREAM)) {
        boolean found = false;

        if (jpaManagedType instanceof EntityType) {
          EntityType<?> jpaEntityType = (EntityType<?>) jpaManagedType;
          if (jpaEntityType.getIdType().getPersistenceType() == PersistenceType.EMBEDDABLE) {
            EmbeddableType<?> et = (EmbeddableType<?>) jpaEntityType.getIdType();

            for (Attribute<?, ?> idAtt : et.getAttributes()) {

              if (idAtt.getName().equals(prop.getName())) {
                Object idValue = JPAMember.create(
                    jpaEntityType.getId(et.getJavaType()),
                    jpaEntity).get();

                JPAProducer.setAttribute(idAtt, prop, idValue);
                found = true;
                break;
              }
            }
          }
        }
        if (found) {
          continue;
        }

        // DSDS Start : Handle Complex data types
        //SingularAttributeImpl<?, ?> att = (SingularAttributeImpl<?, ?>) jpaManagedType.getAttribute(prop
        //.getName());
        Attribute<?, ?> att = jpaManagedType.getAttribute(prop.getName());
        DatabaseMapping mapping = CoreUtils.getFieldValue(att, "mapping", DatabaseMapping.class);

        if (!mapping.isPrimaryKeyMapping()) {
          //Attribute<?, ?> att = jpaManagedType.getAttribute(prop.getName());
          JPAProducer.setAttribute(att, prop, jpaEntity);
        }
      }
    }
  }

  /**
   * This API adda media type of properties to JPA entity. API is used to add
   * stream for the BLOB type of columns to the JPA Entity while creation of
   * an entity, which is received as an InputStream.
   * 
   * @param em
   *            the EntityManager
   * @param jpaManagedType
   *            the JPAManagedType
   * @param oEntity
   *            the OEntity
   * @param jpaEntity
   *            the JPAEntity
   * @param metadata
   *            the Metadata
   */
  public static void applyMediaProperties(EntityManager em,
      ManagedType<?> jpaManagedType, OEntity oEntity, Object jpaEntity,
      EdmDataServices metadata) {

    EdmEntitySet edmEntitySet = metadata
        .getEdmEntitySet(((EntityType<?>) jpaManagedType).getName());

    if (edmEntitySet.getType().getHasStream() != null
        && edmEntitySet.getType().getHasStream()) {
      for (OProperty<?> prop : oEntity.getProperties()) {
        Attribute<?, ?> att = jpaManagedType.getAttribute(prop
            .getName());
        if (prop.getType().equals(EdmSimpleType.STREAM) && oEntity.getMediaLinkStream() != null) {
          JPAMember attMember = JPAMember.create(att, jpaEntity);
          ByteArrayOutputStream outputStream = null;
          try {
            //blob = em.unwrap(Connection.class).createBlob();
            outputStream = new ByteArrayOutputStream();//blob.setBinaryStream(0);
            final InputStream inputStream = oEntity
                .getMediaLinkStream();
            byte[] buffer = new byte[ODataConstants.COPY_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, bytesRead);
            }
          } catch (Exception e) {

          }
          // set the blob value
          attMember.set(outputStream.toByteArray());
        }
      }
    }
  }

  /**
   * Will provide the content-type from extended property of column.
   * 
   * @param mediaPropertyName
   *            the Media Property Name
   * @param jpaEntityType
   *            the jpa entity type
   * @return String - MediaContentType
   */
  public static String getMediaContentType(String mediaPropertyName, EntityType<?> jpaEntityType) {
    String contentType = ODataConstants.APPLICATION_OCTET_STREAM;
    // Hardcoded for now, as we do not get any contentType attribute from JPA mapping.
    /*Attribute<?, ?> sa = jpaEntityType.getAttribute(mediaPropertyName);
    DatabaseMapping mapping = CoreUtils.getFieldValue(sa, JPAConstants.ECLIPSELINK_TYPE_FIELD_MAPPING,
        DatabaseMapping.class);
    if (mapping.getProperties().containsKey(JPAConstants.ENTITY_ATTRIBUTE_CUSTOM_PROP_MEDIA_CONTENT_TYPE)) {
      contentType = mapping.getProperty(JPAConstants.ENTITY_ATTRIBUTE_CUSTOM_PROP_MEDIA_CONTENT_TYPE)
          .toString();
    } */
    return contentType;
  }

  /**
   * API to get the length of InputStream and copy it to an OutputStream.
   * 
   * @param inputStream
   *            the InputStream
   * @param outStream
   *            the OutputStream
   * @return long Length of stream
   * @throws SQLException
   *             the sQL exception
   */
  public static long getStreamLength(InputStream inputStream,
      OutputStream outStream) throws SQLException {
    byte[] buf = new byte[ODataConstants.COPY_BUFFER_SIZE];
    try {
      long length = 0;
      int n;
      while ((n = inputStream.read(buf)) != -1) {
        length += n;
        outStream.write(buf, 0, n);
      }
      return length;
    } catch (IOException e) {
      throw new SQLException(e);
    } finally {
      try {
        outStream.flush();
        inputStream.close();
      } catch (IOException e) {}
    }
  }
}
