/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import me.zodac.folding.FoldingStatsApplication;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * {@link Configuration} used to define the Swagger OpenApi documentation for the {@code folding-stats} project.
 */
@SecurityScheme(
    name = "basicAuthentication",
    scheme = "basic",
    type = SecuritySchemeType.HTTP
)
@PropertySource("classpath:application-swagger.properties")
@Configuration
public class SwaggerConfiguration {

    /**
     * Defines the Swagger documentation for the overall project.
     *
     * @param applicationName    the name of the {@link FoldingStatsApplication}
     * @param applicationVersion the version of the {@link FoldingStatsApplication}
     * @return the {@link GroupedOpenApi} documentation
     */
    @Bean
    public GroupedOpenApi projectInfo(@Value("${application.name}") final String applicationName,
                                      @Value("${application.version}") final String applicationVersion) {
        final String contactName = EnvironmentVariableUtils.get("CONTACT_NAME");
        final String forumLink = EnvironmentVariableUtils.get("FORUM_LINK");
        final String restEndpointUrl = EnvironmentVariableUtils.get("REST_ENDPOINT_URL");

        final Info projectInfo = new Info()
            .title(String.format("The '%s' REST API", applicationName))
            .description(String.format("REST API for the '%s' project", applicationName))
            .contact(new Contact().name(contactName).url(forumLink))
            .license(new License().name("0BSD License").url("https://github.com/zodac/folding-stats/blob/master/LICENSE/"))
            .version(applicationVersion);

        return GroupedOpenApi.builder()
            .group("FoldingStats")
            .addOpenApiCustomizer(openApi -> openApi.info(projectInfo))
            .addOpenApiCustomizer(openApi -> openApi.servers(List.of(new Server().description("Main Website").url(restEndpointUrl))))
            .pathsToExclude("/health/*")
            .build();
    }
}
