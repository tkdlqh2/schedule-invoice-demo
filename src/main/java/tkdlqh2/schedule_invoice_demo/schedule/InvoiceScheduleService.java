package tkdlqh2.schedule_invoice_demo.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.schedule.command.CreateInvoiceScheduleCommand;
import tkdlqh2.schedule_invoice_demo.schedule.dto.CreateInvoiceScheduleResponse;

/**
 * Invoice Schedule 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceScheduleService {

    private final InvoiceScheduleGroupRepository scheduleGroupRepository;
    private final InvoiceScheduleRepository scheduleRepository;

    /**
     * 청구서 스케줄 등록
     * - InvoiceScheduleGroup 생성 (청구서 템플릿 정보 포함)
     * - InvoiceSchedule 생성 (첫 실행 스케줄)
     */
    @Transactional
    public CreateInvoiceScheduleResponse createInvoiceSchedule(CreateInvoiceScheduleCommand command) {
        // 1. InvoiceScheduleGroup 생성 (Factory Pattern)
        InvoiceScheduleGroup scheduleGroup = InvoiceScheduleGroup.from(command);
        scheduleGroup = scheduleGroupRepository.save(scheduleGroup);

        // 2. 첫 InvoiceSchedule 생성 (Factory Pattern)
        InvoiceSchedule firstSchedule = InvoiceSchedule.createFirst(scheduleGroup, command.scheduledAt());
        firstSchedule = scheduleRepository.save(firstSchedule);

        // 3. 응답 생성
        return CreateInvoiceScheduleResponse.from(scheduleGroup, firstSchedule);
    }

    /**
     * 스케줄 그룹 삭제
     * - 스케줄 그룹을 삭제하면 연관된 스케줄도 함께 삭제됨 (Cascade)
     */
    @Transactional
    public void deleteScheduleGroup(Long scheduleGroupId, Corp corp) {
        InvoiceScheduleGroup scheduleGroup = scheduleGroupRepository.findById(scheduleGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄 그룹입니다. ID: " + scheduleGroupId));

        // 권한 확인: 해당 기관의 스케줄인지 확인
        if (!scheduleGroup.getCorp().getId().equals(corp.getId())) {
            throw new IllegalArgumentException("다른 기관의 스케줄 그룹은 삭제할 수 없습니다.");
        }

        scheduleGroupRepository.delete(scheduleGroup);
    }

    /**
     * 기관의 스케줄 그룹 조회
     */
    public InvoiceScheduleGroup findScheduleGroupById(Long scheduleGroupId) {
        return scheduleGroupRepository.findById(scheduleGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄 그룹입니다. ID: " + scheduleGroupId));
    }
}