/*
 * The MIT License
 *
 * Copyright 2023 Kai Denzel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.kswmd.whatsapptool.quartz;

import de.kswmd.whatsapptool.WhatsAppClient;
import de.kswmd.whatsapptool.contacts.Entity;
import de.kswmd.whatsapptool.contacts.Message;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Kai Denzel
 */
public class ScheduleManager {

    private Logger LOGGER = LogManager.getLogger();

    private Scheduler scheduler = null;
    private static ScheduleManager instance;

    private ScheduleManager() {
    }

    public static ScheduleManager getInstance() {
        if (instance == null) {
            instance = new ScheduleManager();
        }
        return instance;
    }

    public boolean start() {
        try {
            if (scheduler == null || !scheduler.isStarted()) {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                return true;
            }
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't initialize Scheduler...", ex);
        }
        return false;
    }

    public boolean stop() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                //shutdown() does not return until executing Jobs complete execution
                scheduler.shutdown(true);
                return true;
            }
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't shutdown Scheduler...", ex);
        }
        return false;
    }

    public void scheduleMaintenanceJob(WhatsAppClient whatsAppClient) {
        JobDetail job = newJob(MaintenanceJob.class)
                .withIdentity("statusReportJob", "maintenance")
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("statusReportTrigger", "maintenance")
                .withSchedule(cronSchedule(" 0 30 3 * * ?"))
                //.withSchedule(cronSchedule(" 0/1 * * * * ?"))
                .forJob(job)
                .build();
        job.getJobDataMap().put(MaintenanceJob.WHATSAPP_CLIENT, whatsAppClient);
        try {
            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't start Job", ex);
        }
        LOGGER.info("Next maintenance time = " + SimpleDateFormat.getDateTimeInstance().format(trigger.getNextFireTime()));
    }

    public void scheduleMessagesJob(List<Entity> entities) {
        JobDetail handleCronMessagesJob = newJob(HandleCronMessageJob.class)
                .withIdentity("messagesJob", "contactPersons")
                .build();
        try {
            Set<Trigger> triggers = new HashSet<>();
            for (Entity e : entities) {
                for (Message m : e.getMessages()) {
                    JobDataMap jdm = new JobDataMap();
                    jdm.put(HandleCronMessageJob.KEY_MESSAGE, m);
                    Trigger trigger = newTrigger()
                            .withIdentity("messagesTrigger_" + e.getIdentifier() + "_" + e.getMessages().indexOf(m), "messagesTrigger")
                            .withSchedule(cronSchedule(m.getCronExpression()))
                            .forJob(handleCronMessagesJob)
                            .usingJobData(jdm)
                            .build();
                    triggers.add(trigger);
                }
            }
            scheduler.scheduleJob(handleCronMessagesJob, triggers, true);
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't schedule Job.", ex);
            unscheduleMessagesJob();
        }
    }

    public boolean unscheduleMessagesJob() {
        try {
            return scheduler.deleteJob(JobKey.jobKey("messagesJob", "contactPersons"));
        }
        catch (SchedulerException ex) {
            LOGGER.debug("Couldn't unschedule messages job.", ex);
        }
        return false;
    }

    public void pauseAllJobs() {
        try {
            scheduler.pauseJob(JobKey.jobKey("statusReportJob", "maintenance"));
            scheduler.pauseJob(JobKey.jobKey("messagesJob", "contactPersons"));
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't unschedule Job", ex);
        }
    }

    public void resumeAllJobs() {
        try {
            scheduler.resumeJob(JobKey.jobKey("statusReportJob", "maintenance"));
            scheduler.resumeJob(JobKey.jobKey("messagesJob", "contactPersons"));
        }
        catch (SchedulerException ex) {
            LOGGER.error("Couldn't unschedule Job", ex);
        }
    }
}
