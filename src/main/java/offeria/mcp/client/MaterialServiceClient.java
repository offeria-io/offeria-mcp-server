package offeria.mcp.client;

import offeria.mcp.dto.MaterialQueryRequest;
import offeria.mcp.dto.MaterialSuggestionResponse;
import offeria.mcp.dto.MaterialToolResponse;
import offeria.mcp.dto.MaterialTranslationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
public class MaterialServiceClient {

    private final RestClient materialServiceRestClient;

    public MaterialServiceClient(RestClient materialServiceRestClient) {
        this.materialServiceRestClient = materialServiceRestClient;
    }

    public Optional<MaterialToolResponse> getMaterial(UUID materialId) {
        if (materialId == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(
                    materialServiceRestClient
                            .get()
                            .uri("/api/v1/mcp/materials/{materialId}", materialId)
                            .retrieve()
                            .body(MaterialToolResponse.class)
            );
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }

    public MaterialToolResponse searchMaterial(String query) {
        return postMaterialQuery(
                "/api/v1/mcp/materials/search",
                query
        );
    }

    public MaterialToolResponse findSimilarMaterial(String query) {
        return postMaterialQuery(
                "/api/v1/mcp/materials/similar",
                query
        );
    }

    public Optional<MaterialTranslationResponse> getMaterialTranslation(
            UUID materialId
    ) {
        if (materialId == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(
                    materialServiceRestClient
                            .get()
                            .uri(
                                    "/api/v1/mcp/materials/{materialId}/translation",
                                    materialId
                            )
                            .retrieve()
                            .body(MaterialTranslationResponse.class)
            );
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }

    public Optional<MaterialSuggestionResponse> suggestMaterialTranslation(
            String query
    ) {
        String value = requireQuery(query);

        return Optional.ofNullable(
                materialServiceRestClient
                        .post()
                        .uri("/api/v1/mcp/materials/suggest")
                        .body(new MaterialQueryRequest(value))
                        .retrieve()
                        .body(MaterialSuggestionResponse.class)
        );
    }

    private MaterialToolResponse postMaterialQuery(
            String uri,
            String query
    ) {
        String value = requireQuery(query);

        return materialServiceRestClient
                .post()
                .uri(uri)
                .body(new MaterialQueryRequest(value))
                .retrieve()
                .body(MaterialToolResponse.class);
    }

    private String requireQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Material query must not be blank"
            );
        }

        return query.trim();
    }
}
