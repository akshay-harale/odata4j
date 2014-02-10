package org.odata4j.test.integration.producer.jpa.northwind;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataClientBatchResponse;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.core.OChangeSetRequest;
import org.odata4j.core.OCountRequest;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.ODataClientChangeSetResponse;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityDeleteRequest;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OModifyLinkRequest;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperties;
import org.odata4j.core.OQueryRequest;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.FormatType;

/**
 * This contain test cases for newly added Batch request. The test will test both consumer/server side of
 * implementation. For JPAProducer the batch create operation does not work if you have a table that has system
 * generated primary key and the key is populated after the row is inserted (post-generation strategy)
 * 
 * @author <a href="mailto:Kevin.Chen@halliburton.com">Kevin Chen</a>
 * 
 */
public class BatchRequestTest extends NorthwindJpaProducerTest {
	/**
	 * @param type
	 */
	public BatchRequestTest(RuntimeFacadeType type) {
		super(type);
	}

	private static final Integer ORDER_ID = 10248;
	private static final Integer PRODUCT_ID = 11;
	private final int RESPONSE_COUNT = 6;
	private final int NUMBER_1000 = 1000;
	private final int[] statusCode = { 200, 200, 201, 200, 200, 200, 500, 200 };
	private final int[] numberOfReqInChangeSet = { 3, 1 };

	@Before
	public void setUp() {
		super.setUp(20);
	}

	/**
	 * Test batch request using Atom format.
	 */
	@Test
	public void batchTestInAtom() {
		ODataConsumer.dump.all(true);
		ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, FormatType.ATOM,
				OClientBehaviors.methodTunneling("PUT"));
		batchRequestTest(consumer);

	}

	/**
	 * Test batch rqeuest using JSON format.
	 */
	@Test
	public void batchTestInJson() {
		ODataConsumer consumer = this.rtFacade.createODataConsumer(endpointUri, FormatType.JSON,
				OClientBehaviors.methodTunneling("PUT"));
		batchRequestTest(consumer);

	}

	/**
	 * The real test code.
	 * 
	 * @param consumer
	 *            the consumer
	 */
	private void batchRequestTest(ODataConsumer consumer) {
		final long now = System.currentTimeMillis();

		ODataConsumer.dump.all(true);

		OEntity existingProd = consumer.getEntities("Products").select("ProductID").top(1).execute().first();
		int delProductId = (Integer) existingProd.getProperty("ProductID").getValue();

		// test count request
		OCountRequest countReq = consumer.getEntitiesCount("Customers").top(NUMBER_1000);

		// test query single entity, i.e, with keys
		HashMap<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put("OrderID", ORDER_ID);
		keyMap.put("ProductID", PRODUCT_ID);
		OEntityKey key = OEntityKey.create(keyMap);
		OEntityGetRequest<OEntity> queryReq = consumer.getEntity("Order_Details", key);

		// test query entities, more than one may return
		OQueryRequest<OEntity> queriesReq = consumer.getEntities("Customers").top(RESPONSE_COUNT);

		// create CUD for change set request
		// create request,
		OCreateRequest<OEntity> createRequest1 = consumer.createEntity("Customers")
				.properties(OProperties.string("CustomerID", "ID" + now))
				.properties(OProperties.string("CompanyName", "Company" + now));

		OCreateRequest<OEntity> createRequest2 = consumer.createEntity("Customers")
				.properties(OProperties.string("CustomerID", "ID" + now + "2"))
				.properties(OProperties.string("CompanyName", "Company" + now + "2"));

		// test update
		OEntity customer = consumer.getEntity("Customers", "ALFKI").execute();
		OModifyRequest<OEntity> updateReq = consumer.updateEntity(customer).properties(
				OProperties.string("ContactName", "John Smith"));

		// test delete
		HashMap<String, Object> mapDelete = new HashMap<String, Object>();
		mapDelete.put("ProductID", delProductId);
		OEntityKey keyDelete = OEntityKey.create(mapDelete);
		OEntityDeleteRequest delReq = consumer.deleteEntity("Products", keyDelete);

		OChangeSetRequest changeReq1 = consumer.changeSetRequest().addRequest(createRequest1).addRequest(delReq)
				.addRequest(updateReq);

		// create link, this does not work on server side, will be in its own change set
		OEntity order = consumer.createEntity("Orders").properties(OProperties.string("ShipName", "Landmark"))
				.execute();
		OModifyLinkRequest createLink = consumer.createLink(customer, "Orders", order);
		// Have createRequest2 and createLink requests in same changeset. First request will be processed successfully
		// createLink will give exception, causing entire changeset to rollback.
		OChangeSetRequest changeReq2 = consumer.changeSetRequest().addRequest(createRequest2).addRequest(createLink);

		// get link
		OQueryRequest<OEntityId> getLink = consumer.getLinks(customer, "Orders");

		try {
			// now, adding query/queries/changeReq/countReq/changeReq to the batch operation
			List<ODataClientBatchResponse> results = consumer.batchRequest().addRequest(queryReq)
					.addRequest(queriesReq).addRequest(changeReq1).addRequest(countReq).addRequest(changeReq2)
					.addRequest(getLink).execute();

			Assert.assertEquals("the number of response must match", RESPONSE_COUNT, results.size());

			int i = 0;
			int changeSet_count = 0;
			for (ODataClientBatchResponse result : results) {
				// queries
				if (result instanceof ODataClientChangeSetResponse) {
					@SuppressWarnings("unchecked")
					List<ODataClientBatchResponse> childList = (List<ODataClientBatchResponse>) result.getEntity();
					Assert.assertEquals("ChangeSet request response number must match",
							numberOfReqInChangeSet[changeSet_count], childList.size());
					for (ODataClientBatchResponse childRes : childList) {
						checkOneBatchResult(childRes, i);
						i++;
					}
					changeSet_count++;
				} else {
					checkOneBatchResult(result, i);
					i++;
				}
			}
		} catch (ODataProducerException e) {
			Assert.fail("The batch reqeust failed, with status code=" + e.getOError().getCode() + ", error message="
					+ e.getMessage());
		}
	}

	/**
	 * Check one result.
	 * 
	 * @param result
	 *            the single result
	 * @param idx
	 *            index to status code array
	 */
	@SuppressWarnings("rawtypes")
	private void checkOneBatchResult(ODataClientBatchResponse result, int idx) {
		Assert.assertEquals("The response status code should match", statusCode[idx], result.getStatus());
		Object obj = result.getEntity();

		if (obj instanceof Enumerable) {
			Enumerable cols = (Enumerable) obj;
			Iterator it = cols.iterator();

			while (it.hasNext()) {
				OEntityId obj1 = (OEntityId) it.next();
				Assert.assertTrue("the key should not be null", obj1.getEntityKey() != null);
			}
		} else if (obj instanceof OEntity) {
			// query, create
			OEntity obj1 = (OEntity) obj;
			Assert.assertTrue("the key should not be null", obj1.getEntityKey() != null);
		} else if (obj instanceof Integer) {
			Assert.assertNotSame("the count should not be 0", 0, (Integer) obj);
		}
	}

}
