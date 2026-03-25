package io.github.Project.game.core.factory;

/**
 * Identifies the entity type for which a {@link DamageCalculator} should be created.
 *
 * Using an enum instead of a raw String makes invalid type identifiers a
 * compile-time error rather than a silent runtime failure.
 */
public enum EntityType {
    ROCKET,
    SATELLITE,
    SPACE_STATION
}
