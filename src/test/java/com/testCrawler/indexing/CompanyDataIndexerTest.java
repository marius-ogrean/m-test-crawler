package com.testCrawler.indexing;

import com.testCrawler.models.CompanyDocument;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

public class CompanyDataIndexerTest {

    //@Test
    public void retrieveFromSolrTest() {
        var companyDataIndexer = new CompanyDataIndexer();
        var conf = new HashMap<String, Object>();
        conf.put("solr.url", "http://localhost:8983/solr");

        companyDataIndexer.prepare(conf, null, null);

        var result = companyDataIndexer.getCompanyDocument("bostonzen.org");

       /*var companyDataFilter = new CompanyDataFilter();
        companyDataFilter.getPhoneData().addAll(Arrays.asList("phone1", "phone2"));
        companyDataFilter.getSocialsData().addAll(Arrays.asList("social1", "social2"));
        companyDataFilter.getAddressData().addAll(Arrays.asList("address1", "address2"));

        companyDataIndexer.createDocument(companyDataFilter, "test.org");

        var companyDataFilterForUpdate = new CompanyDataFilter();
        companyDataFilterForUpdate.getPhoneData().addAll(Arrays.asList("phone3"));
        companyDataFilterForUpdate.getSocialsData().addAll(Arrays.asList("social3"));
        companyDataFilterForUpdate.getAddressData().addAll(Arrays.asList("address3"));

        companyDataIndexer.updateDocument(companyDataFilterForUpdate, "test.org", CompanyDocument.builder().build());*/
    }
}
