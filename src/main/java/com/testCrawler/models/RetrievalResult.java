package com.testCrawler.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RetrievalResult {
    private CompanyDocument companyDocument;
    private boolean hasError;
}
