package tkdlqh2.schedule_invoice_demo.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleGroup;
import tkdlqh2.schedule_invoice_demo.schedule.IntervalUnit;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 청구서 스케줄 등록 응답
 */
@Schema(description = "청구서 스케줄 등록 응답")
public record CreateInvoiceScheduleResponse(

        @Schema(description = "스케줄 그룹 ID")
        Long scheduleGroupId,

        @Schema(description = "기관 ID")
        UUID corpId,

        @Schema(description = "스케줄 타입")
        ScheduleType scheduleType,

        @Schema(description = "반복 주기 단위")
        IntervalUnit intervalUnit,

        @Schema(description = "반복 주기 값")
        Integer intervalValue,

        @Schema(description = "첫 스케줄 ID")
        Long firstScheduleId,

        @Schema(description = "첫 실행 예정 시각")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime firstScheduledAt,

        @Schema(description = "학생 이름")
        String studentName,

        @Schema(description = "보호자 전화번호")
        String guardianPhone,

        @Schema(description = "청구 금액")
        Long amount,

        @Schema(description = "청구서 설명")
        String description
) {

    public static CreateInvoiceScheduleResponse from(InvoiceScheduleGroup group, InvoiceSchedule firstSchedule) {
        return new CreateInvoiceScheduleResponse(
                group.getId(),
                group.getCorp().getId(),
                group.getScheduleType(),
                group.getIntervalUnit(),
                group.getIntervalValue(),
                firstSchedule.getId(),
                firstSchedule.getScheduledAt(),
                group.getStudentName(),
                group.getGuardianPhone(),
                group.getAmount(),
                group.getDescription()
        );
    }
}