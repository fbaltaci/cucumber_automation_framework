package com.example.bookstore.stepdefs;

import com.example.bookstore.util.LoggerUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;

public class Hooks {
    private static boolean initialized = false;
    private static final Logger logger = LoggerUtil.getLogger(Hooks.class);

    @Before(order = 0)
    public void setupOnce() {
        if (!initialized) {
            logger.info("Logger initialized!");
            initialized = true;
        }
    }

    @After
    public void afterScenario() {
        logger.info("Scenario finished.");
    }
}
