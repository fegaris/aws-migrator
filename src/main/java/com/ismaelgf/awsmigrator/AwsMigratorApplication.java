package com.ismaelgf.awsmigrator;

import com.ismaelgf.awsmigrator.job.EventBridgeImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AwsMigratorApplication implements CommandLineRunner {

	@Autowired
	private EventBridgeImporter eventBridgeImporter;

	public static void main(String[] args) {
		SpringApplication.run(AwsMigratorApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		eventBridgeImporter.updateLocalEventBridge("default");
	}
}
