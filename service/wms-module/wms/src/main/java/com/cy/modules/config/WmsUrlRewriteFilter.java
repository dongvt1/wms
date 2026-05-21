package com.cy.modules.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL Rewrite Filter: maps standardized /wms/* paths to existing controller paths.
 * This allows frontend to use clean /wms/ prefix while backend controllers remain unchanged.
 */
@Configuration
public class WmsUrlRewriteFilter {

    private static final Map<String, String> URL_MAPPINGS = new LinkedHashMap<>();

    static {
        // New prefix -> Old prefix
        URL_MAPPINGS.put("/wms/qc/stage", "/qms/stage");
        URL_MAPPINGS.put("/wms/qc/session", "/qms/session");
        URL_MAPPINGS.put("/wms/qc/checklist", "/qms/checklist");
        URL_MAPPINGS.put("/wms/qc/iqc", "/qms/iqc");
        URL_MAPPINGS.put("/wms/qc/pqc", "/qms/pqc");
        URL_MAPPINGS.put("/wms/qc/review", "/qms/review");
        URL_MAPPINGS.put("/wms/work-order", "/warehouse/workOrder");
        URL_MAPPINGS.put("/wms/production-line", "/warehouse/productionLine");
        URL_MAPPINGS.put("/wms/product", "/common/product");
        URL_MAPPINGS.put("/wms/category", "/warehouse/category");
        URL_MAPPINGS.put("/wms/material", "/common/material");
        URL_MAPPINGS.put("/wms/bom", "/common/bom");
        URL_MAPPINGS.put("/wms/inventory", "/warehouse/inventory");
        URL_MAPPINGS.put("/wms/area", "/warehouse/area");
        URL_MAPPINGS.put("/wms/shelf", "/warehouse/shelf");
        URL_MAPPINGS.put("/wms/stock", "/warehouse/stock");
        URL_MAPPINGS.put("/wms/supplier", "/warehouse/supplier");
        URL_MAPPINGS.put("/wms/customer", "/warehouse/customer");
        URL_MAPPINGS.put("/wms/order", "/warehouse/orders");
    }

    @Bean
    public FilterRegistrationBean<Filter> wmsRewriteFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpReq = (HttpServletRequest) request;
                String uri = httpReq.getRequestURI();

                for (Map.Entry<String, String> entry : URL_MAPPINGS.entrySet()) {
                    if (uri.startsWith(entry.getKey())) {
                        String newUri = entry.getValue() + uri.substring(entry.getKey().length());
                        request.getRequestDispatcher(newUri).forward(request, response);
                        return;
                    }
                }
                chain.doFilter(request, response);
            }
        });
        registration.addUrlPatterns("/wms/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("wmsUrlRewriteFilter");
        return registration;
    }
}
