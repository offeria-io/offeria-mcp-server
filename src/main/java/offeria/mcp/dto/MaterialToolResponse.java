package offeria.mcp.dto;

import java.util.List;
import java.util.UUID;

public record MaterialToolResponse(
        String tool,
        String query,
        boolean resolved,
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        String matchType,
        Double similarity,
        boolean approved,
        List<MaterialToolResponse> matches
) {
}
