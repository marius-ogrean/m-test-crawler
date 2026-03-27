package com.testCrawler.indexing;

import com.testCrawler.models.CompanyDocument;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.stormcrawler.Metadata;
import org.apache.stormcrawler.indexing.AbstractIndexerBolt;
import org.apache.stormcrawler.util.CharsetIdentification;
import org.apache.stormcrawler.util.ConfUtils;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CompanyDataIndexer extends AbstractIndexerBolt {
    private static final Logger LOG = LoggerFactory.getLogger(CompanyDataIndexer.class);
    private final String solrCollection = "companies";

    private int maxLengthCharsetDetection = -1;
    private boolean fastCharsetDetection;
    private SolrClient solrClient;

    @Override
    public void prepare(Map<String, Object> conf, TopologyContext context, OutputCollector collector) {
        this.maxLengthCharsetDetection = ConfUtils.getInt(conf, "detect.charset.maxlength", -1);
        this.fastCharsetDetection = ConfUtils.getBoolean(conf, "detect.charset.fast", false);

        var solrUrl = ConfUtils.getString(conf, "solr.url", "");

        solrClient = new HttpJdkSolrClient.Builder(solrUrl).build();
    }

    @Override
    public void execute(Tuple tuple) {
        var content = tuple.getBinaryByField("content");
        var metadata = (Metadata)tuple.getValueByField("metadata");
        var url = tuple.getStringByField("url");

        String charset;
        if (this.fastCharsetDetection) {
            charset = CharsetIdentification.getCharsetFast(metadata, content, this.maxLengthCharsetDetection);
        } else {
            charset = CharsetIdentification.getCharset(metadata, content, this.maxLengthCharsetDetection);
        }

        Document jsoupDoc;

        try {
            var html = Charset.forName(charset).decode(ByteBuffer.wrap(content)).toString();
            jsoupDoc = Parser.htmlParser().parseInput(html, url);
        } catch (Exception ex) {
            LOG.error("Error parsing HTML", ex);
            return;
        }

        LOG.info("Indexing {}", url);

        var body = jsoupDoc.body();

        var companyDataFilter = new CompanyDataFilter();

        body.filter(companyDataFilter);

        companyDataFilter.getPhoneData().forEach(phone -> {
            System.out.println("Phone: " + phone);
        });

        companyDataFilter.getSocialsData().forEach(social -> {
            System.out.println("Social: " + social);
        });

        companyDataFilter.getAddressData().forEach(address -> {
            System.out.println("Address: " + address);
        });

        LOG.info("Finished indexing {}", url);
    }

    CompanyDocument getCompanyDocument(String domain) {
        final Map<String, String> queryParamMap = new HashMap<>();
        var query = String.format("id:\"%s\"", domain);
        queryParamMap.put("q", query);
        var queryParams = new MapSolrParams(queryParamMap);

        try {
            QueryResponse response = solrClient.query(solrCollection, queryParams);
            var documents = response.getBeans(CompanyDocument.class);

            if (documents.isEmpty()) {
                return null;
            }

            var document = documents.get(0);
            return document;
        } catch (Exception ex) {
            LOG.error("Error retrieving document", ex);
            return null;
        }
    }

    void updateDoc() {
        var document = new SolrInputDocument();
        document.addField("id","mmm");

        Map<String,Object> fieldModifier = new HashMap<>(1);
        fieldModifier.put("add","Cyberpunk");

        document.addField("phoneData", fieldModifier);

        try {
            solrClient.add(document);
        } catch (Exception ex) {
            LOG.error("Error updating doc", ex);
        }
    }
}
