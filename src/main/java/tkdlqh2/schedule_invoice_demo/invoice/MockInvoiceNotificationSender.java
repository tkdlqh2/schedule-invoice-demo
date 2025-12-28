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
 * <p>
 * 발송 실패 시뮬레이션:
 * - 보호자 전화번호가 "000"으로 시작하면 발송 실패
 * - 그 외에는 항상 성공
 */
@Component
@RequiredArgsConstructor
public class MockInvoiceNotificationSender implements InvoiceNotificationSender {

    private final MockNotificationRepository mockNotificationRepository;

    @Override
    public boolean send(Invoice invoice) {
        // 발송 실패 시뮬레이션: 전화번호가 "000"으로 시작하면 실패
        if (invoice.getGuardianPhone().startsWith("000")) {
            return false;
        }

        // 발송 로그 저장
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

        return true;  // 발송 성공
    }
}