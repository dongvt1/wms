package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.service.impl.TemplateCodeGeneratorImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based test for TemplateCodeGenerator.
 *
 * **Validates: Requirements 1.1, 1.2**
 *
 * Property 1: Template code generation produces unique codes in correct format.
 * For any number of templates created within the same system, each generated
 * template_code SHALL match the format TPL\d{8}\d{3} AND no two templates
 * SHALL have the same template_code.
 */
class TemplateCodeGeneratorPropertyTest {

    private static final Pattern CODE_FORMAT = Pattern.compile("^TPL\\d{8}\\d{3}$");

    /**
     * Creates a TemplateCodeGeneratorImpl with a mocked mapper that simulates
     * sequential code generation. The mock tracks the last generated code
     * and returns it on subsequent calls, mimicking real DB behavior.
     */
    private TemplateCodeGeneratorImpl createGeneratorWithSequentialMock() {
        TemplateCodeGeneratorImpl generator = new TemplateCodeGeneratorImpl();
        InspectionTemplateMapper mockMapper = Mockito.mock(InspectionTemplateMapper.class);

        // Track the last generated template to simulate DB state
        AtomicInteger counter = new AtomicInteger(0);

        when(mockMapper.selectOne(any(QueryWrapper.class))).thenAnswer(invocation -> {
            int currentCount = counter.get();
            if (currentCount == 0) {
                return null; // No previous code exists
            }
            // Return a template with the last generated code
            InspectionTemplate template = new InspectionTemplate();
            template.setTemplateCode(String.format("TPL%s%03d",
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                    currentCount));
            return template;
        });

        // After each generateCode call, increment the counter
        // We use a wrapper approach: intercept selectOne to track state
        when(mockMapper.selectOne(any(QueryWrapper.class))).thenAnswer(invocation -> {
            int currentCount = counter.getAndIncrement();
            if (currentCount == 0) {
                return null;
            }
            InspectionTemplate template = new InspectionTemplate();
            template.setTemplateCode(String.format("TPL%s%03d",
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                    currentCount));
            return template;
        });

        // Inject mock mapper via reflection
        try {
            Field mapperField = TemplateCodeGeneratorImpl.class.getDeclaredField("inspectionTemplateMapper");
            mapperField.setAccessible(true);
            mapperField.set(generator, mockMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock mapper", e);
        }

        return generator;
    }

    /**
     * Property 1: Template code generation produces unique codes in correct format.
     *
     * For N generated codes:
     * - Each code matches the regex TPL\d{8}\d{3}
     * - All codes are unique (no duplicates)
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    @Property(tries = 50)
    void allGeneratedCodesMatchFormatAndAreUnique(@ForAll @IntRange(min = 1, max = 20) int numberOfCodes) {
        TemplateCodeGeneratorImpl generator = createGeneratorWithSequentialMock();

        Set<String> generatedCodes = new HashSet<>();

        for (int i = 0; i < numberOfCodes; i++) {
            String code = generator.generateCode();

            // Verify format: TPL + 8 digits (date) + 3 digits (sequence)
            assertThat(code)
                    .as("Generated code should match format TPL\\d{8}\\d{3}")
                    .matches(CODE_FORMAT.pattern());

            // Verify code length is exactly 14 characters: "TPL" (3) + date (8) + seq (3)
            assertThat(code).hasSize(14);

            // Verify prefix
            assertThat(code).startsWith("TPL");

            // Verify the date part is 8 digits
            String datePart = code.substring(3, 11);
            assertThat(datePart).matches("\\d{8}");

            // Verify the sequence part is 3 digits
            String seqPart = code.substring(11);
            assertThat(seqPart).matches("\\d{3}");

            // Verify uniqueness: code should not already exist in the set
            assertThat(generatedCodes.add(code))
                    .as("Code '%s' should be unique but was already generated", code)
                    .isTrue();
        }

        // Final check: the set size should equal the number of codes generated
        assertThat(generatedCodes).hasSize(numberOfCodes);
    }

    /**
     * Property: Sequence numbers are monotonically increasing within the same day.
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    @Property(tries = 50)
    void sequenceNumbersAreMonotonicallyIncreasing(@ForAll @IntRange(min = 2, max = 15) int numberOfCodes) {
        TemplateCodeGeneratorImpl generator = createGeneratorWithSequentialMock();

        int previousSeq = 0;
        for (int i = 0; i < numberOfCodes; i++) {
            String code = generator.generateCode();
            int seq = Integer.parseInt(code.substring(11));

            assertThat(seq)
                    .as("Sequence number should be greater than previous (%d)", previousSeq)
                    .isGreaterThan(previousSeq);

            previousSeq = seq;
        }
    }
}
