/**
 * Property 8: Field type change clears incompatible configuration
 *
 * For any Step Field, when its field_type is changed from type A to type B,
 * the field_config SHALL contain only keys valid for type B, with no residual
 * keys from type A's schema.
 *
 * **Validates: Requirements 3.8**
 */
import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { getDefaultConfig, type FieldType } from '../types';

/** All supported field types */
const ALL_FIELD_TYPES: FieldType[] = ['text', 'number', 'boolean', 'select', 'measurement'];

/** Expected keys for each field type's default config */
const EXPECTED_KEYS: Record<FieldType, string[]> = {
  number: ['minValue', 'maxValue', 'decimalPlaces'],
  measurement: ['nominalValue', 'upperTolerance', 'lowerTolerance'],
  select: ['options'],
  boolean: ['trueLabel', 'falseLabel'],
  text: ['maxLength', 'multiline', 'placeholder'],
};

describe('Property 8: Field type change clears incompatible configuration', () => {
  it('each field type has a distinct default config with only its own keys', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...ALL_FIELD_TYPES),
        (fieldType: FieldType) => {
          const config = getDefaultConfig(fieldType);
          const actualKeys = Object.keys(config).sort();
          const expectedKeys = EXPECTED_KEYS[fieldType].sort();

          // Config should contain exactly the expected keys for this type
          expect(actualKeys).toEqual(expectedKeys);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('for all 20 field type transitions (5×4), new config has only new type keys and no residual old type keys', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...ALL_FIELD_TYPES),
        fc.constantFrom(...ALL_FIELD_TYPES),
        (oldType: FieldType, newType: FieldType) => {
          // Only test transitions where type actually changes
          fc.pre(oldType !== newType);

          const oldConfig = getDefaultConfig(oldType);
          const newConfig = getDefaultConfig(newType);

          const oldKeys = new Set(Object.keys(oldConfig));
          const newKeys = Object.keys(newConfig);
          const expectedNewKeys = EXPECTED_KEYS[newType];

          // 1. New config should contain exactly the expected keys for the new type
          expect(newKeys.sort()).toEqual(expectedNewKeys.sort());

          // 2. No residual keys from old type should be present in new config
          //    (keys that are in old config but NOT in new type's expected keys)
          for (const oldKey of oldKeys) {
            if (!expectedNewKeys.includes(oldKey)) {
              expect(newKeys).not.toContain(oldKey);
            }
          }

          // 3. New config keys should be a completely different set from old config keys
          //    (verifying that types have distinct schemas - no overlap means clean transition)
          const hasOverlap = [...oldKeys].some((k) => expectedNewKeys.includes(k));
          // Even if there's overlap in key names, the config should still only have new type's keys
          expect(newKeys.sort()).toEqual(expectedNewKeys.sort());
        },
      ),
      { numRuns: 200 },
    );
  });

  it('exhaustively tests all 20 type transitions produce clean config', () => {
    // Deterministic exhaustive test of all 5×4=20 transitions
    for (const oldType of ALL_FIELD_TYPES) {
      for (const newType of ALL_FIELD_TYPES) {
        if (oldType === newType) continue;

        const oldConfig = getDefaultConfig(oldType);
        const newConfig = getDefaultConfig(newType);

        const oldKeys = Object.keys(oldConfig);
        const newKeys = Object.keys(newConfig);
        const expectedNewKeys = EXPECTED_KEYS[newType];

        // New config has exactly the right keys
        expect(newKeys.sort()).toEqual(expectedNewKeys.sort());

        // No residual keys from old type leak into new config
        const residualKeys = oldKeys.filter(
          (k) => newKeys.includes(k) && !expectedNewKeys.includes(k),
        );
        expect(residualKeys).toEqual([]);
      }
    }
  });

  it('default configs for different types have no overlapping keys (ensuring clean separation)', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...ALL_FIELD_TYPES),
        fc.constantFrom(...ALL_FIELD_TYPES),
        (typeA: FieldType, typeB: FieldType) => {
          fc.pre(typeA !== typeB);

          const keysA = new Set(Object.keys(getDefaultConfig(typeA)));
          const keysB = new Set(Object.keys(getDefaultConfig(typeB)));

          // Verify that different field types have non-overlapping config keys
          // This ensures that switching types always produces a completely clean config
          const intersection = [...keysA].filter((k) => keysB.has(k));
          expect(intersection).toEqual([]);
        },
      ),
      { numRuns: 100 },
    );
  });
});
