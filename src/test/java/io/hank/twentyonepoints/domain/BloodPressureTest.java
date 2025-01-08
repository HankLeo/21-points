package io.hank.twentyonepoints.domain;

import static io.hank.twentyonepoints.domain.BloodPressureTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.hank.twentyonepoints.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BloodPressureTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BloodPressure.class);
        BloodPressure bloodPressure1 = getBloodPressureSample1();
        BloodPressure bloodPressure2 = new BloodPressure();
        assertThat(bloodPressure1).isNotEqualTo(bloodPressure2);

        bloodPressure2.setId(bloodPressure1.getId());
        assertThat(bloodPressure1).isEqualTo(bloodPressure2);

        bloodPressure2 = getBloodPressureSample2();
        assertThat(bloodPressure1).isNotEqualTo(bloodPressure2);
    }
}
