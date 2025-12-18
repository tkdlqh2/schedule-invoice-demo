package tkdlqh2.schedule_invoice_demo.corp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.corp.command.CreateCorpCommand;
import tkdlqh2.schedule_invoice_demo.corp.dto.CorpResponse;
import tkdlqh2.schedule_invoice_demo.corp.dto.CreateCorpRequest;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorpService {

    private final CorpRepository corpRepository;

    /**
     * 기관 생성
     */
    @Transactional
    public Corp createCorp(CreateCorpCommand command) {
        // 사업자 번호 중복 체크
        if (command.businessNumber() != null) {
            corpRepository.findByBusinessNumber(command.businessNumber())
                    .ifPresent(c -> {
                        throw new IllegalArgumentException("이미 존재하는 사업자 번호입니다: " + command.businessNumber());
                    });
        }

        Corp corp = Corp.create(command);
        return corpRepository.save(corp);
    }

    /**
     * 기관 목록 조회
     */
    public List<Corp> findAllCorps() {
        return corpRepository.findAll();
    }

    /**
     * 기관 ID로 조회
     */
    public Corp findCorpById(UUID corpId) {
        return corpRepository.findById(corpId)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다: " + corpId));
    }

    /**
     * 기관 존재 여부 확인
     */
    public boolean existsById(UUID corpId) {
        return corpRepository.existsById(corpId);
    }

    /**
     * 기관 생성 (DTO 반환)
     */
    @Transactional
    public CorpResponse createCorpWithResponse(CreateCorpRequest request) {
        CreateCorpCommand command = new CreateCorpCommand(
                request.name(),
                request.businessNumber(),
                request.contactPhone()
        );
        Corp corp = createCorp(command);
        return CorpResponse.from(corp);
    }

    /**
     * 기관 목록 조회 (DTO 반환)
     */
    public List<CorpResponse> findAllCorpsAsResponse() {
        return findAllCorps().stream()
                .map(CorpResponse::from)
                .toList();
    }

    /**
     * 기관 상세 조회 (DTO 반환)
     */
    public CorpResponse findCorpByIdAsResponse(UUID corpId) {
        Corp corp = findCorpById(corpId);
        return CorpResponse.from(corp);
    }
}