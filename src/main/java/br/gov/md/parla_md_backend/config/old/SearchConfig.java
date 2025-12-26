package br.gov.md.parla_md_backend.config.old;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class SearchConfig {
    @Value("classpath:search_terms.txt")
    private Resource searchTermsResource;

    public List<String> getSearchTerms() throws IOException {
        return Files.readAllLines(Paths.get(searchTermsResource.getURI()));
    }
}
