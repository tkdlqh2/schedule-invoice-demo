package tkdlqh2.schedule_invoice_demo.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 스케줄러 실행 요청 (데모용)
 */
@Schema(description = "스케줄러 실행 요청 (데모용)")
public record RunSchedulerRequest(

        @Schema(description = "실행 기준 시각 (생략 시 현재 시각 사용)", example = "2025-12-10T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime executeAt
) {
}