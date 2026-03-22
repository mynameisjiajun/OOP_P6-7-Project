package io.github.Project.game.factory;

import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.damage.RocketDamageCalculator;
import io.github.Project.game.damage.SatelliteDamageCalculator;
import io.github.Project.game.damage.SpaceStationDamageCalculator;

public class DamageCalculatorFactory {
    
    public static DamageCalculator createForEntity(String entityType) {
        switch (entityType) {
            case "Rocket":
                return new RocketDamageCalculator();
            case "Satellite":
                return new SatelliteDamageCalculator();
            case "SpaceStation":
                return new SpaceStationDamageCalculator();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
}