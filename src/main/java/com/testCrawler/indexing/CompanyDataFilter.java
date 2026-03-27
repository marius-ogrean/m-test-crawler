package com.testCrawler.indexing;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CompanyDataFilter implements NodeFilter {
    private final List<String> phoneData = new ArrayList<>();
    private final List<String> socialsData = new ArrayList<>();
    private final List<String> addressData = new ArrayList<>();

    public List<String> getPhoneData() {
        return phoneData;
    }

    public List<String> getSocialsData() {
        return socialsData;
    }

    public List<String> getAddressData() {
        return addressData;
    }

    @Override
    public FilterResult head(Node node, int depth) {
        try {
            var ownText = ((Element)node).ownText();

            if (Pattern.compile(Pattern.quote("phone"), Pattern.CASE_INSENSITIVE).matcher(ownText).find()) {
                phoneData.add(ownText);
            }

            if (Pattern.compile(Pattern.quote("facebook"), Pattern.CASE_INSENSITIVE).matcher(ownText).find()) {
                socialsData.add(ownText);
            }

            if (Pattern.compile(Pattern.quote("instagram"), Pattern.CASE_INSENSITIVE).matcher(ownText).find()) {
                socialsData.add(ownText);
            }

            if (Pattern.compile(Pattern.quote("address"), Pattern.CASE_INSENSITIVE).matcher(ownText).find()) {
                addressData.add(ownText);
            }
        } catch (Exception ex) {

        }

        return FilterResult.CONTINUE;
    }
}
