package it.fabioformosa.quartzmanager.configuration;

import it.fabioformosa.quartzmanager.common.properties.QuartzModuleProperties;
import it.fabioformosa.quartzmanager.scheduler.AutowiringSpringBeanJobFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.io.IOException;
import java.util.Properties;

@ComponentScan(basePackages = {"it.fabioformosa.quartzmanager.controllers"})
@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class SchedulerConfig {

    private static final int DEFAULT_MISFIRE_INSTRUCTION = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT;

    private static JobDetailFactoryBean createJobDetail(Class<? extends Job> jobClass) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(false);
        return factoryBean;
    }

    private static SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, long pollFrequencyMs,
            int repeatCount) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setStartDelay(3000L);
        factoryBean.setRepeatInterval(pollFrequencyMs);
        factoryBean.setRepeatCount(repeatCount);
        factoryBean
        .setMisfireInstruction(DEFAULT_MISFIRE_INSTRUCTION);// in case of misfire, ignore all missed triggers and continue
        return factoryBean;
    }

    @Value("${quartz-manager.jobClass}")
    private String jobClassname;

    @Autowired(required = false)
    private QuartzModuleProperties quartzModuleProperties;


    // REMOVEME
//    @Bean(name = "triggerMonitor")
//    public TriggerMonitor createTriggerMonitor(@Qualifier("jobTrigger") Trigger trigger) {
//        TriggerMonitor triggerMonitor = new TriggerMonitorImpl();
//        triggerMonitor.setTrigger(trigger);
//        return triggerMonitor;
//    }

    @Bean
    @SuppressWarnings("unchecked")
    public JobDetailFactoryBean jobDetail() throws ClassNotFoundException {
        Class<? extends Job> JobClass = (Class<? extends Job>) Class.forName(jobClassname);
        return createJobDetail(JobClass);
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

//    @Bean(name = "jobTrigger")
//    public SimpleTriggerFactoryBean sampleJobTrigger(@Qualifier("jobDetail") JobDetail jobDetail,
//            @Value("${job.frequency}") long frequency, @Value("${job.repeatCount}") int repeatCount) {
//        return createTrigger(jobDetail, frequency, repeatCount);
//    }

    @Bean(name = "scheduler")
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) throws IOException {
//      public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory,
//            @Qualifier("jobTrigger") Trigger sampleJobTrigger) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        Properties mergedProperties = new Properties();
        mergedProperties.putAll(quartzProperties());
        if(quartzModuleProperties != null)
            mergedProperties.putAll(quartzModuleProperties.getProperties());
        factory.setQuartzProperties(mergedProperties);
        //factory.setTriggers(sampleJobTrigger);
        factory.setAutoStartup(false);
        return factory;
    }
}
