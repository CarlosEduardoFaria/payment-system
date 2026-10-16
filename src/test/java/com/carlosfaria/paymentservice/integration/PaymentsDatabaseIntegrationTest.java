package com.carlosfaria.paymentservice.integration;

import com.carlosfaria.paymentservice.model.Payment;
import com.carlosfaria.paymentservice.model.enums.PaymentSource;
import com.carlosfaria.paymentservice.model.enums.PaymentStatus;
import com.carlosfaria.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentsDatabaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Should correctly sum daily payments by payerId")
    void shouldSumDailyPaymentsByPayerId() {

        //Gera um ID único
        final UUID payerId = UUID.randomUUID();
        //Cria o pagamento 1
        final var firstPayment =  createPayment(payerId, PaymentSource.PIX, 50.00, PaymentStatus.PENDING);
        //Cria o pagamento 2
        final var secondPayment = createPayment(payerId, PaymentSource.CREDIT_CARD, 350.00, PaymentStatus.PENDING);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal total = paymentRepository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        assertThat(total).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Should return zero when there are no payments for the payerId on the day")
    void shouldReturnZeroWhenNoPayments() {
        UUID nonExistentPayerId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal total = paymentRepository.sumPaymentsByPayerIdAndDate(nonExistentPayerId, startOfDay, endOfDay);

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should find payments by payerId")
    void shouldFindPaymentsByPayerId() {

        //Gera um ID único
        final UUID payerId = UUID.randomUUID();
        //Cria o pagamento 1
        final var firstPayment =  createPayment(payerId, PaymentSource.PIX, 50.00, PaymentStatus.PENDING);
        //Cria o pagamento 2
        final var secondPayment = createPayment(payerId, PaymentSource.CREDIT_CARD, 150.00, PaymentStatus.PAID);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        List<Payment> payments = paymentRepository.findAllByPayerId(payerId);

        assertThat(payments).hasSize(2);
        assertThat(payments).extracting(Payment::getPayerId).containsOnly(payerId);
        assertThat(payments).extracting(Payment::getAmount).contains(BigDecimal.valueOf(50.00), BigDecimal.valueOf(150.00));
    }

    @Test
    @DisplayName("Should return empty list when there are no payments for the payerId")
    void shouldReturnEmptyListForNonExistentPayerId() {
        List<Payment> payments = paymentRepository.findAllByPayerId(UUID.randomUUID());

        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Shold not sum payments from diferent days")
    void sholdNotSumPaymentsFromDiferentDays() {

        //Gera um ID único
        final UUID payerId = UUID.randomUUID();
        //Cria o pagamento 1
        final var firstPayment =  createPayment(payerId, PaymentSource.PIX, 50.00, PaymentStatus.PENDING);
        //Cria o pagamento 2
        final var secondPayment = createPayment(payerId, PaymentSource.CREDIT_CARD, 350.00, PaymentStatus.PENDING);

        //Perssite os pagamentos e disponibiliza o dado
        paymentRepository.saveAll(List.of(firstPayment, secondPayment));
        entityManager.flush();

        //Cria os timestamps
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterday = startOfDay.minusDays(1).plusHours(10);

        //Atualiza a data do segundo pagamento para uma data retroativa
        entityManager.getEntityManager().createQuery("""
        UPDATE Payment p SET p.createdAt = :createdAt
        WHERE p.id = :id
        """)
                .setParameter("createdAt", yesterday)
                .setParameter("id", secondPayment.getId())
                .executeUpdate();

        //Limpa o cache do entityManager
        entityManager.clear();

        //Executa a instrução de soma
        var total = paymentRepository.sumPaymentsByPayerIdAndDate(payerId, startOfDay, endOfDay);

        //Não deve somar o pagamento do dia anterior
        assertThat(total).isEqualByComparingTo("50.00");
    }

    //Helper para a criação de pagamentos
    private Payment createPayment(UUID payerId, PaymentSource source, Double amount, PaymentStatus status) {
        return Payment.builder()
                .payerId(payerId)
                .paymentSource(source)
                .amount(BigDecimal.valueOf(amount))
                .status(status)
                .build();
    }
}