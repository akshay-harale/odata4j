package org.odata4j.test.integration.producer.custom;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.core4j.Func1;
import org.junit.After;
import org.junit.Ignore;
import org.odata4j.examples.producer.jpa.DatabaseUtils;
import org.odata4j.examples.producer.jpa.JPAProvider;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.jpa.JPAProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;
import org.odata4j.test.integration.AbstractRuntimeTest;
import org.odata4j.test.integration.producer.jpa.northwind.NorthwindTestUtils;

/**
 * The Class StreamingBaseTest used to set up test environment for streaming media resource test case. 
 */
@Ignore
public class StreamingBaseTest extends AbstractRuntimeTest {

  public StreamingBaseTest(RuntimeFacadeType type) {
    super(type);
  }

  protected static final String endpointUri = "http://localhost:8810/mle/Streaming.svc/";

  protected EntityManagerFactory emf;
  protected ODataServer server;

  protected NorthwindTestUtils utils;

  public void setUp(int maxResults) {
    setUp(maxResults, null);
  }

  public void setUp(int maxResults, Func1<ODataProducer, ODataProducer> producerModification) {
    String persistenceUnitName = "MLEService" + JPAProvider.JPA_PROVIDER.caption;
    String namespace = "medialinkentity";

    emf = Persistence.createEntityManagerFactory(persistenceUnitName);

    ODataProducer producer = new JPAProducer(emf, namespace, maxResults); // http://services.odata.org/northwind/Northwind.svc/
    // is
    // using 20 as maxResult in almost any case but not
    // for every

    DatabaseUtils.fillDatabase(namespace.toLowerCase(), "/META-INF/medialinkentity_insert.sql");

    if (producerModification != null)
      producer = producerModification.apply(producer);

    DefaultODataProducerProvider.setInstance(producer);
    server = this.rtFacade.startODataServer(endpointUri);
  }

  @After
  public void tearDown() throws Exception {
    try {

    } finally {
      if (server != null) {
        server.stop();
        server = null;
      }

      if (emf != null) {
        emf.close();
        emf = null;
      }
    }
  }

}
