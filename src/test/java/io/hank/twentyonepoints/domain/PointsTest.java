package io.hank.twentyonepoints.domain;

import static io.hank.twentyonepoints.domain.PointsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.hank.twentyonepoints.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PointsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Points.class);
        Points points1 = getPointsSample1();
        Points points2 = new Points();
        assertThat(points1).isNotEqualTo(points2);

        points2.setId(points1.getId());
        assertThat(points1).isEqualTo(points2);

        points2 = getPointsSample2();
        assertThat(points1).isNotEqualTo(points2);
    }
}
