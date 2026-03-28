package com.testCrawler.indexing;

import com.testCrawler.models.CompanyDocument;
import com.testCrawler.models.RetrievalResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.stormcrawler.Metadata;
import org.apache.stormcrawler.indexing.AbstractIndexerBolt;
import org.apache.stormcrawler.persistence.Status;
import org.apache.stormcrawler.util.CharsetIdentification;
import org.apache.stormcrawler.util.ConfUtils;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyDataIndexer extends AbstractIndexerBolt {
    private static final Logger LOG = LoggerFactory.getLogger(CompanyDataIndexer.class);
    private final String solrCollection = "companies";

    private OutputCollector collector;
    private int maxLengthCharsetDetection = -1;
    private boolean fastCharsetDetection;
    private SolrClient solrClient;
    private String solrUrl;

    @Override
    public void prepare(Map<String, Object> conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.maxLengthCharsetDetection = ConfUtils.getInt(conf, "detect.charset.maxlength", -1);
        this.fastCharsetDetection = ConfUtils.getBoolean(conf, "detect.charset.fast", false);

        solrUrl = ConfUtils.getString(conf, "solr.url", "");

        solrClient = new HttpJdkSolrClient.Builder(solrUrl).build();
    }

    @Override
    public void execute(Tuple tuple) {
        var content = tuple.getBinaryByField("content");
        var metadata = (Metadata)tuple.getValueByField("metadata");
        var url = tuple.getStringByField("url");

        var jsoupDoc = getPageDocument(metadata, content, url);

        if (jsoupDoc == null) {
            return;
        }

        var body = jsoupDoc.body();

        var companyDataFilter = new CompanyDataFilter();

        body.filter(companyDataFilter);

        // uncomment to debug crawled data
        // logCompanyDataFilter(companyDataFilter);

        var domain = metadata.getFirstValue("domain");

        var retrievalResult = getCompanyDocument(domain);

        if (retrievalResult.isHasError()) {
            return;
        }

        if (retrievalResult.getCompanyDocument() == null) {
            createDocument(companyDataFilter, domain);
        } else {
            updateDocument(companyDataFilter, domain, retrievalResult.getCompanyDocument());
        }

        this.collector.emit("status", tuple, new Values(new Object[]{ url, metadata, Status.FETCHED }));
        this.collector.ack(tuple);
    }

    RetrievalResult getCompanyDocument(String domain) {
        final Map<String, String> queryParamMap = new HashMap<>();
        var query = String.format("id:\"%s\"", domain);
        queryParamMap.put("q", query);
        var queryParams = new MapSolrParams(queryParamMap);

        var result = new RetrievalResult();

        try {
            QueryResponse response = solrClient.query(solrCollection, queryParams);
            var documents = response.getBeans(CompanyDocument.class);

            if (documents.isEmpty()) {
                return result;
            }

            var document = documents.get(0);
            result.setCompanyDocument(document);
            return result;
        } catch (Exception ex) {
            LOG.error("Error retrieving document", ex);

            result.setHasError(true);
            return result;
        }
    }

    void createDocument(CompanyDataFilter companyDataFilter, String domain) {
        var companyDocument = CompanyDocument.builder()
                .id(domain)
                .phoneData(companyDataFilter.getPhoneData())
                .socialsData(companyDataFilter.getSocialsData())
                .addressData(companyDataFilter.getAddressData())
                .fromCrawl(List.of(true))
                .build();

        try {
            solrClient.addBean(solrCollection, companyDocument, 100);
        } catch (Exception ex) {
            LOG.error("Error creating document", ex);
        }
    }

    void updateDocument(CompanyDataFilter companyDataFilter, String domain, CompanyDocument existingDocument) {
        var document = new SolrInputDocument();
        document.addField("id",domain);

        boolean shouldUpdate = false;

        if (!companyDataFilter.getPhoneData().isEmpty()) {
            var fieldModifier = new HashMap<String, Object>();
            if (existingDocument.getPhoneData() == null) {
                fieldModifier.put("set", companyDataFilter.getPhoneData());
            } else {
                fieldModifier.put("add", companyDataFilter.getPhoneData());
            }

            document.addField("phoneData", fieldModifier);
            shouldUpdate = true;
        }

        if (!companyDataFilter.getSocialsData().isEmpty()) {
            var fieldModifier = new HashMap<String, Object>();
            if (existingDocument.getSocialsData() == null) {
                fieldModifier.put("set", companyDataFilter.getSocialsData());
            } else {
                fieldModifier.put("add", companyDataFilter.getSocialsData());
            }

            document.addField("socialsData", fieldModifier);
            shouldUpdate = true;
        }

        if (!companyDataFilter.getAddressData().isEmpty()) {
            var fieldModifier = new HashMap<String, Object>();
            if (existingDocument.getAddressData() == null) {
                fieldModifier.put("set", companyDataFilter.getAddressData());
            } else {
                fieldModifier.put("add", companyDataFilter.getAddressData());
            }

            document.addField("addressData", fieldModifier);
            shouldUpdate = true;
        }

        if (!existingDocument.getFromCrawl().get(0)) {
            var fieldModifier = new HashMap<String, Object>();
            fieldModifier.put("set", List.of(true));

            document.addField("fromCrawl", fieldModifier);
            shouldUpdate = true;
        }

        if (!shouldUpdate) {
            return;
        }

        try {
            solrClient.add(solrCollection, document, 100);
        } catch (Exception ex) {
            LOG.error("Error updating doc", ex);
            try {
                solrClient.add(solrCollection, document, 100);
            } catch (Exception ex1) {
                LOG.error("Error updating doc retry", ex1);
            }
        }
    }

    private void logCompanyDataFilter(CompanyDataFilter companyDataFilter) {
        companyDataFilter.getPhoneData().forEach(phone -> {
            System.out.println("Phone: " + phone);
        });

        companyDataFilter.getSocialsData().forEach(social -> {
            System.out.println("Social: " + social);
        });

        companyDataFilter.getAddressData().forEach(address -> {
            System.out.println("Address: " + address);
        });
    }

    private Document getPageDocument(Metadata metadata, byte[] content, String url) {
        String charset;
        if (this.fastCharsetDetection) {
            charset = CharsetIdentification.getCharsetFast(metadata, content, this.maxLengthCharsetDetection);
        } else {
            charset = CharsetIdentification.getCharset(metadata, content, this.maxLengthCharsetDetection);
        }

        Document jsoupDoc = null;

        try {
            var html = Charset.forName(charset).decode(ByteBuffer.wrap(content)).toString();
            jsoupDoc = Parser.htmlParser().parseInput(html, url);
        } catch (Exception ex) {
            LOG.error("Error parsing HTML", ex);
        }

        return jsoupDoc;
    }
}
