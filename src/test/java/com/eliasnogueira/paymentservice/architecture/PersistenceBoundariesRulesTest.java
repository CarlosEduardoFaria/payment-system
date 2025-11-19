package com.eliasnogueira.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class PersistenceBoundariesRulesTest {

    private static final String BASE_PACKAGE = "com.eliasnogueira.paymentservice";

    @Test
    @DisplayName("Entities should reside in ..model..")
    void entitiesTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage(BASE_PACKAGE + ".model..");

        rule.check(imported);
    }

    @Test
    @DisplayName("DTOs and Controllers should not be annotated with @Entity")
    void dtoTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().resideInAnyPackage(BASE_PACKAGE + ".dto..", BASE_PACKAGE + ".controller..")
                .should().notBeAnnotatedWith(Entity.class);

        rule.check(imported);
    }
}
