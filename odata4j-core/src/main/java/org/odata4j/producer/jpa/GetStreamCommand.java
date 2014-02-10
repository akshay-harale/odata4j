package org.odata4j.producer.jpa;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.sql.rowset.serial.SerialBlob;

import org.core4j.Enumerable;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.jpa.JPAContext.EntityAccessor;
import org.odata4j.util.OdataHelper;

/**
 * The Class GetStreamCommand is used to get the input stream for the BLOB type
 * of columns.
 * 
 * @author <a href="mailto:shantanu@synerzip.com">shantanu</a>
 */
public class GetStreamCommand implements Command {

  private EntityAccessor accessor;

  /**
   * Instantiates a GetStreamCommand.
   */
  public GetStreamCommand() {
    this(EntityAccessor.ENTITY);
  }

  /**
   * Instantiates a GetStreamCommand.
   * 
   * @param accessor
   *            - the accessor
   */
  public GetStreamCommand(EntityAccessor accessor) {
    this.accessor = accessor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.lgc.dsl.producer.jpa.commands.Command#execute(com.lgc.dsl.producer
   * .jpa.base.DsdsJPAContext)
   */
  @Override
  public boolean execute(JPAContext context) {
    EdmEntitySet edmEntitySet = context.getMetadata().getEdmEntitySet(
        context.getEntity().getEntitySetName());
    EntityType<?> jpaEntityType = accessor.getEntity(context)
        .getJPAEntityType();
    String selectSql = JPAConstants.SELECT_BLOB_SQL_PLACEHOLDER_CLAUSE;
    Object typeSafeEntityKey = accessor.getEntity(context)
        .getTypeSafeEntityKey();
    String keyAttributeName = context.getEntity().getKeyAttributeName();

    // replace BLOB column name in select sql
    String mediaPropertyName = getMediaPropertyName(edmEntitySet);
    selectSql = selectSql.replace(
        JPAConstants.SELECT_BLOB_COLUMN_NAME_PLACEHOLDER,
        mediaPropertyName);
    // replace table name in select sql
    selectSql = selectSql.replace(
        JPAConstants.SELECT_BLOB_TABLE_NAME_PLACEHOLDER,
        jpaEntityType.getName());
    // generate where clause
    StringBuilder whereClauseBuilder = getWhereClauseBuilder(
        typeSafeEntityKey, keyAttributeName);

    // add where clause in select sql
    selectSql = selectSql.replace(
        JPAConstants.SELECT_BLOB_WHERE_CLAUSE_PLACEHOLDER,
        whereClauseBuilder.toString());

    // get the unwrapped connection from the entity manager
    Query query = context.getEntityManager().createNativeQuery(selectSql);
    InputStream binaryStream = null;
    try {
      Object object = query.getSingleResult();
      // check if object is byte[], then convert it to blob
      if (object instanceof byte[]) {
        java.sql.Blob blob = new SerialBlob((byte[]) object);
        object = blob;
      }
      if (object != null && object instanceof Blob) {
        binaryStream = ((Blob) object).getBinaryStream();
      }
    } catch (SQLException e) {
      // handle exception
    }

    context.getContextStream().setInputStream(binaryStream);
    context.getContextStream().setContentType(
        OdataHelper.getMediaContentType(mediaPropertyName, jpaEntityType));
    return false;
  }

  /**
   * API will return the Where Clause Builder.
   * 
   * @param typeSafeEntityKey
   *            the type safe entity key
   * @param keyAttributeName
   *            primary key attribute name
   * 
   * @return StringBuilder
   */
  private StringBuilder getWhereClauseBuilder(Object typeSafeEntityKey,
      String keyAttributeName) {
    StringBuilder columnBuilder = new StringBuilder();
    //TODO: Handle Embeddable keys later
    columnBuilder.append(keyAttributeName);
    columnBuilder
        .append(JPAConstants.CUD_OPERATION_COLUMN_EQUAL_SIGN);
    columnBuilder.append(toJpqlLiteral(typeSafeEntityKey));

    return columnBuilder;
  }

  /**
   * Will provide the BOLB column name depending on the IsMedia extended
   * property.
   * 
   * @param edmEntitySet
   *            the EdmEntitySet
   * 
   * @return String - Media property name
   */
  private String getMediaPropertyName(EdmEntitySet edmEntitySet) {
    String mediaPropertyName = null;
    Enumerable<EdmProperty> properties = edmEntitySet.getType()
        .getProperties();
    for (EdmProperty edmProperty : properties) {
      if (edmProperty.getType().equals(EdmSimpleType.STREAM)) {
        return edmProperty.getName();

      }
    }
    return mediaPropertyName;
  }

  /**
   * To jpql literal.
   * 
   * @param value
   *            the value
   * @return the string
   */
  private String toJpqlLiteral(Object value) {
    if (value instanceof String) {
      return "'" + value + "'";
    }
    if (value instanceof LocalTime) {
      return "'"
          + new java.sql.Time(new LocalDateTime(
              ((LocalTime) value).getMillisOfDay(),
              DateTimeZone.UTC).toDateTime().getMillis())
              .toString() + "'";
    }
    if (value instanceof LocalDateTime) {
      java.sql.Timestamp d = new Timestamp(((LocalDateTime) value)
          .toDateTime().getMillis());
      return "'" + d + "'";
    }
    return value.toString();
  }
}
