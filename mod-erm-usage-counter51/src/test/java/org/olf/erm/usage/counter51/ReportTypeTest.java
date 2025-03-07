package org.olf.erm.usage.counter51;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReportTypeTest {

  @Test
  void testIsMasterReport() {
    assertThat(ReportType.DR.isMasterReport()).isTrue();
    assertThat(ReportType.IR.isMasterReport()).isTrue();
    assertThat(ReportType.PR.isMasterReport()).isTrue();
    assertThat(ReportType.TR.isMasterReport()).isTrue();

    assertThat(ReportType.DR_D1.isMasterReport()).isFalse();
    assertThat(ReportType.IR_A1.isMasterReport()).isFalse();
    assertThat(ReportType.PR_P1.isMasterReport()).isFalse();
    assertThat(ReportType.TR_B1.isMasterReport()).isFalse();
  }
}
