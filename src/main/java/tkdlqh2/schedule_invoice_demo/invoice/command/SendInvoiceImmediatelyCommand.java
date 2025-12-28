package tkdlqh2.schedule_invoice_demo.invoice.command;

import tkdlqh2.schedule_invoice_demo.invoice.dto.SendInvoiceImmediatelyRequest;

import java.util.UUID;

/**
 * 즉시 발송 Invoice 생성 커맨드
 */
public record SendInvoiceImmediatelyCommand(
        UUID corpId,
        String studentName,
        String guardianPhone,
        Long amount,
        String description
) {
    public SendInvoiceImmediatelyCommand {
        if (corpId == null) {
            throw new IllegalArgumentException("corpId는 필수입니다.");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("studentName은 필수입니다.");
        }
        if (guardianPhone == null || guardianPhone.isBlank()) {
            throw new IllegalArgumentException("guardianPhone은 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("description은 200자 이하여야 합니다.");
        }
    }

    /**
     * Request로부터 Command 생성
     */
    public static SendInvoiceImmediatelyCommand from(UUID corpId, SendInvoiceImmediatelyRequest request) {
        return new SendInvoiceImmediatelyCommand(
                corpId,
                request.studentName(),
                request.guardianPhone(),
                request.amount(),
                request.description()
        );
    }
}