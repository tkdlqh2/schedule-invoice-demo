package tkdlqh2.schedule_invoice_demo.mock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tkdlqh2.schedule_invoice_demo.mock.MockNotificationRepository;
import tkdlqh2.schedule_invoice_demo.mock.dto.MockNotificationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Admin - Mock 발송 로그 조회 API
 */
@RestController
@RequestMapping("/admin/mock-notifications")
@RequiredArgsConstructor
public class AdminMockNotificationController {

    private final MockNotificationRepository mockNotificationRepository;

    /**
     * Mock 발송 로그 조회
     * GET /admin/mock-notifications?corpId={corpId}
     *
     * @param corpId (선택) 특정 기관의 발송 로그만 조회. 생략 시 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<MockNotificationResponse>> getMockNotifications(
            @RequestParam(required = false) UUID corpId
    ) {
        if (corpId != null) {
            // Corp ID로 필터링
            return ResponseEntity.ok(
                    mockNotificationRepository.findByCorpIdOrderBySentAtDesc(corpId)
                            .stream()
                            .map(MockNotificationResponse::from)
                            .toList()
            );
        } else {
            // 전체 조회
            return ResponseEntity.ok(
                    mockNotificationRepository.findAllOrderBySentAtDesc()
                            .stream()
                            .map(MockNotificationResponse::from)
                            .toList()
            );
        }
    }
}