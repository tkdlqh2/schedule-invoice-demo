package tkdlqh2.schedule_invoice_demo.invoice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 즉시 발송 Invoice 생성 요청
 */
public record SendInvoiceImmediatelyRequest(
        @NotBlank(message = "학생 이름은 필수입니다.")
        @Size(max = 50, message = "학생 이름은 50자 이하여야 합니다.")
        String studentName,

        @NotBlank(message = "보호자 전화번호는 필수입니다.")
        @Size(max = 20, message = "보호자 전화번호는 20자 이하여야 합니다.")
        String guardianPhone,

        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1, message = "금액은 0보다 커야 합니다.")
        Long amount,

        @Size(max = 200, message = "설명은 200자 이하여야 합니다.")
        String description
) {
}