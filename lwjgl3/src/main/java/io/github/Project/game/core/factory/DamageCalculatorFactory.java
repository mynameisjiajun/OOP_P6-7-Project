package io.github.Project.game.core.factory;

import io.github.Project.game.systems.damage.DamageCalculator;
import io.github.Project.game.systems.damage.RocketDamageCalculator;
import io.github.Project.game.systems.damage.SatelliteDamageCalculator;
import io.github.Project.game.systems.damage.SpaceStationDamageCalculator;

/**
 * PATTERN: Factory
 *
 * Creates the appropriate {@link DamageCalculator} for a given entity type.
 * Accepts {@link EntityType} enum values instead of raw Strings, so invalid
 * type identifiers are caught at compile time rather than at runtime.
 */
public class DamageCalculatorFactory {

    private DamageCalculatorFactory() { /* static factory — no instances */ }

    /**
     * Returns the {@link DamageCalculator} for the specified entity type.
     *
     * @param entityType the type of entity being damaged
     * @return a calculator appropriate for that entity type
     */
    public static DamageCalculator createForEntity(EntityType entityType) {
        switch (entityType) {
            case ROCKET:        return new RocketDamageCalculator();
            case SATELLITE:     return new SatelliteDamageCalculator();
            case SPACE_STATION: return new SpaceStationDamageCalculator();
            default:
                throw new IllegalArgumentException("Unhandled EntityType: " + entityType);
        }
    }
}