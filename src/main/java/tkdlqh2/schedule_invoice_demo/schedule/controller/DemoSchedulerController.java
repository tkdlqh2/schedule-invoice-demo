package tkdlqh2.schedule_invoice_demo.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedulerService;
import tkdlqh2.schedule_invoice_demo.schedule.dto.RunSchedulerRequest;
import tkdlqh2.schedule_invoice_demo.schedule.dto.RunSchedulerResponse;

import java.time.LocalDateTime;

/**
 * 데모용 스케줄러 Controller
 */
@Tag(name = "Demo Scheduler", description = "데모용 스케줄러 실행 API")
@RestController
@RequestMapping("/internal/demo")
@RequiredArgsConstructor
public class DemoSchedulerController {

    private final InvoiceSchedulerService schedulerService;

    /**
     * 스케줄러 실행 (데모용)
     */
    @Operation(
            summary = "스케줄러 실행 (데모용)",
            description = "지정된 시각(또는 현재 시각)까지 실행되어야 하는 모든 스케줄을 처리합니다."
    )
    @PostMapping("/run-scheduler")
    public ResponseEntity<RunSchedulerResponse> runScheduler(
            @RequestBody(required = false) RunSchedulerRequest request
    ) {
        // executeAt이 없으면 현재 시각 사용
        LocalDateTime executeAt = (request != null && request.executeAt() != null)
                ? request.executeAt()
                : LocalDateTime.now();

        // 스케줄러 실행
        RunSchedulerResponse response = schedulerService.runScheduler(executeAt);

        return ResponseEntity.ok(response);
    }
}