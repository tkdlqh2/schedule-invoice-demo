package tkdlqh2.schedule_invoice_demo.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tkdlqh2.schedule_invoice_demo.mock.MockNotification;
import tkdlqh2.schedule_invoice_demo.mock.MockNotificationRepository;

import java.time.LocalDateTime;

/**
 * Mock Invoice 발송 구현체
 * <p>
 * 실제 발송 대신 DB에 발송 로그를 기록합니다.
 */
@Component
@RequiredArgsConstructor
public class MockInvoiceNotificationSender implements InvoiceNotificationSender {

    private final MockNotificationRepository mockNotificationRepository;

    @Override
    public void send(Invoice invoice) {
        MockNotification mockNotification = MockNotification.builder()
                .invoiceId(invoice.getId())
                .corpId(invoice.getCorp().getId())
                .studentName(invoice.getStudentName())
                .guardianPhone(invoice.getGuardianPhone())
                .amount(invoice.getAmount())
                .description(invoice.getDescription())
                .sentAt(LocalDateTime.now())
                .build();

        mockNotificationRepository.save(mockNotification);
    }
}