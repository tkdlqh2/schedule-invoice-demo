package tkdlqh2.schedule_invoice_demo.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.corp.CorpService;
import tkdlqh2.schedule_invoice_demo.wallet.WalletService;
import tkdlqh2.schedule_invoice_demo.wallet.dto.FreeChargeRequest;
import tkdlqh2.schedule_invoice_demo.wallet.dto.FreeChargeResponse;
import tkdlqh2.schedule_invoice_demo.wallet.dto.WalletResponse;

import java.util.UUID;

/**
 * Admin - 무료 포인트 지급 API
 */
@RestController
@RequestMapping("/admin/corps/{corpId}/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final CorpService corpService;
    private final WalletService walletService;

    /**
     * 무료 포인트 지급
     * POST /admin/corps/{corpId}/wallet/free-charge
     */
    @PostMapping("/free-charge")
    public ResponseEntity<FreeChargeResponse> chargeFreePoints(
            @PathVariable UUID corpId,
            @RequestBody FreeChargeRequest request
    ) {
        Corp corp = corpService.findCorpById(corpId);
        return ResponseEntity.ok(walletService.chargeFreePointsWithResponse(
                corp,
                request.amount(),
                request.reason()
        ));
    }

    /**
     * Wallet 조회
     * GET /admin/corps/{corpId}/wallet
     */
    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID corpId) {
        Corp corp = corpService.findCorpById(corpId);
        return ResponseEntity.ok(walletService.getWalletByCorpAsResponse(corp));
    }
}