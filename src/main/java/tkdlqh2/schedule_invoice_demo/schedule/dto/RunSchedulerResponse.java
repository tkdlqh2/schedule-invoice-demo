package tkdlqh2.schedule_invoice_demo.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 스케줄러 실행 응답 (데모용)
 */
@Schema(description = "스케줄러 실행 응답 (데모용)")
public record RunSchedulerResponse(

        @Schema(description = "실행 기준 시각")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime executeAt,

        @Schema(description = "처리된 스케줄 수")
        int processedCount,

        @Schema(description = "성공한 스케줄 수")
        int succeededCount,

        @Schema(description = "실패한 스케줄 수")
        int failedCount
) {
}