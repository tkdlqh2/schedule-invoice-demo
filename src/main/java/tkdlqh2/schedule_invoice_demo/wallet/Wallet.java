package tkdlqh2.schedule_invoice_demo.wallet;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.wallet.command.CreateWalletCommand;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_id", nullable = false, unique = true)
    private Corp corp;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(nullable = false)
    private Long balance = 0L;

    public static Wallet create(CreateWalletCommand command) {
        Wallet wallet = new Wallet();
        wallet.corp = command.corp();
        wallet.balance = 0L;
        return wallet;
    }

    /**
     * 잔액 증가
     */
    public void increaseBalance(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가 금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /**
     * 잔액 차감
     */
    public void decreaseBalance(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다. 현재 잔액: " + this.balance + ", 차감 요청: " + amount);
        }
        this.balance -= amount;
    }

    /**
     * 잔액 확인
     */
    public boolean hasEnoughBalance(Long amount) {
        return this.balance >= amount;
    }
}
