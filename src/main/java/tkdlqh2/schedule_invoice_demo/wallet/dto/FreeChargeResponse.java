package tkdlqh2.schedule_invoice_demo.wallet.dto;

import java.util.UUID;

public record FreeChargeResponse(
        Long walletId,
        UUID corpId,
        String corpName,
        Long chargedAmount,
        Long currentBalance,
        String reason
) {
}