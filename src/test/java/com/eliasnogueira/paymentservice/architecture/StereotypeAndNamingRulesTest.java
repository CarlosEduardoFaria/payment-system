package com.eliasnogueira.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class StereotypeAndNamingRulesTest {

    private static final String BASE_PACKAGE = "com.eliasnogueira.paymentservice";

    @Test
    @DisplayName("Controllers should reside in ..controller.. and be named properly")
    void controllersTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule =
                classes().that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage(BASE_PACKAGE + ".controller..")
                .andShould().haveSimpleNameEndingWith("Controller");

        rule.check(imported);
    }

    @Test
    @DisplayName("Services should reside in ..service.. and be named properly")
    void servicesTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().areAnnotatedWith(Service.class)
                .should().resideInAPackage(BASE_PACKAGE + ".service..")
                .andShould().haveSimpleNameEndingWith("Service");

        rule.check(imported);
    }

    @Test
    @DisplayName("Repositories should reside in ..repository.. and be named properly")
    void repositoriesTest() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().areAnnotatedWith(Repository.class)
                .should().resideInAPackage(BASE_PACKAGE + ".repository..")
                .andShould().haveSimpleNameEndingWith("Repository");

        rule.check(imported);
    }
}
