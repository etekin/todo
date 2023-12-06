package com.task.todo.service;

import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import com.task.todo.model.ItemStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
@Slf4j
public class DueDateSchedulerService {

    private final static int FIVE_SECONDS_IN_MILLISECOND = 5 * 1000;
    private final Scheduler scheduler;
    private final ItemService itemService;

    public DueDateSchedulerService(Scheduler scheduler, @Lazy ItemService itemService) {
        this.scheduler = scheduler;
        this.itemService = itemService;
    }

    @PostConstruct
    public void postConstruct() {
        try {
            scheduler.start();
        } catch (SchedulerException schedulerException) {
            log.error("Error on scheduling check-due-date-job", schedulerException);
        }
    }

    public void addOrUpdateSchedulerForItem(ItemEntity itemEntity) {
        try {
            scheduler.deleteJob(JobKey.jobKey(itemEntity.getId().toString()));
            JobDetail job = newJob(UpdateDueDateCronJob.class)
                    .withIdentity(itemEntity.getId().toString())
                    .build();
            Trigger trigger = newTrigger()
                    .startAt(Date.from(itemEntity.getDueDate()
                            .plus(1, SECONDS)
                            .atZone(TimeZone.getDefault().toZoneId()).toInstant()))
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(1)
                            .withRepeatCount(0)
                            .withMisfireHandlingInstructionFireNow()
                    )
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException schedulerException) {
            log.error("Error on scheduling new task for item with id:" + itemEntity.getId(), schedulerException);
        }
    }

    @Scheduled(fixedRateString = "${todo.duedate.cron.intervalInMillisecond}", initialDelay = FIVE_SECONDS_IN_MILLISECOND)
    public void checkDueDateCronJob() {
        log.info("checkDueDateCronJob started");
        List<UUID> itemIds = itemService.updateDueDates();
        List<JobKey> jobKeys = itemIds.stream().map(i -> JobKey.jobKey(i.toString())).toList();
        try {
            scheduler.deleteJobs(jobKeys);
        } catch (SchedulerException schedulerException) {
            log.error("Error on deleting with ids:" + itemIds, schedulerException);
        }
        log.info("checkDueDateCronJob ended. {} item(s) processed", itemIds.size());
    }


    class UpdateDueDateCronJob implements Job {
        public void execute(JobExecutionContext context) {
            UUID itemId = null;
            try {
                log.info("UpdateDueDateCronJob started");
                JobKey jobKey = context.getJobDetail().getKey();
                itemId = UUID.fromString(jobKey.getName());
                ItemDto itemDto = itemService.findById(itemId);
                if (itemDto.getStatus() == ItemStatus.UNDONE && itemDto.getDueDate().isBefore(LocalDateTime.now())) {
                    itemDto.setStatus(ItemStatus.DUE_DATE_PASSED);
                    itemService.updatePartialItem(itemId, itemDto);
                }
                scheduler.deleteJob(jobKey);
                log.info("UpdateDueDateCronJob ended with key: {}", itemId);
            } catch (SchedulerException schedulerException) {
                log.error("Error on deleting with id:" + itemId, schedulerException);
            }
        }
    }


}
