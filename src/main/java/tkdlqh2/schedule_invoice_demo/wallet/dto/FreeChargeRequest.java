package tkdlqh2.schedule_invoice_demo.wallet.dto;

public record FreeChargeRequest(
        Long amount,
        String reason
) {
}