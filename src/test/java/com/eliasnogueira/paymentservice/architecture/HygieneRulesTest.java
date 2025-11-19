package com.eliasnogueira.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HygieneRulesTest {

    private static final String BASE = "com.eliasnogueira.paymentservice";

    @Test
    @DisplayName("No generic exceptions should be thrown")
    void noGenericExceptionsShouldBeThrown() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE);
        ArchRule rule = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
        rule.check(imported);
    }

    @Test
    @DisplayName("No classes should use deprecated API")
    void shouldNotUseDeprecatedApi() {
        JavaClasses imported = new ClassFileImporter().importPackages(BASE);
        ArchRule rule = GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;
        rule.check(imported);
    }
}
