package com.testCrawler.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDocument {
    @Field
    private String id;

    @Field
    private List<String> phoneData;

    @Field
    private List<String> socialsData;

    @Field
    private List<String> addressData;

    @Field
    private boolean fromCrawl;
}
