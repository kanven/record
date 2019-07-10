package com.kanven.record.ext.plugins.load.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.kanven.record.core.flow.FlowData;
import com.kanven.record.core.meta.Column;
import com.kanven.record.core.meta.Row;
import com.kanven.record.exception.RecordException;

/**
 * 
 * @author kanven
 *
 */
public class EsClient {

	private Client client;

	private IndicesAdminClient admin;

	private Cache cache = new Cache();

	private Map<String, IndexRule> idxm;

	public EsClient(String clusterName, String address, Map<String, IndexRule> idxm) {
		if (StringUtils.isBlank(clusterName)) {
			throw new RecordException("es cluster name should not be null");
		}
		if (StringUtils.isBlank(address)) {
			throw new RecordException("es cluster address should not be null");
		}
		if (idxm == null) {
			this.idxm = new HashMap<>(0);
		} else {
			this.idxm = idxm;
		}
		Settings settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", false)
				.build();
		@SuppressWarnings("resource")
		TransportClient tc = new PreBuiltTransportClient(settings);
		String[] nodes = address.split(",");
		for (String node : nodes) {
			String[] items = node.split(":");
			if (items.length != 2) {
				throw new RecordException("es cluster node format error");
			}
			try {
				TransportAddress td = new InetSocketTransportAddress(InetAddress.getByName(items[0]),
						Integer.parseInt(items[1]));
				tc.addTransportAddress(td);
			} catch (NumberFormatException | UnknownHostException e) {
				throw new RecordException("es cluster node format error", e);
			}
		}
		this.client = tc;
		this.admin = tc.admin().indices();
	}

	public void record(FlowData records) {
		List<List<Row>> merges = records.getRows();
		if (merges == null || merges.isEmpty()) {
			return;
		}
		BulkRequestBuilder request = client.prepareBulk();
		for (List<Row> rows : merges) {
			if (rows == null || rows.isEmpty()) {
				continue;
			}
			for (Row row : rows) {
				createIndex(row);
				IndexRequest index = addRecord(row);
				UpdateRequest update = updateRecord(row);
				update.upsert(index);
				request.add(update);
			}
		}
		if (request.numberOfActions() == 0) {
			return;
		}
		BulkResponse response = request.execute().actionGet();
		if (response.hasFailures()) {
			throw new RecordException("the data add failure,the data is :" + records + ",the reason is:"
					+ response.buildFailureMessage());
		}
	}

	public boolean remove(FlowData records) {
		List<List<Row>> merges = records.getRows();
		if (merges == null || merges.isEmpty()) {
			return false;
		}
		BulkRequestBuilder request = client.prepareBulk();
		for (List<Row> rows : merges) {
			if (rows == null || rows.isEmpty()) {
				continue;
			}
			for (Row row : rows) {
				Set<Column> columns = row.columns();
				if (columns == null || columns.isEmpty()) {
					continue;
				}
				for (Column column : columns) {
					if (column.isKey()) {
						request.add(client.prepareDelete(row.schema(), row.table(),
								column.value() + "" + row.executeTime()));
						break;
					}
				}
			}
		}
		if (request.numberOfActions() == 0) {
			return false;
		}
		BulkResponse response = request.get();
		return !response.hasFailures();
	}

	private UpdateRequest updateRecord(Row row) {
		UpdateRequest request = new UpdateRequest();
		request.index(row.table());
		request.type(row.schema());
		Set<Column> columns = row.columns();
		Set<Column> keys = row.primaryKey();
		String id = "";
		for (Column key : keys) {
			id += key.value();
		}
		id += row.executeTime();
		request.id(id);
		request.routing(id);
		Map<String, Object> items = new HashMap<>(columns.size());
		for (Column column : columns) {
			items.put(column.name(), column.value());
		}
		items.put("execute_time", new Date());
		request.doc(items);
		return request;
	}

	private IndexRequest addRecord(Row row) {
		IndexRequest request = new IndexRequest(row.table(), row.schema());
		Set<Column> columns = row.columns();
		Map<String, Object> items = new HashMap<>(columns.size());
		Set<Column> keys = row.primaryKey();
		String id = "";
		for (Column key : keys) {
			id += key.value();
		}
		id += row.executeTime();
		request.id(id);
		request.routing(id);
		for (Column column : columns) {
			items.put(column.name(), column.value());
		}
		items.put("execute_time", new Date());
		request.source(items);
		return request;
	}

	private void createIndex(Row row) {
		String table = row.table();
		if (cache.hasKey(table)) {
			return;
		}
		// 构造索引名
		String index = table + idxm.get(table).handler();
		if (!existIndex(index)) {
			synchronized (table) {
				if (cache.hasKey(table)) {
					return;
				}
				admin.prepareCreate(index).get();
				try {
					XContentBuilder mapping = jsonBuilder().startObject().startObject("properties");
					Set<Column> columns = row.columns();
					for (Column column : columns) {
						mapping.startObject(column.name()).field("index", "true").field("type", getType(column.type()))
								.endObject();
					}
					mapping.startObject("execute_time").field("index", "true").field("type", "date").endObject();
					mapping.endObject().endObject();
					PutMappingRequest request = Requests.putMappingRequest(row.table()).type(row.schema())
							.source(mapping);
					admin.putMapping(request).actionGet();
				} catch (IOException e) {
					throw new RecordException("the " + table + " index's mapping created failure");
				}
			}
		}
		cache.set(table, index, idxm.get(table).time(), TimeUnit.MILLISECONDS);
	}

	private String getType(String type) {
		if (type.contains("tinyint")) {
			return "integer";
		}
		if (type.contains("bigint")) {
			return "long";
		}
		return "keyword";
	}

	private boolean existIndex(String index) {
		IndicesExistsRequest request = new IndicesExistsRequest(index);
		IndicesExistsResponse response = admin.exists(request).actionGet();
		return response.isExists();
	}

	public void close() {
		client.close();
	}

}
