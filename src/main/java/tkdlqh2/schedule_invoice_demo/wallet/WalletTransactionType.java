package tkdlqh2.schedule_invoice_demo.wallet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지갑 트랜잭션 타입
 */
@Getter
@RequiredArgsConstructor
public enum WalletTransactionType {

    FREE_CHARGE("무료 충전", 1),
    INVOICE_USE("청구서 발송 차감", -1),
    INVOICE_REFUND("청구서 환불", 1);

    private final String description;
    private final int sign; // +1: 증가, -1: 감소

    public boolean isIncrease() {
        return sign > 0;
    }

    public boolean isDecrease() {
        return sign < 0;
    }
}
