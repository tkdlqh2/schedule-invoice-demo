package tkdlqh2.schedule_invoice_demo.corp;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;
import tkdlqh2.schedule_invoice_demo.corp.command.CreateCorpCommand;

import java.util.UUID;

@Entity
@Table(name = "corps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Corp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String businessNumber;

    @Column(length = 20)
    private String contactPhone;

    public static Corp create(CreateCorpCommand command) {
        Corp corp = new Corp();
        corp.name = command.name();
        corp.businessNumber = command.businessNumber();
        corp.contactPhone = command.contactPhone();
        return corp;
    }
}
