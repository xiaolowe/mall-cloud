package com.mall.gateway.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Serves the entry document for the two Vue single-page apps bundled in this jar.
 * Actual assets (/assets/**, /admin/assets/**) are handled by the default static
 * resource handler; this only answers the bare directory paths.
 */
@RestController
public class SpaIndexController {

    @GetMapping(value = {"/admin", "/admin/"}, produces = MediaType.TEXT_HTML_VALUE)
    public Mono<Resource> adminIndex() {
        return Mono.just(new ClassPathResource("static/admin/index.html"));
    }

    @GetMapping(value = {"/", ""}, produces = MediaType.TEXT_HTML_VALUE)
    public Mono<Resource> storefrontIndex() {
        return Mono.just(new ClassPathResource("static/index.html"));
    }
}
