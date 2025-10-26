package com.nebulamind.agentbuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NebulaMind Agent Builder Application
 * 
 * LLM runtime that orchestrates trading decisions by:
 * - Calling Trading Core API for signals and orders
 * - Implementing guardrails and risk policies
 * - Logging all LLM decisions for audit
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableRetry
public class AgentBuilderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentBuilderApplication.class, args);
    }
}

