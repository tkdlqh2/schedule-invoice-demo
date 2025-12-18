package tkdlqh2.schedule_invoice_demo.corp.command;

public record CreateCorpCommand(
        String name,
        String businessNumber,
        String contactPhone
) {
}