package com.synkrato.services.partner.business.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.BillingRuleChangeLogDTO;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import com.synkrato.services.partner.data.jpa.BillingRuleChangeLogRepository;
import com.synkrato.services.partner.util.TestHelper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BillingRuleChangeLogServiceTest {

  @InjectMocks BillingRuleChangeLogServiceImpl billingRuleChangeLogService;
  @Mock BillingRuleChangeLogRepository billingRuleChangeLogRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  /** This is a positive test case to create a new change log */
  @Test
  public void createChangeLogTest() {

    // Arrange
    BillingRuleChangeLog billingRuleChangeLog = TestHelper.buildBillingRuleChangeLog();

    // Mock
    when(billingRuleChangeLogRepository.save(Mockito.any(BillingRuleChangeLog.class)))
        .thenReturn(billingRuleChangeLog);

    // Act
    BillingRuleChangeLog savedLog =
        billingRuleChangeLogService.createChangeLog(billingRuleChangeLog);

    // Assert
    assertEquals(billingRuleChangeLog.getBillingRuleId(), savedLog.getBillingRuleId());
  }

  /** This is a positive test case to get list of change logs */
  @Test
  public void getChangeLogTest() {
    BillingRuleChangeLog changeLog = TestHelper.buildBillingRuleChangeLog();
    List<BillingRuleChangeLog> changeLogList = new ArrayList<>();
    changeLogList.add(changeLog);

    BillingRuleChangeLogDTO changeLogDTO =
        BillingRuleChangeLogDTO.builder()
            .billingRuleId(changeLog.getBillingRuleId().toString())
            .build();
    List<BillingRuleChangeLogDTO> changeLogDTOList = new ArrayList<>();
    changeLogDTOList.add(changeLogDTO);

    when(billingRuleChangeLogRepository.findByBillingRuleId(changeLog.getBillingRuleId()))
        .thenReturn(changeLogList);

    List<BillingRuleChangeLogDTO> changeLogDTOLst =
        billingRuleChangeLogService.getChangeLog(changeLog.getBillingRuleId().toString());

    assertNotNull(changeLogDTOLst);
    assertNotNull(changeLogDTOLst.get(0));
    assertEquals(
        changeLogDTOLst.get(0).getBillingRuleId(), changeLog.getBillingRuleId().toString());
  }
}
