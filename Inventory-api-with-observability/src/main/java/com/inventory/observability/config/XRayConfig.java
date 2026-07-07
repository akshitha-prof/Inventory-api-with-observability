package com.inventory.observability.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ElasticBeanstalkPlugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AWS X-Ray configuration for distributed tracing.
 * Traces requests end-to-end: Controller → Service → Repository → Database.
 *
 * Only active in 'aws' profile to avoid issues during local development.
 */
@Configuration
@Profile("aws")
public class XRayConfig {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new EC2Plugin())
                .withPlugin(new ElasticBeanstalkPlugin())
                .withDefaultPlugins();

        // Use localized sampling to reduce costs in production
        builder.withSamplingStrategy(new LocalizedSamplingStrategy());

        AWSXRay.setGlobalRecorder(builder.build());
    }

    /**
     * Servlet filter that creates X-Ray segments for every incoming HTTP request.
     * Captures: URL, method, response code, latency.
     */
    @Bean
    public Filter tracingFilter() {
        return new AWSXRayServletFilter("InventoryLendingAPI");
    }
}
