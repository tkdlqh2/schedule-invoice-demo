package tkdlqh2.schedule_invoice_demo.corp.dto;

import tkdlqh2.schedule_invoice_demo.corp.Corp;

import java.util.UUID;

public record CorpResponse(
        UUID id,
        String name,
        String businessNumber,
        String contactPhone
) {
    public static CorpResponse from(Corp corp) {
        return new CorpResponse(
                corp.getId(),
                corp.getName(),
                corp.getBusinessNumber(),
                corp.getContactPhone()
        );
    }
}