package tkdlqh2.schedule_invoice_demo.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FreeChargeRequest(
        @NotNull(message = "충전 금액은 필수입니다")
        @Positive(message = "충전 금액은 양수여야 합니다")
        Long amount,
        @NotBlank(message = "충전 사유는 필수입니다")
        String reason
) {
}