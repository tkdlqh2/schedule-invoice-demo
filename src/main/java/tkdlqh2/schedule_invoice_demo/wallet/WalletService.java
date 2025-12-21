package tkdlqh2.schedule_invoice_demo.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.wallet.command.ChargeFreeCreditCommand;
import tkdlqh2.schedule_invoice_demo.wallet.command.CreateWalletCommand;
import tkdlqh2.schedule_invoice_demo.wallet.dto.FreeChargeResponse;
import tkdlqh2.schedule_invoice_demo.wallet.dto.WalletResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    /**
     * 무료 포인트 지급
     * - Wallet이 없으면 생성
     * - Wallet 잔액 증가
     * - WalletTransaction 생성 (FREE_CHARGE)
     */
    @Transactional
    public void chargeFreePoints(ChargeFreeCreditCommand command) {
        // Wallet 조회 또는 생성
        Wallet wallet;
        try {
            wallet = walletRepository.findByCorpIdWithLock (command.corp().getId())
                    .orElseGet(() -> {
                        CreateWalletCommand createCommand = new CreateWalletCommand(command.corp());
                        Wallet newWallet = Wallet.create(createCommand);
                        return walletRepository.save(newWallet);
                    });
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 동시 생성 시도로 인한 충돌 - 다시 조회
            wallet = walletRepository.findByCorpIdWithLock(command.corp().getId())
                    .orElseThrow(() -> new IllegalStateException("Wallet 생성 실패", e));
        }

        // 잔액 증가
        wallet.increaseBalance(command.amount());

        // 트랜잭션 기록 생성
        WalletTransaction transaction = WalletTransaction.createFreeCharge(
                wallet,
                command.amount(),
                command.reason()
        );
        walletTransactionRepository.save(transaction);
    }

    /**
     * Corp의 Wallet 조회
     */
    public Wallet getWalletByCorp(Corp corp) {
        return walletRepository.findByCorp(corp)
                .orElseThrow(() -> new IllegalArgumentException("Wallet이 존재하지 않습니다."));
    }

    /**
     * 무료 포인트 지급 (DTO 반환)
     */
    @Transactional
    public FreeChargeResponse chargeFreePointsWithResponse(Corp corp, Long amount, String reason) {
        // Request -> Command 변환
        ChargeFreeCreditCommand command = new ChargeFreeCreditCommand(corp, amount, reason);

        // 무료 포인트 지급
        chargeFreePoints(command);

        // Wallet 조회하여 결과 반환
        Wallet wallet = getWalletByCorp(corp);

        return new FreeChargeResponse(
                wallet.getId(),
                corp.getId(),
                corp.getName(),
                amount,
                wallet.getBalance(),
                reason
        );
    }

    /**
     * Wallet 조회 (DTO 반환)
     */
    public WalletResponse getWalletByCorpAsResponse(Corp corp) {
        Wallet wallet = getWalletByCorp(corp);

        return new WalletResponse(
                wallet.getId(),
                corp.getId(),
                corp.getName(),
                wallet.getBalance()
        );
    }
}