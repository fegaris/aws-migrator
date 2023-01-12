package com.ismaelgf.awsmigrator.runner;

import static com.ismaelgf.awsmigrator.constant.Constants.SERVICE_NAME;

import com.ismaelgf.awsmigrator.exception.MandatoryParameterNotFound;
import com.ismaelgf.awsmigrator.service.AwsImportFactory;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
@Order(value = 1)
public class AwsAplicationRunner implements ApplicationRunner {

    private final AwsImportFactory awsImportFactory;

    @Override
    public void run(ApplicationArguments args) {
        if (Objects.nonNull(args.getOptionValues(SERVICE_NAME))) {
            args.getOptionValues(SERVICE_NAME).forEach(serviceName -> {
                log.info(String.format("Migrating %s", serviceName));
              awsImportFactory.getAwsImportService(serviceName.toLowerCase()).importService(args);
            });
        } else {
            throw new MandatoryParameterNotFound(String.format("%s is a mandatory parameter", SERVICE_NAME));
        }
    }
}
