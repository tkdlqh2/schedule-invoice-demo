package tkdlqh2.schedule_invoice_demo.outbox.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.integration.BaseIntegrationTest;
import tkdlqh2.schedule_invoice_demo.invoice.Invoice;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceRepository;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceStatus;
import tkdlqh2.schedule_invoice_demo.mock.MockNotificationRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventType;
import tkdlqh2.schedule_invoice_demo.wallet.Wallet;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransaction;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Transactional
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
@DisplayName("InvoiceSendRequestedEventHandler 테스트")
class InvoiceSendRequestedEventHandlerTest extends BaseIntegrationTest {

    @Autowired
    private InvoiceSendRequestedEventHandler handler;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private MockNotificationRepository mockNotificationRepository;

    @Test
    @DisplayName("supports - INVOICE_SEND_REQUESTED 이벤트 지원")
    void supports_InvoiceSendRequested() {
        // when & then
        assertThat(handler.supports(OutboxEventType.INVOICE_SEND_REQUESTED)).isTrue();
    }

    @Test
    @Sql("/sql/test-data-invoice-send-success.sql")
    @DisplayName("handle - 발송 성공")
    void handle_Success() throws Exception {
        // given - SQL로 생성된 테스트 데이터 사용
        Long invoiceId = 1000L;
        OutboxEvent event = OutboxEvent.forInvoiceSendRequest(invoiceId);

        // when
        handler.handle(event);

        // then
        Invoice updatedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT);

        // MockNotification 생성 확인
        long mockNotificationCount = mockNotificationRepository.findByInvoiceId(invoiceId).size();
        assertThat(mockNotificationCount).isEqualTo(1);
    }

    @Test
    @Sql("/sql/test-data-invoice-send-failure.sql")
    @DisplayName("handle - 발송 실패 (전화번호 000으로 시작)")
    void handle_Failure() {
        // given - SQL로 생성된 테스트 데이터 사용
        Long invoiceId = 1001L;
        OutboxEvent event = OutboxEvent.forInvoiceSendRequest(invoiceId);

        // when & then
        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invoice send failed");

        // Invoice는 여전히 PENDING 상태
        Invoice unchangedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(unchangedInvoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);

        // MockNotification 생성되지 않음
        long mockNotificationCount = mockNotificationRepository.findByInvoiceId(invoiceId).size();
        assertThat(mockNotificationCount).isZero();
    }

    @Test
    @Sql("/sql/test-data-invoice-send-compensate.sql")
    @DisplayName("compensate - Invoice 실패 처리 및 Wallet 환불")
    void compensate_RefundWallet() {
        // given - SQL로 생성된 테스트 데이터 사용
        Long invoiceId = 1002L;
        Long walletId = 302L;
        UUID useTransactionId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        OutboxEvent event = OutboxEvent.forInvoiceSendRequest(invoiceId);

        // when
        handler.compensate(event);

        // then - Invoice FAILED 상태
        Invoice failedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(failedInvoice.getStatus()).isEqualTo(InvoiceStatus.FAILED);

        // Wallet 환불 확인
        Wallet refundedWallet = walletRepository.findById(walletId).orElseThrow();
        assertThat(refundedWallet.getBalance()).isEqualTo(100000L);  // 90000 + 10000

        // 환불 트랜잭션 생성 확인
        List<WalletTransaction> transactions = walletTransactionRepository.findByInvoiceId(invoiceId);
        assertThat(transactions).hasSize(2);  // INVOICE_USE + INVOICE_REFUND

        WalletTransaction refundTransaction = transactions.stream()
                .filter(t -> t.getType() == WalletTransactionType.INVOICE_REFUND)
                .findFirst()
                .orElseThrow();
        assertThat(refundTransaction.getAmount()).isEqualTo(10000L);
        assertThat(refundTransaction.getRelatedTransactionId()).isEqualTo(useTransactionId);
    }
}