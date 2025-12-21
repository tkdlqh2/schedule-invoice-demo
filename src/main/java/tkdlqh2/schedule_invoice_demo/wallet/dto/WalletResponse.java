package tkdlqh2.schedule_invoice_demo.wallet.dto;

import java.util.UUID;

public record WalletResponse(
        Long walletId,
        UUID corpId,
        String corpName,
        Long balance
) {
}