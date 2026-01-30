package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorsConfigDTO {

    @JsonProperty("allowed_origins")
    private List<String> allowedOrigins;

    @JsonProperty("allowed_methods")
    private List<String> allowedMethods;

    @JsonProperty("allowed_headers")
    private List<String> allowedHeaders;

    @JsonProperty("allow_credentials")
    private Boolean allowCredentials;

    @JsonProperty("max_age")
    private Integer maxAge;
}
