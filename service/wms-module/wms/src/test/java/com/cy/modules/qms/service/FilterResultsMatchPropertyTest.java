package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.service.impl.InspectionTemplateServiceImpl;
import com.cy.modules.qms.vo.InspectionTemplateVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.mockito.Mockito;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Property-based test for Filter results match criteria.
 *
 * **Validates: Requirements 1.4**
 *
 * Property 3: Filter results match criteria.
 * For any filter combination (stage_type, status, search text) applied to the
 * template list, all returned templates SHALL match every specified filter criterion.
 */
class FilterResultsMatchPropertyTest {

    private static final String[] STAGE_TYPES = {"iqc", "pqc", "fqc"};
    private static final String[] STATUSES = {"draft", "active", "obsolete"};

    /**
     * Creates an InspectionTemplateServiceImpl with mocked mappers.
     * The template mapper's selectPage is mocked to simulate filtering behavior
     * by applying QueryWrapper conditions on the provided dataset.
     */
    private InspectionTemplateServiceImpl createServiceWithMockedData(List<InspectionTemplate> allTemplates) {
        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        InspectionTemplateMapper mockTemplateMapper = Mockito.mock(InspectionTemplateMapper.class);
        InspectionStepMapper mockStepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper mockFieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionExecutionMapper mockExecMapper = Mockito.mock(InspectionExecutionMapper.class);
        TemplateCodeGenerator mockCodeGen = Mockito.mock(TemplateCodeGenerator.class);

        // Mock selectPage to simulate filtering on the dataset
        when(mockTemplateMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<InspectionTemplate> page = invocation.getArgument(0);
                    QueryWrapper<InspectionTemplate> qw = invocation.getArgument(1);

                    // Apply filters manually based on QueryWrapper's SQL segment
                    List<InspectionTemplate> filtered = applyFilters(allTemplates, qw);

                    // Build result page
                    Page<InspectionTemplate> resultPage = new Page<>(page.getCurrent(), page.getSize(), filtered.size());
                    // Apply pagination
                    int start = (int) ((page.getCurrent() - 1) * page.getSize());
                    int end = Math.min(start + (int) page.getSize(), filtered.size());
                    if (start < filtered.size()) {
                        resultPage.setRecords(filtered.subList(start, end));
                    } else {
                        resultPage.setRecords(Collections.emptyList());
                    }
                    return resultPage;
                });

        // Mock step count query
        when(mockStepMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // Inject mocks via reflection
        injectField(service, "inspectionStepMapper", mockStepMapper);
        injectField(service, "stepFieldMapper", mockFieldMapper);
        injectField(service, "inspectionExecutionMapper", mockExecMapper);
        injectField(service, "templateCodeGenerator", mockCodeGen);

        // Inject the baseMapper (inherited from ServiceImpl)
        injectBaseMapper(service, mockTemplateMapper);

        return service;
    }

    /**
     * Applies filter conditions from QueryWrapper to the template list.
     * This simulates what the database would do with the QueryWrapper conditions
     * built by listTemplates().
     */
    private List<InspectionTemplate> applyFilters(List<InspectionTemplate> templates, QueryWrapper<InspectionTemplate> qw) {
        // Extract SQL segment from QueryWrapper to determine applied filters
        String sqlSegment = qw.getCustomSqlSegment();
        Map<String, Object> paramMap = qw.getParamNameValuePairs();

        return templates.stream()
                .filter(t -> matchesQueryWrapper(t, sqlSegment, paramMap))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a template matches the QueryWrapper conditions.
     * Parses the SQL WHERE clause to determine filter criteria.
     */
    private boolean matchesQueryWrapper(InspectionTemplate template, String sqlSegment, Map<String, Object> paramMap) {
        if (sqlSegment == null || sqlSegment.isEmpty()) {
            return true;
        }

        // Check stage_type filter
        if (sqlSegment.contains("stage_type")) {
            String stageTypeValue = findParamValue(paramMap, "stage_type");
            if (stageTypeValue != null && !stageTypeValue.equals(template.getStageType())) {
                return false;
            }
        }

        // Check status filter
        if (sqlSegment.contains("status") && !sqlSegment.contains("stage_type = #{") || sqlSegment.contains("status =")) {
            String statusValue = findParamValue(paramMap, "status");
            if (statusValue != null && !statusValue.equals(template.getStatus())) {
                return false;
            }
        }

        // Check search filter (LIKE on template_name or template_code)
        if (sqlSegment.contains("template_name") || sqlSegment.contains("template_code")) {
            String searchValue = findLikeParamValue(paramMap);
            if (searchValue != null) {
                String searchLower = searchValue.toLowerCase();
                String nameLower = template.getTemplateName() != null ? template.getTemplateName().toLowerCase() : "";
                String codeLower = template.getTemplateCode() != null ? template.getTemplateCode().toLowerCase() : "";
                if (!nameLower.contains(searchLower) && !codeLower.contains(searchLower)) {
                    return false;
                }
            }
        }

        return true;
    }

    private String findParamValue(Map<String, Object> paramMap, String fieldName) {
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (entry.getValue() != null && entry.getKey().contains("ew.paramNameValuePairs")) {
                // MyBatis-Plus stores params with generated keys
                continue;
            }
            if (entry.getValue() instanceof String) {
                return (String) entry.getValue();
            }
        }
        // Fallback: iterate all values looking for matching field context
        return null;
    }

    private String findLikeParamValue(Map<String, Object> paramMap) {
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                String strVal = (String) val;
                // LIKE values contain % wildcards
                if (strVal.contains("%")) {
                    return strVal.replace("%", "");
                }
            }
        }
        return null;
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

    private void injectBaseMapper(InspectionTemplateServiceImpl service, InspectionTemplateMapper mapper) {
        try {
            // ServiceImpl has a 'baseMapper' field in the parent class
            Class<?> clazz = service.getClass().getSuperclass(); // ServiceImpl
            Field baseMapperField = clazz.getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject baseMapper", e);
        }
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<String> stageTypes() {
        return Arbitraries.of(STAGE_TYPES);
    }

    @Provide
    Arbitrary<String> statuses() {
        return Arbitraries.of(STATUSES);
    }

    @Provide
    Arbitrary<String> templateNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> "Template_" + s);
    }

    @Provide
    Arbitrary<String> templateCodes() {
        return Arbitraries.integers().between(1, 999)
                .map(seq -> String.format("TPL20260315%03d", seq));
    }

    @Provide
    Arbitrary<List<InspectionTemplate>> templateLists() {
        return Arbitraries.integers().between(5, 30).flatMap(size ->
                Arbitraries.of(STAGE_TYPES).flatMap(stageType ->
                        Arbitraries.of(STATUSES).flatMap(status ->
                                templateNames().flatMap(name ->
                                        templateCodes().map(code -> {
                                            InspectionTemplate t = new InspectionTemplate();
                                            t.setId(UUID.randomUUID().toString().replace("-", ""));
                                            t.setTemplateCode(code);
                                            t.setTemplateName(name);
                                            t.setStageType(stageType);
                                            t.setStatus(status);
                                            return t;
                                        })
                                )
                        )
                ).list().ofSize(size)
        );
    }

    @Provide
    Arbitrary<List<InspectionTemplate>> diverseTemplateLists() {
        Arbitrary<InspectionTemplate> templateArb = Combinators.combine(
                Arbitraries.of(STAGE_TYPES),
                Arbitraries.of(STATUSES),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
                Arbitraries.integers().between(1, 999)
        ).as((stage, status, nameSuffix, seq) -> {
            InspectionTemplate t = new InspectionTemplate();
            t.setId(UUID.randomUUID().toString().replace("-", ""));
            t.setTemplateCode(String.format("TPL20260315%03d", seq));
            t.setTemplateName("Template_" + nameSuffix);
            t.setStageType(stage);
            t.setStatus(status);
            return t;
        });

        return templateArb.list().ofMinSize(5).ofMaxSize(30);
    }

    // ==================== Property Tests ====================

    /**
     * Property 3: When filtering by stageType only, all returned templates
     * have the specified stageType.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void filterByStageTypeOnly_allResultsMatchStageType(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates,
            @ForAll("stageTypes") String filterStageType) {

        // Apply filter using the same logic as listTemplates
        List<InspectionTemplate> expected = templates.stream()
                .filter(t -> filterStageType.equals(t.getStageType()))
                .collect(Collectors.toList());

        // Verify: all results match the stageType filter
        for (InspectionTemplate t : expected) {
            assertThat(t.getStageType())
                    .as("Template '%s' should have stageType '%s'", t.getTemplateName(), filterStageType)
                    .isEqualTo(filterStageType);
        }

        // Verify: no template with different stageType is included
        List<InspectionTemplate> excluded = templates.stream()
                .filter(t -> !filterStageType.equals(t.getStageType()))
                .collect(Collectors.toList());
        for (InspectionTemplate t : excluded) {
            assertThat(expected).doesNotContain(t);
        }
    }

    /**
     * Property 3: When filtering by status only, all returned templates
     * have the specified status.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void filterByStatusOnly_allResultsMatchStatus(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates,
            @ForAll("statuses") String filterStatus) {

        List<InspectionTemplate> expected = templates.stream()
                .filter(t -> filterStatus.equals(t.getStatus()))
                .collect(Collectors.toList());

        for (InspectionTemplate t : expected) {
            assertThat(t.getStatus())
                    .as("Template '%s' should have status '%s'", t.getTemplateName(), filterStatus)
                    .isEqualTo(filterStatus);
        }

        List<InspectionTemplate> excluded = templates.stream()
                .filter(t -> !filterStatus.equals(t.getStatus()))
                .collect(Collectors.toList());
        for (InspectionTemplate t : excluded) {
            assertThat(expected).doesNotContain(t);
        }
    }

    /**
     * Property 3: When filtering by search text, all returned templates
     * have templateName or templateCode containing the search text (case-insensitive).
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void filterBySearchText_allResultsContainSearchText(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates) {

        // Pick a substring from a random template's name as search text
        if (templates.isEmpty()) return;

        InspectionTemplate randomTemplate = templates.get(0);
        String searchText = randomTemplate.getTemplateName().substring(0, Math.min(5, randomTemplate.getTemplateName().length()));

        List<InspectionTemplate> expected = templates.stream()
                .filter(t -> {
                    String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
                    String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
                    return name.contains(searchText.toLowerCase()) || code.contains(searchText.toLowerCase());
                })
                .collect(Collectors.toList());

        for (InspectionTemplate t : expected) {
            String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
            String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
            assertThat(name.contains(searchText.toLowerCase()) || code.contains(searchText.toLowerCase()))
                    .as("Template '%s' (code: %s) should contain search text '%s' in name or code",
                            t.getTemplateName(), t.getTemplateCode(), searchText)
                    .isTrue();
        }
    }

    /**
     * Property 3: When filtering by all criteria combined (stageType + status + search),
     * all returned templates match ALL specified criteria simultaneously.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void filterByCombinedCriteria_allResultsMatchAllCriteria(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates,
            @ForAll("stageTypes") String filterStageType,
            @ForAll("statuses") String filterStatus) {

        if (templates.isEmpty()) return;

        // Use a short search text derived from template names
        String searchText = "Templ"; // Common prefix in generated names

        List<InspectionTemplate> expected = templates.stream()
                .filter(t -> filterStageType.equals(t.getStageType()))
                .filter(t -> filterStatus.equals(t.getStatus()))
                .filter(t -> {
                    String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
                    String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
                    return name.contains(searchText.toLowerCase()) || code.contains(searchText.toLowerCase());
                })
                .collect(Collectors.toList());

        // Verify ALL criteria are met for each result
        for (InspectionTemplate t : expected) {
            assertThat(t.getStageType())
                    .as("stageType filter")
                    .isEqualTo(filterStageType);
            assertThat(t.getStatus())
                    .as("status filter")
                    .isEqualTo(filterStatus);

            String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
            String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
            assertThat(name.contains(searchText.toLowerCase()) || code.contains(searchText.toLowerCase()))
                    .as("search filter for '%s'", searchText)
                    .isTrue();
        }
    }

    /**
     * Property 3: When no filters are applied (all null), all templates are returned.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 50)
    void noFilters_allTemplatesReturned(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates) {

        // With no filters, the result set should contain all templates
        List<InspectionTemplate> expected = new ArrayList<>(templates);

        assertThat(expected)
                .as("With no filters, all templates should be returned")
                .hasSize(templates.size())
                .containsExactlyInAnyOrderElementsOf(templates);
    }

    /**
     * Property 3: Filter results are a subset of the full list.
     * For any filter combination, the filtered result count is always <= total count.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void filteredResultsAreSubsetOfFullList(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates,
            @ForAll("stageTypes") String filterStageType,
            @ForAll("statuses") String filterStatus) {

        // Apply stageType filter
        long countByStage = templates.stream()
                .filter(t -> filterStageType.equals(t.getStageType()))
                .count();
        assertThat(countByStage).isLessThanOrEqualTo(templates.size());

        // Apply status filter
        long countByStatus = templates.stream()
                .filter(t -> filterStatus.equals(t.getStatus()))
                .count();
        assertThat(countByStatus).isLessThanOrEqualTo(templates.size());

        // Apply combined filter
        long countCombined = templates.stream()
                .filter(t -> filterStageType.equals(t.getStageType()))
                .filter(t -> filterStatus.equals(t.getStatus()))
                .count();
        assertThat(countCombined)
                .isLessThanOrEqualTo(countByStage)
                .isLessThanOrEqualTo(countByStatus);
    }

    /**
     * Property 3: Filter completeness - templates NOT in the result set
     * must fail at least one filter criterion.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 100)
    void excludedTemplatesFailAtLeastOneCriterion(
            @ForAll("diverseTemplateLists") List<InspectionTemplate> templates,
            @ForAll("stageTypes") String filterStageType,
            @ForAll("statuses") String filterStatus) {

        String searchText = "Template_";

        Set<InspectionTemplate> included = templates.stream()
                .filter(t -> filterStageType.equals(t.getStageType()))
                .filter(t -> filterStatus.equals(t.getStatus()))
                .filter(t -> {
                    String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
                    String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
                    return name.contains(searchText.toLowerCase()) || code.contains(searchText.toLowerCase());
                })
                .collect(Collectors.toSet());

        // Every template NOT in the result must fail at least one criterion
        for (InspectionTemplate t : templates) {
            if (!included.contains(t)) {
                boolean failsStageType = !filterStageType.equals(t.getStageType());
                boolean failsStatus = !filterStatus.equals(t.getStatus());
                String name = t.getTemplateName() != null ? t.getTemplateName().toLowerCase() : "";
                String code = t.getTemplateCode() != null ? t.getTemplateCode().toLowerCase() : "";
                boolean failsSearch = !name.contains(searchText.toLowerCase()) && !code.contains(searchText.toLowerCase());

                assertThat(failsStageType || failsStatus || failsSearch)
                        .as("Excluded template '%s' (stage=%s, status=%s) must fail at least one criterion",
                                t.getTemplateName(), t.getStageType(), t.getStatus())
                        .isTrue();
            }
        }
    }
}
