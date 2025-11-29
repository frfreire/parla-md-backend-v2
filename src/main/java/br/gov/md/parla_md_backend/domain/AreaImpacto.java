package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "parladb.area_impacto")
public class AreaImpacto {

    @Id
    private String id;
    private String name;
    private List<String> keywords;

    public AreaImpacto() {

    }

    public AreaImpacto(String id, String name, List<String> keywords) {
        this.id = id;
        this.name = name;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
