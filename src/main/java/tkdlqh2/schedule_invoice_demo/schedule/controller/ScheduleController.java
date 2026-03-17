package tkdlqh2.schedule_invoice_demo.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.corp.CorpService;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleService;
import tkdlqh2.schedule_invoice_demo.schedule.command.CreateInvoiceScheduleCommand;
import tkdlqh2.schedule_invoice_demo.schedule.dto.CreateInvoiceScheduleRequest;
import tkdlqh2.schedule_invoice_demo.schedule.dto.CreateInvoiceScheduleResponse;

import java.util.UUID;

/**
 * 청구서 스케줄 관리 Controller
 */
@Tag(name = "Schedule", description = "청구서 스케줄 관리 API")
@RestController
@RequestMapping("/app/corps/{corpId}/invoice-schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final CorpService corpService;
    private final InvoiceScheduleService scheduleService;

    /**
     * 청구서 스케줄 등록
     */
    @Operation(summary = "청구서 스케줄 등록", description = "예약 또는 정기 청구서 스케줄을 등록합니다.")
    @PostMapping
    public ResponseEntity<CreateInvoiceScheduleResponse> createInvoiceSchedule(
            @PathVariable UUID corpId,
            @RequestBody CreateInvoiceScheduleRequest request
    ) {
        // Corp 조회
        Corp corp = corpService.findCorpById(corpId);

        // Request -> Command 변환 (Factory Pattern)
        CreateInvoiceScheduleCommand command = CreateInvoiceScheduleCommand.from(corp, request);

        // 스케줄 등록
        CreateInvoiceScheduleResponse response = scheduleService.createInvoiceSchedule(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 스케줄 그룹 삭제
     */
    @Operation(summary = "스케줄 그룹 삭제", description = "청구서 스케줄 그룹을 삭제합니다.")
    @DeleteMapping("/{scheduleGroupId}")
    public ResponseEntity<Void> deleteScheduleGroup(
            @PathVariable UUID corpId,
            @PathVariable Long scheduleGroupId
    ) {
        // Corp 조회
        Corp corp = corpService.findCorpById(corpId);

        // 스케줄 그룹 삭제
        scheduleService.deleteScheduleGroup(scheduleGroupId, corp);

        return ResponseEntity.noContent().build();
    }
}