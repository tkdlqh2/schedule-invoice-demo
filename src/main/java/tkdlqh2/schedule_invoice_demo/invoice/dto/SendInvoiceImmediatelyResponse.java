package tkdlqh2.schedule_invoice_demo.invoice.dto;

import lombok.Builder;
import tkdlqh2.schedule_invoice_demo.invoice.Invoice;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 즉시 발송 Invoice 생성 응답
 */
@Builder
public record SendInvoiceImmediatelyResponse(
        Long invoiceId,
        UUID corpId,
        String corpName,
        InvoiceStatus status,
        String studentName,
        String guardianPhone,
        Long amount,
        String description,
        Long remainingWalletBalance,
        LocalDateTime createdAt
) {
    public static SendInvoiceImmediatelyResponse from(Invoice invoice, Long remainingBalance) {
        return SendInvoiceImmediatelyResponse.builder()
                .invoiceId(invoice.getId())
                .corpId(invoice.getCorp().getId())
                .corpName(invoice.getCorp().getName())
                .status(invoice.getStatus())
                .studentName(invoice.getStudentName())
                .guardianPhone(invoice.getGuardianPhone())
                .amount(invoice.getAmount())
                .description(invoice.getDescription())
                .remainingWalletBalance(remainingBalance)
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}