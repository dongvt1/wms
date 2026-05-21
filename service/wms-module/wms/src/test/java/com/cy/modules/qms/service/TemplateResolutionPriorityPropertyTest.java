package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.common.entity.Product;
import com.cy.modules.common.mapper.ProductMapper;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.TemplateAssignment;
import com.cy.modules.qms.exception.TemplateNotFoundException;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.TemplateAssignmentMapper;
import com.cy.modules.qms.service.impl.TemplateResolutionServiceImpl;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Template resolution priority order.
 *
 * **Validates: Requirements 5.4, 6.1**
 *
 * Property 11: Template resolution follows priority order.
 * For any product that has a product-specific template, a product-group template,
 * and a default template all active for the same stage type, resolving the template
 * SHALL return the product-specific template. If no product-specific template exists,
 * it SHALL return the product-group template. If neither exists, it SHALL return the
 * default template. If no template exists at any level, TemplateNotFoundException is thrown.
 */
class TemplateResolutionPriorityPropertyTest {

    private static final String[] STAGE_TYPES = {"iqc", "pqc", "fqc"};

    // ==================== Test Infrastructure ====================

    /**
     * Creates a TemplateResolutionServiceImpl with mocked dependencies.
     * Uses invocation counting to determine which priority level is being queried,
     * since the implementation calls findActiveTemplate in order:
     * 1st call = "product", 2nd call = "product_group", 3rd call = "default".
     *
     * @param productId        the product ID
     * @param productGroupId   the product's group/category ID
     * @param stageType        the QC stage type
     * @param productTemplate  template assigned to the product (null if none)
     * @param groupTemplate    template assigned to the product group (null if none)
     * @param defaultTemplate  default template (null if none)
     */
    private TemplateResolutionServiceImpl createService(
            String productId,
            String productGroupId,
            String stageType,
            InspectionTemplate productTemplate,
            InspectionTemplate groupTemplate,
            InspectionTemplate defaultTemplate) {

        TemplateResolutionServiceImpl service = new TemplateResolutionServiceImpl();

        TemplateAssignmentMapper assignmentMapper = Mockito.mock(TemplateAssignmentMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);

        // Mock ProductMapper - return product with categoryId as productGroupId
        Product product = new Product();
        product.setId(productId);
        product.setCategoryId(productGroupId);
        when(productMapper.selectById(productId)).thenReturn(product);

        // Use invocation counter to determine which priority level is being queried.
        // The implementation calls selectList in order: product, product_group, default.
        AtomicInteger callCount = new AtomicInteger(0);
        when(assignmentMapper.selectList(any(QueryWrapper.class))).thenAnswer((InvocationOnMock invocation) -> {
            int call = callCount.incrementAndGet();

            switch (call) {
                case 1: // First call: product-specific
                    if (productTemplate != null) {
                        TemplateAssignment assignment = new TemplateAssignment();
                        assignment.setId("assign-product");
                        assignment.setTemplateId(productTemplate.getId());
                        assignment.setAssignmentType("product");
                        assignment.setTargetId(productId);
                        assignment.setIsActive(1);
                        return Collections.singletonList(assignment);
                    }
                    return Collections.emptyList();

                case 2: // Second call: product-group
                    if (groupTemplate != null) {
                        TemplateAssignment assignment = new TemplateAssignment();
                        assignment.setId("assign-group");
                        assignment.setTemplateId(groupTemplate.getId());
                        assignment.setAssignmentType("product_group");
                        assignment.setTargetId(productGroupId);
                        assignment.setIsActive(1);
                        return Collections.singletonList(assignment);
                    }
                    return Collections.emptyList();

                case 3: // Third call: default
                    if (defaultTemplate != null) {
                        TemplateAssignment assignment = new TemplateAssignment();
                        assignment.setId("assign-default");
                        assignment.setTemplateId(defaultTemplate.getId());
                        assignment.setAssignmentType("default");
                        assignment.setTargetId(null);
                        assignment.setIsActive(1);
                        return Collections.singletonList(assignment);
                    }
                    return Collections.emptyList();

                default:
                    return Collections.emptyList();
            }
        });

        // Configure template mapper to return templates by ID
        when(templateMapper.selectById(any())).thenAnswer((InvocationOnMock invocation) -> {
            String id = invocation.getArgument(0);
            if (productTemplate != null && id.equals(productTemplate.getId())) {
                return productTemplate;
            }
            if (groupTemplate != null && id.equals(groupTemplate.getId())) {
                return groupTemplate;
            }
            if (defaultTemplate != null && id.equals(defaultTemplate.getId())) {
                return defaultTemplate;
            }
            return null;
        });

        // Inject mocks via reflection
        injectField(service, "templateAssignmentMapper", assignmentMapper);
        injectField(service, "inspectionTemplateMapper", templateMapper);
        injectField(service, "productMapper", productMapper);

        return service;
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    private InspectionTemplate createTemplate(String id, String stageType) {
        InspectionTemplate t = new InspectionTemplate();
        t.setId(id);
        t.setTemplateCode("TPL" + id.substring(0, Math.min(11, id.length())));
        t.setTemplateName("Template " + id);
        t.setStageType(stageType);
        t.setStatus("active");
        t.setVersion("1.0");
        return t;
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<String> stageTypes() {
        return Arbitraries.of(STAGE_TYPES);
    }

    @Provide
    Arbitrary<String> productIds() {
        return Arbitraries.strings().alpha().ofLength(8).map(s -> "prod-" + s);
    }

    @Provide
    Arbitrary<String> groupIds() {
        return Arbitraries.strings().alpha().ofLength(8).map(s -> "group-" + s);
    }

    // ==================== Property Tests ====================

    /**
     * Property 11: When a product-specific template exists, it is always returned
     * regardless of whether group and default templates also exist.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 100)
    void productSpecificTemplate_alwaysHasHighestPriority(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId,
            @ForAll boolean hasGroupTemplate,
            @ForAll boolean hasDefaultTemplate) {

        // Product-specific template always exists in this test
        InspectionTemplate productTemplate = createTemplate(
                "tpl-product-" + UUID.randomUUID().toString().substring(0, 8), stageType);
        InspectionTemplate groupTemplate = hasGroupTemplate
                ? createTemplate("tpl-group-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;
        InspectionTemplate defaultTemplate = hasDefaultTemplate
                ? createTemplate("tpl-default-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;

        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, productTemplate, groupTemplate, defaultTemplate);

        // Act
        InspectionTemplate result = service.resolveTemplate(productId, groupId, stageType);

        // Assert: product-specific template is always returned
        assertThat(result).isNotNull();
        assertThat(result.getId())
                .as("Product-specific template should be returned when it exists, " +
                        "regardless of group (%s) or default (%s) templates",
                        hasGroupTemplate, hasDefaultTemplate)
                .isEqualTo(productTemplate.getId());
    }

    /**
     * Property 11: When no product-specific template exists but a product-group template does,
     * the group template is returned regardless of whether a default template exists.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 100)
    void productGroupTemplate_returnedWhenNoProductSpecific(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId,
            @ForAll boolean hasDefaultTemplate) {

        // No product-specific template
        InspectionTemplate groupTemplate = createTemplate(
                "tpl-group-" + UUID.randomUUID().toString().substring(0, 8), stageType);
        InspectionTemplate defaultTemplate = hasDefaultTemplate
                ? createTemplate("tpl-default-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;

        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, null, groupTemplate, defaultTemplate);

        // Act
        InspectionTemplate result = service.resolveTemplate(productId, groupId, stageType);

        // Assert: group template is returned
        assertThat(result).isNotNull();
        assertThat(result.getId())
                .as("Product-group template should be returned when no product-specific exists, " +
                        "regardless of default template (%s)", hasDefaultTemplate)
                .isEqualTo(groupTemplate.getId());
    }

    /**
     * Property 11: When only a default template exists (no product-specific, no group),
     * the default template is returned.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 100)
    void defaultTemplate_returnedWhenNoProductOrGroupSpecific(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId) {

        // Only default template exists
        InspectionTemplate defaultTemplate = createTemplate(
                "tpl-default-" + UUID.randomUUID().toString().substring(0, 8), stageType);

        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, null, null, defaultTemplate);

        // Act
        InspectionTemplate result = service.resolveTemplate(productId, groupId, stageType);

        // Assert: default template is returned
        assertThat(result).isNotNull();
        assertThat(result.getId())
                .as("Default template should be returned when no product-specific or group template exists")
                .isEqualTo(defaultTemplate.getId());
    }

    /**
     * Property 11: When no template exists at any level (product, group, default),
     * TemplateNotFoundException is thrown.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 100)
    void noTemplateAtAnyLevel_throwsTemplateNotFoundException(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId) {

        // No templates at any level
        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, null, null, null);

        // Act & Assert: TemplateNotFoundException is thrown
        assertThatThrownBy(() -> service.resolveTemplate(productId, groupId, stageType))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining(productId)
                .hasMessageContaining(stageType);
    }

    /**
     * Property 11: Priority is strictly ordered - product > group > default.
     * Generate random assignment configurations (all 7 non-empty combinations of presence/absence)
     * and verify the correct template is always returned based on priority.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 200)
    void resolutionPriority_isStrictlyOrdered(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId,
            @ForAll boolean hasProductTemplate,
            @ForAll boolean hasGroupTemplate,
            @ForAll boolean hasDefaultTemplate) {

        // Skip the case where no templates exist (tested separately above)
        Assume.that(hasProductTemplate || hasGroupTemplate || hasDefaultTemplate);

        InspectionTemplate productTemplate = hasProductTemplate
                ? createTemplate("tpl-product-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;
        InspectionTemplate groupTemplate = hasGroupTemplate
                ? createTemplate("tpl-group-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;
        InspectionTemplate defaultTemplate = hasDefaultTemplate
                ? createTemplate("tpl-default-" + UUID.randomUUID().toString().substring(0, 8), stageType)
                : null;

        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, productTemplate, groupTemplate, defaultTemplate);

        // Act
        InspectionTemplate result = service.resolveTemplate(productId, groupId, stageType);

        // Assert: verify priority order
        assertThat(result).isNotNull();

        if (hasProductTemplate) {
            assertThat(result.getId())
                    .as("Product-specific template should have highest priority")
                    .isEqualTo(productTemplate.getId());
        } else if (hasGroupTemplate) {
            assertThat(result.getId())
                    .as("Product-group template should be second priority")
                    .isEqualTo(groupTemplate.getId());
        } else {
            assertThat(result.getId())
                    .as("Default template should be lowest priority")
                    .isEqualTo(defaultTemplate.getId());
        }
    }

    /**
     * Property 11: The resolveTemplate(productId, stageType) overload (without explicit groupId)
     * correctly looks up the product's categoryId and uses it as the group ID for resolution.
     *
     * **Validates: Requirements 5.4, 6.1**
     */
    @Property(tries = 50)
    void resolveWithProductLookup_usesProductCategoryAsGroup(
            @ForAll("stageTypes") String stageType,
            @ForAll("productIds") String productId,
            @ForAll("groupIds") String groupId) {

        // Only group template exists - verifies that product lookup correctly finds the group
        InspectionTemplate groupTemplate = createTemplate(
                "tpl-group-" + UUID.randomUUID().toString().substring(0, 8), stageType);

        TemplateResolutionServiceImpl service = createService(
                productId, groupId, stageType, null, groupTemplate, null);

        // Act - use the overload that looks up product's categoryId
        InspectionTemplate result = service.resolveTemplate(productId, stageType);

        // Assert: group template is found via product's categoryId
        assertThat(result).isNotNull();
        assertThat(result.getId())
                .as("resolveTemplate(productId, stageType) should find group template via product's categoryId")
                .isEqualTo(groupTemplate.getId());
    }
}
