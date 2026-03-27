package com.testCrawler.indexing;

import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

public class CompanyDataFilterTest {
    @Test
    public void testFilter() {
        var companyDataFilter = new CompanyDataFilter();

        var html = """
                <div>
                    <div>
                        <div>
                            <p>Abc</p>
                            <p>Abc</p>
                            <p>Phone: 123</p>
                        </div>
                    </div>
                    <div>
                        <div>
                            <p>Abc</p>
                            <p>Abc</p>
                            <p>xyz</p>
                        </div>
                    </div>
                    <div>
                        <div>
                            <p>Abc</p>
                            <p>Abc</p>
                            <p>Instagram: abc</p>
                        </div>
                    </div>
                    <div>
                        <div>
                            <p>Abc</p>
                            <p>Abc</p>
                            <p>Address: aaa</p>
                        </div>
                    </div>
                </div>
                """;

        var node = Parser.htmlParser().parseInput(html, "http://url");

        node.body().filter(companyDataFilter);

        Assert.assertEquals(1, companyDataFilter.getPhoneData().size());
        Assert.assertEquals(1, companyDataFilter.getSocialsData().size());
        Assert.assertEquals(1, companyDataFilter.getAddressData().size());
    }
}
