package tkdlqh2.schedule_invoice_demo.corp;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;

@Entity
@Table(name = "corps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Corp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String businessNumber;

    @Column(length = 20)
    private String contactPhone;

    @Builder
    public Corp(String name, String businessNumber, String contactPhone) {
        this.name = name;
        this.businessNumber = businessNumber;
        this.contactPhone = contactPhone;
    }
}
