package com.testCrawler.indexing;

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
import java.util.Map;

public class CompanyDataIndexer extends AbstractIndexerBolt {
    private static final Logger LOG = LoggerFactory.getLogger(CompanyDataIndexer.class);

    private int maxLengthCharsetDetection = -1;
    private boolean fastCharsetDetection;

    @Override
    public void prepare(Map<String, Object> conf, TopologyContext context, OutputCollector collector) {
        super.prepare(conf, context, collector);

        this.maxLengthCharsetDetection = ConfUtils.getInt(conf, "detect.charset.maxlength", -1);
        this.fastCharsetDetection = ConfUtils.getBoolean(conf, "detect.charset.fast", false);
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

        LOG.info("Indexing " + url);

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

        LOG.info("Finished indexing " + url);
    }
}
