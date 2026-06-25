package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.AiFeature;
import ch.zhaw.praesto.model.AiUsage;
import ch.zhaw.praesto.repository.AiUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiQuotaServiceTest {

    @Mock
    private AiUsageRepository aiUsageRepository;

    @InjectMocks
    private AiQuotaService aiQuotaService;

    @BeforeEach
    void setLimits() {
        ReflectionTestUtils.setField(aiQuotaService, "limitPracticeInterview", 3);
        ReflectionTestUtils.setField(aiQuotaService, "limitCv", 1);
        ReflectionTestUtils.setField(aiQuotaService, "limitCoverLetter", 3);
    }

    @Test
    void status_withoutUsage_returnsFullRemaining() {
        when(aiUsageRepository.findByUserIdAndFeature("u1", AiFeature.CV)).thenReturn(Optional.empty());
        var status = aiQuotaService.status("u1", AiFeature.CV);
        assertThat(status.limit()).isEqualTo(1);
        assertThat(status.used()).isZero();
        assertThat(status.remaining()).isEqualTo(1);
    }

    @Test
    void consume_underLimit_incrementsAndSaves() {
        when(aiUsageRepository.findByUserIdAndFeature("u1", AiFeature.PRACTICE_INTERVIEW))
                .thenReturn(Optional.of(AiUsage.builder()
                        .userId("u1").feature(AiFeature.PRACTICE_INTERVIEW).usedTotal(1).build()));

        aiQuotaService.consume("u1", "s1", AiFeature.PRACTICE_INTERVIEW);

        verify(aiUsageRepository).save(argThat(u -> u.getUsedTotal() == 2));
    }

    @Test
    void consume_atLimit_throws() {
        when(aiUsageRepository.findByUserIdAndFeature("u1", AiFeature.CV))
                .thenReturn(Optional.of(AiUsage.builder()
                        .userId("u1").feature(AiFeature.CV).usedTotal(1).build()));

        assertThatThrownBy(() -> aiQuotaService.consume("u1", "s1", AiFeature.CV))
                .isInstanceOf(BadRequestException.class);
        verify(aiUsageRepository, never()).save(any());
    }
}
