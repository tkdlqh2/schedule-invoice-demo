package tkdlqh2.schedule_invoice_demo.corp.dto;

public record CreateCorpRequest(
        String name,
        String businessNumber,
        String contactPhone
) {
}