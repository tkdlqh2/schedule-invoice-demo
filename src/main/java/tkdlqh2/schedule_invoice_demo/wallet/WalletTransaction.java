package tkdlqh2.schedule_invoice_demo.wallet;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;

import java.util.UUID;

/**
 * 지갑 트랜잭션 (불변 원장)
 */
@Entity
@Table(name = "wallet_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletTransactionType type;

    /**
     * 트랜잭션 금액 (부호 포함)
     * FREE_CHARGE, INVOICE_REFUND: 양수
     * INVOICE_USE: 음수
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * 연관된 청구서 ID (nullable)
     */
    @Column(name = "invoice_id")
    private Long invoiceId;

    /**
     * 환불의 경우 원본 사용 트랜잭션 ID (nullable)
     */
    @Column(name = "related_transaction_id", columnDefinition = "uuid")
    private UUID relatedTransactionId;

    @Column(length = 200)
    private String reason;

    @Builder
    public WalletTransaction(
            Wallet wallet,
            WalletTransactionType type,
            Long amount,
            Long invoiceId,
            UUID relatedTransactionId,
            String reason
    ) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.invoiceId = invoiceId;
        this.relatedTransactionId = relatedTransactionId;
        this.reason = reason;
    }

    /**
     * 무료 충전 트랜잭션 생성
     */
    public static WalletTransaction createFreeCharge(Wallet wallet, Long amount, String reason) {
        if (wallet == null) {
            throw new IllegalArgumentException("지갑 정보는 필수입니다.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        return WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.FREE_CHARGE)
                .amount(amount)
                .reason(reason)
                .build();
    }

    /**
     * 청구서 사용 트랜잭션 생성
     */
    public static WalletTransaction createInvoiceUse(Wallet wallet, Long amount, Long invoiceId) {
        if (wallet == null) {
            throw new IllegalArgumentException("지갑 정보는 필수입니다.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        if (invoiceId == null) {
            throw new IllegalArgumentException("청구서 사용 시 invoiceId는 필수입니다.");
        }

        return WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.INVOICE_USE)
                .amount(-amount) // 음수로 저장
                .invoiceId(invoiceId)
                .reason("청구서 발송")
                .build();
    }

    /**
     * 청구서 환불 트랜잭션 생성
     */
    public static WalletTransaction createInvoiceRefund(
            Wallet wallet,
            Long amount,
            Long invoiceId,
            UUID relatedTransactionId
    ) {
        if (wallet == null) {
            throw new IllegalArgumentException("지갑 정보는 필수입니다.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0보다 커야 합니다.");
        }
        if (invoiceId == null) {
            throw new IllegalArgumentException("환불 시 invoiceId는 필수입니다.");
        }
        if (relatedTransactionId == null) {
            throw new IllegalArgumentException("환불 시 relatedTransactionId는 필수입니다.");
        }

        return WalletTransaction.builder()
                .wallet(wallet)
                .type(WalletTransactionType.INVOICE_REFUND)
                .amount(amount)
                .invoiceId(invoiceId)
                .relatedTransactionId(relatedTransactionId)
                .reason("청구서 발송 실패 환불")
                .build();
    }
}
