package com.starwars.listener;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Component
public class StepListener {
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("before step");
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        System.out.println(stepExecution.getSummary());
        System.out.println("after step");
    }
}
