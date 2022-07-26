package com.ismaelgf.awsmigrator.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AwsImportFactory {


    private final List<AwsImportService> awsImportServices;

    private Map<String, AwsImportService> serviceMap;

    @PostConstruct
    private void init() {
        serviceMap = new HashMap<>();
        awsImportServices.forEach(awsImportService -> serviceMap.put(awsImportService.getType().getServiceName(), awsImportService));
    }

    public AwsImportService getAwsImportService(String serviceName) {
        return serviceMap.get(serviceName);
    }


}
