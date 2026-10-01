package offeria.mcp.tools;

import offeria.mcp.client.MaterialServiceClient;
import offeria.mcp.dto.MaterialSuggestionResponse;
import offeria.mcp.dto.MaterialToolResponse;
import offeria.mcp.dto.MaterialTranslationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaterialToolsTest {

    private MaterialServiceClient client;
    private MaterialTools tools;

    @BeforeEach
    void setUp() {
        client = mock(MaterialServiceClient.class);
        tools = new MaterialTools(client);
    }

    @Test
    void shouldSearchMaterial() {
        MaterialToolResponse response = materialResponse();

        when(client.searchMaterial("steel pipe"))
                .thenReturn(response);

        assertEquals(
                response,
                tools.searchMaterial("steel pipe")
        );
    }

    @Test
    void shouldGetMaterial() {
        UUID id = UUID.randomUUID();
        MaterialToolResponse response = materialResponse();

        when(client.getMaterial(id))
                .thenReturn(Optional.of(response));

        assertEquals(
                response,
                tools.getMaterial(id.toString())
        );
    }

    @Test
    void shouldFindSimilarMaterial() {
        MaterialToolResponse response = materialResponse();

        when(client.findSimilarMaterial("industrial pipe"))
                .thenReturn(response);

        assertEquals(
                response,
                tools.findSimilarMaterial("industrial pipe")
        );
    }

    @Test
    void shouldGetMaterialTranslation() {
        UUID id = UUID.randomUUID();

        MaterialTranslationResponse response =
                new MaterialTranslationResponse(
                        id,
                        "Steel Pipe",
                        "بايب حديد",
                        "أنبوب فولاذي",
                        "PCS",
                        "Piping",
                        null,
                        null,
                        null,
                        null,
                        "Carbon steel",
                        true
                );

        when(client.getMaterialTranslation(id))
                .thenReturn(Optional.of(response));

        assertEquals(
                response,
                tools.getMaterialTranslation(id.toString())
        );

        assertTrue(response.approved());
    }

    @Test
    void shouldReturnUnapprovedSuggestion() {
        MaterialSuggestionResponse response =
                new MaterialSuggestionResponse(
                        "industrial tubing",
                        "Steel Pipe",
                        "بايب حديد",
                        "أنبوب فولاذي",
                        "PCS",
                        "Piping",
                        "Carbon steel",
                        false
                );

        when(client.suggestMaterialTranslation(
                "industrial tubing"
        )).thenReturn(Optional.of(response));

        MaterialSuggestionResponse result =
                tools.suggestMaterialTranslation(
                        "industrial tubing"
                );

        assertEquals(response, result);
        assertFalse(result.approved());
    }

    @Test
    void shouldRejectInvalidUuid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMaterial("invalid-id")
        );

        verifyNoInteractions(client);
    }

    @Test
    void shouldRejectBlankUuid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tools.getMaterialTranslation(" ")
        );

        verifyNoInteractions(client);
    }

    private MaterialToolResponse materialResponse() {
        return new MaterialToolResponse(
                "search_material",
                "steel pipe",
                true,
                UUID.randomUUID(),
                "Steel Pipe",
                "بايب حديد",
                "أنبوب فولاذي",
                "PCS",
                "Piping",
                "Carbon steel",
                "EXACT",
                null,
                true,
                List.of()
        );
    }
}
