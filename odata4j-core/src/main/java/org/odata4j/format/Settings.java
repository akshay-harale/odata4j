package org.odata4j.format;

import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmType;

public class Settings {

  public final ODataVersion version;
  public final EdmDataServices metadata;
  public final String entitySetName;
  public final OEntityKey entityKey;
  public final boolean isResponse;
  public final EdmType parseType;
  public final EdmFunctionImport parseFunction;

  public Settings(ODataVersion version, EdmDataServices metadata,
      String entitySetName, OEntityKey entityKey) {
    this(version, metadata, entitySetName, entityKey, true, null, null);
  }

  public Settings(ODataVersion version, EdmDataServices metadata,
      String entitySetName, OEntityKey entityKey,
      boolean isResponse) {
    this(version, metadata, entitySetName, entityKey, isResponse, null, null);
  }

  public Settings(ODataVersion version, EdmDataServices metadata,
      String entitySetName, OEntityKey entityKey,
      boolean isResponse, EdmType parseType) {
    this(version, metadata, entitySetName, entityKey, isResponse, parseType, null);
  }

  public Settings(ODataVersion version, EdmDataServices metadata,
      String entitySetName, OEntityKey entityKey, 
      boolean isResponse, EdmType parseType, EdmFunctionImport parseFunction) {
    this.version = version;
    this.metadata = metadata;
    this.entitySetName = entitySetName;
    this.entityKey = entityKey;
    this.isResponse = isResponse;
    this.parseType = parseType;
    this.parseFunction = parseFunction;
  }

}
