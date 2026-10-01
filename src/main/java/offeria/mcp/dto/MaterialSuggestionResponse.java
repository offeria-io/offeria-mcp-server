package offeria.mcp.dto;

public record MaterialSuggestionResponse(
        String inputText,
        String canonicalEnglishName,
        String preferredIraqiName,
        String standardArabicName,
        String unit,
        String category,
        String specification,
        boolean approved
) {
}
