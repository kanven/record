package ext.plugin.load;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kanven.record.exception.RecordException;

public class EsTest {
	
	private Client client;

	@Before
	public void before(){
		String clusterName = "es-test3";
		String address = "10.204.58.170:9300,10.204.58.171:9300,10.204.58.172:9300";
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
	}
	
	@Test
	public void query(){
		SearchRequestBuilder builder = client.prepareSearch("uic_user");
		builder.setTypes("uic");
		builder.setFrom(0).setSize(100);
		builder.setExplain(Boolean.TRUE);
		//builder.setQuery(QueryBuilders.termsQuery("mobile_phone", "18018765025"));
		builder.setQuery(QueryBuilders.termsQuery("real_name","王五"));
		//builder.setQuery(QueryBuilders.termsQuery("wx_id", "oU31Es3_Gq6wEqXh1X6hX2h7ZXWI"));
		SearchResponse response = builder.execute().actionGet();
		SearchHits hits = response.getHits();
		SearchHit[] documents = hits.getHits();
		for (SearchHit document : documents) {
			System.out.println(document.getSource());
		}
	}
	
	@Test
	public void testIndex(){
		client.admin().indices().prepareCreate("twitter")
        .setSettings(Settings.builder()             
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        )
        .get();  
	}
	
	@Test
	public void testIn() throws IOException{
		IndexResponse response = client.prepareIndex("twitter", "doc", "1")
		        .setSource(jsonBuilder()
		                    .startObject()
		                        .field("user", "kimchy")
		                        .field("postDate", new Date())
		                        .field("message", "trying out Elasticsearch")
		                    .endObject()
		                  )
		        .get();
	}
	
	@Test
	public void testMapping(){
		client.admin().indices().prepareCreate("twitter")    
        .addMapping("doc", "message", "type=text") 
        .get();
	}
	
	@After
	public void after(){
		client.close();
	}
	
}
