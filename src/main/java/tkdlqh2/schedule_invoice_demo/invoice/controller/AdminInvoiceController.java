package tkdlqh2.schedule_invoice_demo.invoice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceService;
import tkdlqh2.schedule_invoice_demo.invoice.command.SendInvoiceImmediatelyCommand;
import tkdlqh2.schedule_invoice_demo.invoice.dto.SendInvoiceImmediatelyRequest;
import tkdlqh2.schedule_invoice_demo.invoice.dto.SendInvoiceImmediatelyResponse;

import java.util.UUID;

/**
 * Admin - Invoice 즉시 발송 API
 */
@RestController
@RequestMapping("/admin/corps/{corpId}/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Invoice 즉시 발송
     * POST /admin/corps/{corpId}/invoices/send-now
     * <p>
     * 요청 즉시 Invoice를 생성하고 발송합니다.
     * - Wallet에서 금액을 차감합니다.
     * - 발송 성공 시 Invoice 상태는 SENT로 변경됩니다.
     *
     * @param corpId  기관 ID
     * @param request 발송 요청 (학생명, 보호자 전화번호, 금액, 설명)
     * @return 발송된 Invoice 정보 및 남은 Wallet 잔액
     */
    @PostMapping("/send-now")
    public ResponseEntity<SendInvoiceImmediatelyResponse> sendInvoiceImmediately(
            @PathVariable UUID corpId,
            @Valid @RequestBody SendInvoiceImmediatelyRequest request
    ) {
        SendInvoiceImmediatelyCommand command = SendInvoiceImmediatelyCommand.from(corpId, request);
        SendInvoiceImmediatelyResponse response = invoiceService.sendInvoiceImmediately(command);
        return ResponseEntity.ok(response);
    }
}