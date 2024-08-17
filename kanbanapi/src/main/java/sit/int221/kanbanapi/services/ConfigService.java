package sit.int221.kanbanapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.configs.StatusConfiguration;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class ConfigService {

    @Autowired
    private StatusConfiguration configuration;

    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties";

    public StatusConfiguration getConfiguration() {
        return configuration;
    }

    public void updateStatusConfiguration(Boolean taskLimitEnabled, Integer maxTasksPerStatus) throws IOException {
        configuration.setTaskLimitEnabled(taskLimitEnabled);
        configuration.setMaxTasksPerStatus(maxTasksPerStatus);
        saveConfigurationToFile();
    }

    private void saveConfigurationToFile() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        }
        properties.setProperty("status.taskLimitEnabled", String.valueOf(configuration.getTaskLimitEnabled()));
        properties.setProperty("status.maxTasksPerStatus", String.valueOf(configuration.getMaxTasksPerStatus()));
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(output, null);
        }
    }
}

