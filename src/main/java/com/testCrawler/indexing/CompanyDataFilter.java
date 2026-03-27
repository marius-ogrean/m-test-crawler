package com.testCrawler.indexing;

import lombok.Getter;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class CompanyDataFilter implements NodeFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CompanyDataFilter.class);

    private final List<String> phoneData = new ArrayList<>();
    private final List<String> socialsData = new ArrayList<>();
    private final List<String> addressData = new ArrayList<>();

    @Override
    public FilterResult head(Node node, int depth) {
        try {
            String ownText;

            if (node instanceof TextNode) {
                return FilterResult.CONTINUE;
            } else if (node instanceof Element) {
                ownText = ((Element)node).ownText();

                if (StringUtil.isBlank(ownText)) {
                    return FilterResult.CONTINUE;
                }
            } else {
                return FilterResult.CONTINUE;
            }

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
            LOG.error("Filter error: ", ex);
        }

        return FilterResult.CONTINUE;
    }
}
