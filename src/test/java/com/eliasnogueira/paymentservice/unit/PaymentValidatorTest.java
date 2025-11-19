/*
 * MIT License
 *
 * Copyright (c) 2025 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.eliasnogueira.paymentservice.unit;

import com.eliasnogueira.paymentservice.validator.PaymentLimitValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentValidatorTest {

    @ParameterizedTest(name = "Payment should be successful when amount is {0}")
    @MethodSource("happyPathsForLimit")
    void happyPaths(BigDecimal amount) {
        assertThat(PaymentLimitValidator.isWithinLimit(amount)).isTrue();
    }

    static Stream<BigDecimal> happyPathsForLimit() {
        return Stream.of(
                new BigDecimal("0.01"),
                new BigDecimal("1999.99"),
                new BigDecimal("2000.00"));
    }

    @ParameterizedTest(name = "Payment should NOT be successful when amount is {0}")
    @MethodSource("edgeCasesForLimit")
    void edgeCases(BigDecimal amount) {
        assertThat(PaymentLimitValidator.isWithinLimit(amount)).isFalse();
    }

    static Stream<BigDecimal> edgeCasesForLimit() {
        return Stream.of(
                new BigDecimal("00.0"),
                new BigDecimal("2000.01"),
                new BigDecimal("3500.00"));
    }
}

