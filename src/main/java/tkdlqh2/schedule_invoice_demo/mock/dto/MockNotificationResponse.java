package tkdlqh2.schedule_invoice_demo.mock.dto;

import tkdlqh2.schedule_invoice_demo.mock.MockNotification;

import java.time.LocalDateTime;
import java.util.UUID;

public record MockNotificationResponse(
        Long id,
        Long invoiceId,
        UUID corpId,
        String studentName,
        String guardianPhone,
        Long amount,
        String description,
        LocalDateTime sentAt
) {
    public static MockNotificationResponse from(MockNotification mockNotification) {
        return new MockNotificationResponse(
                mockNotification.getId(),
                mockNotification.getInvoiceId(),
                mockNotification.getCorpId(),
                mockNotification.getStudentName(),
                mockNotification.getGuardianPhone(),
                mockNotification.getAmount(),
                mockNotification.getDescription(),
                mockNotification.getSentAt()
        );
    }
}