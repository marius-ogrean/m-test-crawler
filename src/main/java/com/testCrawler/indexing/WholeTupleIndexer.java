package com.testCrawler.indexing;

import org.apache.storm.tuple.Tuple;
import org.apache.stormcrawler.indexing.AbstractIndexerBolt;

public class WholeTupleIndexer  extends AbstractIndexerBolt {
    @Override
    public void execute(Tuple tuple) {
        System.out.println("tuple:");
        for (var field : tuple.getFields()) {
            System.out.println(field + " - " + tuple.getValueByField(field).toString());
        }
    }
}
