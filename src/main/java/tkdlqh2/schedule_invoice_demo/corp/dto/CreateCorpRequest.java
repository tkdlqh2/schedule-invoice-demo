package tkdlqh2.schedule_invoice_demo.corp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCorpRequest(
        @NotBlank(message = "기관명은 필수입니다")
        String name,
        @NotBlank(message = "사업자번호는 필수입니다")
        @Pattern(regexp = "\\d{3}-\\d{2}-\\d{5}", message = "사업자번호 형식이 올바르지 않습니다")
        String businessNumber,
        @NotBlank(message = "연락처는 필수입니다")
        String contactPhone
) {
}