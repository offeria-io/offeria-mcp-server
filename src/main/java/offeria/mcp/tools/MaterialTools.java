package offeria.mcp.tools;

import offeria.mcp.client.MaterialServiceClient;
import offeria.mcp.dto.MaterialSuggestionResponse;
import offeria.mcp.dto.MaterialToolResponse;
import offeria.mcp.dto.MaterialTranslationResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MaterialTools {

    private final MaterialServiceClient materialServiceClient;

    public MaterialTools(MaterialServiceClient materialServiceClient) {
        this.materialServiceClient = materialServiceClient;
    }

    @Tool(
            name = "search_material",
            description = """
                    Search the Offeria Material Knowledge Base using
                    approved exact, alias, similar and semantic matching.
                    """
    )
    public MaterialToolResponse searchMaterial(
            @ToolParam(
                    description = "Material name or description to search"
            )
            String query
    ) {
        return materialServiceClient.searchMaterial(query);
    }

    @Tool(
            name = "get_material",
            description = """
                    Get an approved material from the Offeria
                    Material Knowledge Base by UUID.
                    """
    )
    public MaterialToolResponse getMaterial(
            @ToolParam(description = "Material UUID")
            String materialId
    ) {
        UUID id = requireUuid(materialId);

        return materialServiceClient
                .getMaterial(id)
                .orElse(null);
    }

    @Tool(
            name = "find_similar_material",
            description = """
                    Find similar approved materials using fuzzy
                    and semantic matching.
                    """
    )
    public MaterialToolResponse findSimilarMaterial(
            @ToolParam(
                    description = "Material name or description"
            )
            String query
    ) {
        return materialServiceClient.findSimilarMaterial(query);
    }

    @Tool(
            name = "get_material_translation",
            description = """
                    Get approved Iraqi and standard Arabic translation
                    knowledge for a material UUID.
                    """
    )
    public MaterialTranslationResponse getMaterialTranslation(
            @ToolParam(description = "Material UUID")
            String materialId
    ) {
        UUID id = requireUuid(materialId);

        return materialServiceClient
                .getMaterialTranslation(id)
                .orElse(null);
    }

    @Tool(
            name = "suggest_material_translation",
            description = """
                    Suggest translation and material knowledge for an
                    unknown material. AI suggestions are not approved
                    knowledge and require human approval.
                    """
    )
    public MaterialSuggestionResponse suggestMaterialTranslation(
            @ToolParam(
                    description = "Unknown material name or description"
            )
            String query
    ) {
        return materialServiceClient
                .suggestMaterialTranslation(query)
                .orElse(null);
    }

    private UUID requireUuid(String materialId) {
        if (materialId == null || materialId.isBlank()) {
            throw new IllegalArgumentException(
                    "Material ID must not be blank"
            );
        }

        try {
            return UUID.fromString(materialId.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Material ID must be a valid UUID"
            );
        }
    }
}
