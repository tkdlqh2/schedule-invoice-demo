package tkdlqh2.schedule_invoice_demo.wallet.command;

import tkdlqh2.schedule_invoice_demo.corp.Corp;

public record ChargeFreeCreditCommand(
        Corp corp,
        Long amount,
        String reason
) {
}