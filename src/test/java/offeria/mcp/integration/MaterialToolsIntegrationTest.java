package offeria.mcp.integration;

import offeria.mcp.client.MaterialServiceClient;
import offeria.mcp.dto.MaterialSuggestionResponse;
import offeria.mcp.dto.MaterialToolResponse;
import offeria.mcp.dto.MaterialTranslationResponse;
import offeria.mcp.tools.MaterialTools;
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

class MaterialToolsIntegrationTest {

    private MockRestServiceServer server;
    private MaterialTools tools;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://material-service");

        server = MockRestServiceServer.bindTo(builder).build();

        MaterialServiceClient client =
                new MaterialServiceClient(builder.build());

        tools = new MaterialTools(client);
    }

    @Test
    void shouldSearchMaterialThroughToolAndServiceClient() {
        server.expect(
                        once(),
                        requestTo(
                                "http://material-service/api/v1/mcp/materials/search"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"query":"cement"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "tool":"search_material",
                          "query":"cement",
                          "resolved":true,
                          "materialId":"11111111-1111-1111-1111-111111111111",
                          "canonicalEnglishName":"Portland Cement",
                          "preferredIraqiName":"سمنت",
                          "standardArabicName":"أسمنت بورتلاندي",
                          "unit":"BAG",
                          "category":"Construction",
                          "specification":"50 kg",
                          "matchType":"EXACT",
                          "approved":true,
                          "matches":[]
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialToolResponse response =
                tools.searchMaterial("cement");

        assertNotNull(response);
        assertTrue(response.resolved());
        assertTrue(response.approved());
        assertEquals(
                "Portland Cement",
                response.canonicalEnglishName()
        );
        assertEquals(
                "سمنت",
                response.preferredIraqiName()
        );

        server.verify();
    }

    @Test
    void shouldGetApprovedTranslationThroughToolAndServiceClient() {
        UUID id = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        server.expect(
                        once(),
                        requestTo(
                                "http://material-service/api/v1/mcp/materials/"
                                        + id
                                        + "/translation"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "materialId":"22222222-2222-2222-2222-222222222222",
                          "canonicalEnglishName":"Steel Pipe",
                          "preferredIraqiName":"بايب حديد",
                          "standardArabicName":"أنبوب فولاذي",
                          "unit":"PCS",
                          "category":"Piping",
                          "specification":"Carbon steel",
                          "approved":true
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialTranslationResponse response =
                tools.getMaterialTranslation(id.toString());

        assertNotNull(response);
        assertTrue(response.approved());
        assertEquals(
                "بايب حديد",
                response.preferredIraqiName()
        );

        server.verify();
    }

    @Test
    void shouldKeepAiSuggestionUnapproved() {
        server.expect(
                        once(),
                        requestTo(
                                "http://material-service/api/v1/mcp/materials/suggest"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"query":"industrial tubing"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "inputText":"industrial tubing",
                          "canonicalEnglishName":"Steel Pipe",
                          "preferredIraqiName":"بايب حديد",
                          "standardArabicName":"أنبوب فولاذي",
                          "unit":"PCS",
                          "category":"Piping",
                          "specification":"Carbon steel",
                          "approved":false
                        }
                        """, MediaType.APPLICATION_JSON));

        MaterialSuggestionResponse response =
                tools.suggestMaterialTranslation(
                        "industrial tubing"
                );

        assertNotNull(response);
        assertFalse(response.approved());
        assertEquals(
                "Steel Pipe",
                response.canonicalEnglishName()
        );

        server.verify();
    }

    @Test
    void shouldReturnNullWhenMaterialDoesNotExist() {
        UUID id = UUID.fromString(
                "33333333-3333-3333-3333-333333333333"
        );

        server.expect(
                        once(),
                        requestTo(
                                "http://material-service/api/v1/mcp/materials/"
                                        + id
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        MaterialToolResponse response =
                tools.getMaterial(id.toString());

        assertNull(response);

        server.verify();
    }

    @Test
    void shouldRejectInvalidUuidBeforeCallingMaterialService() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMaterial("invalid-material-id")
        );

        server.verify();
    }

    @Test
    void shouldRejectBlankQueryBeforeCallingMaterialService() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tools.searchMaterial("   ")
        );

        server.verify();
    }
}
