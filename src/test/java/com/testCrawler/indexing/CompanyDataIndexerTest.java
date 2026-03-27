package com.testCrawler.indexing;

import org.junit.Test;

import java.util.HashMap;

public class CompanyDataIndexerTest {

    @Test
    public void retrieveFromSolrTest() {
        var companyDataIndexer = new CompanyDataIndexer();
        var conf = new HashMap<String, Object>();
        conf.put("solr.url", "http://localhost:8983/solr/companies");

        companyDataIndexer.prepare(conf, null, null);

        //var result = companyDataIndexer.getCompanyDocument("url");
        companyDataIndexer.updateDoc();
    }
}
