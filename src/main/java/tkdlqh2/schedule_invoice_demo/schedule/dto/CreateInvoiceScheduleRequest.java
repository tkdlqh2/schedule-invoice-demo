package tkdlqh2.schedule_invoice_demo.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import tkdlqh2.schedule_invoice_demo.schedule.IntervalUnit;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleType;

import java.time.LocalDateTime;

/**
 * 청구서 스케줄 등록 요청
 */
@Schema(description = "청구서 스케줄 등록 요청")
public record CreateInvoiceScheduleRequest(

        @Schema(description = "스케줄 타입 (ONCE: 1회, RECURRING: 반복)", example = "RECURRING")
        ScheduleType scheduleType,

        @Schema(description = "첫 실행 시각", example = "2025-12-10T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime scheduledAt,

        @Schema(description = "반복 주기 단위 (RECURRING인 경우 필수)", example = "MONTH")
        IntervalUnit intervalUnit,

        @Schema(description = "반복 주기 값 (RECURRING인 경우 필수, 예: 1=매 단위마다)", example = "1")
        Integer intervalValue,

        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,

        @Schema(description = "보호자 전화번호", example = "010-1111-2222")
        String guardianPhone,

        @Schema(description = "청구 금액", example = "50000")
        Long amount,

        @Schema(description = "청구서 설명", example = "매월 수업료")
        String description
) {
}