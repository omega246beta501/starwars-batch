package com.starwars.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("before job");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("after job");
    }
}
