package offeria.mcp.config;

import offeria.mcp.tools.MaterialTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    ToolCallbackProvider materialToolCallbackProvider(
            MaterialTools materialTools
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(materialTools)
                .build();
    }
}
