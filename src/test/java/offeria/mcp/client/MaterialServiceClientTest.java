package offeria.mcp.client;

import offeria.mcp.dto.MaterialSuggestionResponse;
import offeria.mcp.dto.MaterialToolResponse;
import offeria.mcp.dto.MaterialTranslationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MaterialServiceClientTest {

    private MockRestServiceServer server;
    private MaterialServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://material-service");

        server = MockRestServiceServer.bindTo(builder).build();
        client = new MaterialServiceClient(builder.build());
    }

    @Test
    void shouldSearchMaterial() {
        server.expect(
                        once(),
                        requestTo("http://material-service/api/v1/mcp/materials/search")
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"query":"steel pipe"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "tool":"search_material",
                          "query":"steel pipe",
                          "resolved":true,
                          "canonicalEnglishName":"Steel Pipe",
                          "approved":true,
                          "matches":[]
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialToolResponse response =
                client.searchMaterial("steel pipe");

        assertTrue(response.resolved());
        assertEquals(
                "Steel Pipe",
                response.canonicalEnglishName()
        );

        server.verify();
    }

    @Test
    void shouldFindSimilarMaterial() {
        server.expect(
                        requestTo("http://material-service/api/v1/mcp/materials/similar")
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "tool":"find_similar_material",
                          "query":"industrial pipe",
                          "resolved":true,
                          "approved":true,
                          "matches":[]
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialToolResponse response =
                client.findSimilarMaterial(
                        "industrial pipe"
                );

        assertTrue(response.resolved());
        assertEquals(
                "find_similar_material",
                response.tool()
        );

        server.verify();
    }

    @Test
    void shouldGetMaterial() {
        UUID id = UUID.randomUUID();

        server.expect(requestTo(
                        "http://material-service/api/v1/mcp/materials/" + id
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "tool":"get_material",
                          "resolved":true,
                          "canonicalEnglishName":"Steel Pipe",
                          "approved":true,
                          "matches":[]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertTrue(client.getMaterial(id).isPresent());

        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenMaterialNotFound() {
        UUID id = UUID.randomUUID();

        server.expect(requestTo(
                        "http://material-service/api/v1/mcp/materials/" + id
                ))
                .andRespond(withResourceNotFound());

        assertTrue(client.getMaterial(id).isEmpty());

        server.verify();
    }

    @Test
    void shouldGetMaterialTranslation() {
        UUID id = UUID.randomUUID();

        server.expect(requestTo(
                        "http://material-service/api/v1/mcp/materials/"
                                + id + "/translation"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "materialId":"%s",
                          "canonicalEnglishName":"Steel Pipe",
                          "preferredIraqiName":"بايب حديد",
                          "standardArabicName":"أنبوب فولاذي",
                          "approved":true
                        }
                        """.formatted(id), MediaType.APPLICATION_JSON));

        MaterialTranslationResponse response =
                client.getMaterialTranslation(id)
                        .orElseThrow();

        assertTrue(response.approved());
        assertEquals(
                "بايب حديد",
                response.preferredIraqiName()
        );

        server.verify();
    }

    @Test
    void shouldGetUnapprovedSuggestion() {
        server.expect(requestTo(
                        "http://material-service/api/v1/mcp/materials/suggest"
                ))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "inputText":"industrial tubing",
                          "canonicalEnglishName":"Steel Pipe",
                          "preferredIraqiName":"بايب حديد",
                          "approved":false
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialSuggestionResponse response =
                client.suggestMaterialTranslation(
                        "industrial tubing"
                ).orElseThrow();

        assertFalse(response.approved());

        server.verify();
    }

    @Test
    void shouldRejectBlankQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> client.searchMaterial(" ")
        );
    }
}
