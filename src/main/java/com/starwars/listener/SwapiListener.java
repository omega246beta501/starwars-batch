package com.starwars.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwapiListener {

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info(stepExecution.getSummary());
    }
}
