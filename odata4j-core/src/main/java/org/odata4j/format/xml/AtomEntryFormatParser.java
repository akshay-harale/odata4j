package org.odata4j.format.xml;

import java.io.Reader;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;

/**
 * The Class AtomEntryFormatParser creates the entry point in the feed.
 */
public class AtomEntryFormatParser implements FormatParser<Entry> {

  protected EdmDataServices metadata;
  protected String entitySetName;
  protected OEntityKey entityKey;
  protected EdmFunctionImport functionImport;

  /**
   * Instantiates a new atom entry format parser.
   *
   * @param metadata the metadata
   * @param entitySetName the entity set name
   * @param entityKey the entity key
   * @param functionImport the function import
   */
  public AtomEntryFormatParser(EdmDataServices metadata, String entitySetName, OEntityKey entityKey, EdmFunctionImport functionImport) {
    this.metadata = metadata;
    this.entitySetName = entitySetName;
    this.entityKey = entityKey;
    this.functionImport = functionImport;
  }

  @Override
  public Entry parse(Reader reader) {
    return new AtomFeedFormatParser(metadata, entitySetName, entityKey, functionImport)
        .parse(reader).entries.iterator().next();
  }

}
