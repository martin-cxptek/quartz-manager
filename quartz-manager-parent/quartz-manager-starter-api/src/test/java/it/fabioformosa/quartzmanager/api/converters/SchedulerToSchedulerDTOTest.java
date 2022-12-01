package it.fabioformosa.quartzmanager.api.converters;

import it.fabioformosa.quartzmanager.api.dto.SchedulerDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.annotation.DirtiesContext;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SchedulerToSchedulerDTOTest {

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private ConversionService conversionService;

  @Order(1)
  @Test
  void givenAnActiveScheduler_whenItIsConverted_thenADtoIsReturned () throws SchedulerException {
    Assertions.assertThat(scheduler.isShutdown()).isFalse();
    SchedulerDTO schedulerDTO = conversionService.convert(scheduler, SchedulerDTO.class);
    Assertions.assertThat(schedulerDTO).isNotNull();
    Assertions.assertThat(schedulerDTO.getName()).isEqualTo(scheduler.getSchedulerName());
    Assertions.assertThat(schedulerDTO.getInstanceId()).isEqualTo(scheduler.getSchedulerInstanceId());
  }

  @DirtiesContext
  @Order(2)
  @Test
  void givenASchedulerInShutdown_whenItIsConverted_thenADtoIsReturned () throws SchedulerException {
    Assertions.assertThat(scheduler.isShutdown()).isFalse();
    scheduler.shutdown(false);
    Assertions.assertThat(scheduler.isShutdown()).isTrue();

    SchedulerDTO schedulerDTO = conversionService.convert(scheduler, SchedulerDTO.class);
    Assertions.assertThat(schedulerDTO).isNotNull();
    Assertions.assertThat(schedulerDTO.getName()).isEqualTo(scheduler.getSchedulerName());
    Assertions.assertThat(schedulerDTO.getInstanceId()).isEqualTo(scheduler.getSchedulerInstanceId());
    Assertions.assertThat(schedulerDTO.getTriggerKeys()).isNull();
  }


}
