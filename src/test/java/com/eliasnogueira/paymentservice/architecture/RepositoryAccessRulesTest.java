package com.eliasnogueira.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class RepositoryAccessRulesTest {

    private static final String BASE = "com.eliasnogueira.paymentservice";

    @Test
    @DisplayName("Controllers should not access repositories")
    void controllersTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE);

        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".controller..")
                .should().accessClassesThat().resideInAPackage(BASE + ".repository..");

        rule.check(imported);
    }

    @Test
    @DisplayName("Repositories should be interfaces")
    void repositoriesTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE);

        ArchRule rule = classes()
                .that().resideInAPackage(BASE + ".repository..")
                .should().beInterfaces();

        rule.check(imported);
    }
}
