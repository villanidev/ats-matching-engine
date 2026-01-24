package com.villanidev.atsmatchingengine.elt;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
public class EltAsyncConfig {

    @Bean(name = "eltTaskExecutor")
    public Executor eltTaskExecutor() {
        return new VirtualThreadTaskExecutor("elt-");
    }
}
