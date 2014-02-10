package org.odata4j.producer.jpa;

/**
 * The Class JPAConstants holds the constants for JPA.
 */
public final class JPAConstants {

  public static final String SELECT_BLOB_SQL_PLACEHOLDER_CLAUSE = "SELECT <Blob column Name> FROM <table Name> WHERE <where clause>";
  public static final String SELECT_BLOB_COLUMN_NAME_PLACEHOLDER = "<Blob column Name>";
  public static final String SELECT_BLOB_TABLE_NAME_PLACEHOLDER = "<table Name>";
  public static final String SELECT_BLOB_WHERE_CLAUSE_PLACEHOLDER = "<where clause>";
  public static final String ECLIPSELINK_TYPE_FIELD_MAPPING = "mapping";
  public static final String ENTITY_ATTRIBUTE_CUSTOM_PROP_IS_MEDIA_TYPE = "isMedia";
  public static final String ENTITY_ATTRIBUTE_CUSTOM_PROP_MEDIA_CONTENT_TYPE = "mediaContentType";
  public static final String CUD_OPERATION_COLUMN_EQUAL_SIGN = " = ";
  public static final String PATH_SEPARATOR_FORWARD_SLASH = "/";
  public static final String URL_PATTERN_FOR_MEDIA_LINK = "$value";

}
