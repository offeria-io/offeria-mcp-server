package offeria.mcp.dto;

import java.util.UUID;

public record MaterialTranslationResponse(
        UUID materialId,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String subCategory,
        String manufacturer,
        String brand,
        String partNumber,
        String specification,
        boolean approved
) {
}
