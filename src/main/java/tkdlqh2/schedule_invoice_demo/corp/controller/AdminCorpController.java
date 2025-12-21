package tkdlqh2.schedule_invoice_demo.corp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tkdlqh2.schedule_invoice_demo.corp.CorpService;
import tkdlqh2.schedule_invoice_demo.corp.dto.CorpResponse;
import tkdlqh2.schedule_invoice_demo.corp.dto.CreateCorpRequest;

import java.util.List;
import java.util.UUID;

/**
 * Admin - 기관(Corp) 관리 API
 */
@RestController
@RequestMapping("/admin/corps")
@RequiredArgsConstructor
public class AdminCorpController {

    private final CorpService corpService;

    /**
     * 기관 생성
     */
    @PostMapping
    public ResponseEntity<CorpResponse> createCorp(@RequestBody CreateCorpRequest request) {
        return ResponseEntity.ok(corpService.createCorpWithResponse(request));
    }

    /**
     * 기관 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CorpResponse>> listCorps() {
        return ResponseEntity.ok(corpService.findAllCorpsAsResponse());
    }

    /**
     * 기관 상세 조회
     */
    @GetMapping("/{corpId}")
    public ResponseEntity<CorpResponse> getCorp(@PathVariable UUID corpId) {
        return ResponseEntity.ok(corpService.findCorpByIdAsResponse(corpId));
    }
}