package com.autosalon.service;

import com.autosalon.TestContext;
import com.autosalon.domain.enums.ComponentType;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.IncompatibleComponentException;
import com.autosalon.domain.model.CarModel;
import com.autosalon.domain.model.ComponentOption;
import com.autosalon.domain.model.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguratorServiceTest {
    private TestContext context;
    private CarModel bmw320;
    private CarModel bmw330;
    private ComponentOption wheels17;
    private ComponentOption wheels19;
    private ComponentOption automatic8;
    private ComponentOption steeringStandard;
    private ComponentOption steeringHeated;
    private ComponentOption graphiteInterior;
    private ComponentOption dakotaInterior;

    @BeforeEach
    void setUp() {
        context = new TestContext();
        bmw320 = context.createBmw320();
        bmw330 = context.createBmw330();
        wheels17 = context.configuratorService.registerComponentOption(
                ComponentType.WHEELS,
                "17'' Standard",
                BigDecimal.ZERO,
                Set.of(bmw320.getId())
        );
        wheels19 = context.configuratorService.registerComponentOption(
                ComponentType.WHEELS,
                "19'' M-Sport",
                BigDecimal.valueOf(95_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );
        automatic8 = context.configuratorService.registerComponentOption(
                ComponentType.TRANSMISSION,
                "Automatic 8AT",
                BigDecimal.ZERO,
                Set.of(bmw320.getId(), bmw330.getId())
        );
        steeringStandard = context.configuratorService.registerComponentOption(
                ComponentType.STEERING_WHEEL,
                "Sport leather standard",
                BigDecimal.ZERO,
                Set.of(bmw320.getId(), bmw330.getId())
        );
        steeringHeated = context.configuratorService.registerComponentOption(
                ComponentType.STEERING_WHEEL,
                "M-Sport heated",
                BigDecimal.valueOf(25_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );
        graphiteInterior = context.configuratorService.registerComponentOption(
                ComponentType.INTERIOR,
                "Graphite fabric",
                BigDecimal.ZERO,
                Set.of(bmw320.getId())
        );
        dakotaInterior = context.configuratorService.registerComponentOption(
                ComponentType.INTERIOR,
                "Dakota leather",
                BigDecimal.valueOf(110_000),
                Set.of(bmw320.getId(), bmw330.getId())
        );

        context.configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.WHEELS, wheels17.getId());
        context.configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.TRANSMISSION, automatic8.getId());
        context.configuratorService.assignBaseComponent(
                bmw320.getId(),
                ComponentType.STEERING_WHEEL,
                steeringStandard.getId()
        );
        context.configuratorService.assignBaseComponent(bmw320.getId(), ComponentType.INTERIOR, graphiteInterior.getId());
    }

    @Test
    void buildsValidConfigurationAndCalculatesTotalPrice() {
        Configuration configuration = context.configuratorService.buildConfiguration(
                bmw320.getId(),
                Map.of(
                        ComponentType.WHEELS, wheels19.getId(),
                        ComponentType.TRANSMISSION, automatic8.getId(),
                        ComponentType.STEERING_WHEEL, steeringHeated.getId(),
                        ComponentType.INTERIOR, dakotaInterior.getId()
                )
        );

        assertEquals(0, BigDecimal.valueOf(4_230_000).compareTo(configuration.getTotalPrice()));
        assertEquals("BMW 320i", configuration.getModel().getDisplayName());
        assertEquals(wheels19.getId(), configuration.getComponent(ComponentType.WHEELS).getId());
    }

    @Test
    void buildsBaseConfigurationFromModelDefaults() {
        Configuration configuration = context.configuratorService.buildBaseConfiguration(bmw320.getId());

        assertEquals(0, BigDecimal.valueOf(4_000_000).compareTo(configuration.getTotalPrice()));
        assertEquals(graphiteInterior.getName(), configuration.getComponent(ComponentType.INTERIOR).getName());
    }

    @Test
    void rejectsIncompatibleComponent() {
        ComponentOption performanceInterior = context.configuratorService.registerComponentOption(
                ComponentType.INTERIOR,
                "Performance sport",
                BigDecimal.valueOf(160_000),
                Set.of(bmw330.getId())
        );

        IncompatibleComponentException exception = assertThrows(
                IncompatibleComponentException.class,
                () -> context.configuratorService.buildConfiguration(
                        bmw320.getId(),
                        Map.of(
                                ComponentType.WHEELS, wheels19.getId(),
                                ComponentType.TRANSMISSION, automatic8.getId(),
                                ComponentType.STEERING_WHEEL, steeringHeated.getId(),
                                ComponentType.INTERIOR, performanceInterior.getId()
                        )
                )
        );

        assertTrue(exception.getMessage().contains("not available for model BMW 320i"));
    }

    @Test
    void rejectsMissingRequiredComponent() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> context.configuratorService.buildConfiguration(
                        bmw320.getId(),
                        Map.of(
                                ComponentType.WHEELS, wheels19.getId(),
                                ComponentType.TRANSMISSION, automatic8.getId(),
                                ComponentType.STEERING_WHEEL, steeringHeated.getId()
                        )
                )
        );

        assertTrue(exception.getMessage().contains("missing required component"));
    }

    @Test
    void listsOnlyCompatibleOptionsForModelAndType() {
        ComponentOption optionForBmw330Only = context.configuratorService.registerComponentOption(
                ComponentType.WHEELS,
                "20'' Track",
                BigDecimal.valueOf(150_000),
                Set.of(bmw330.getId())
        );

        var options = context.configuratorService.listOptionsForModel(bmw320.getId(), ComponentType.WHEELS);

        assertTrue(options.contains(wheels17));
        assertTrue(options.contains(wheels19));
        assertTrue(options.stream().noneMatch(option -> option.getId().equals(optionForBmw330Only.getId())));
    }
}
