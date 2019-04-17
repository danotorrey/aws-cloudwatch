import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class CloudWatchReader {

    public static final String AWS_REGION_ENVIRONMENT_VARIABLE = "AWS_REGION";

    public static void main(String[] args) {

        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                                                              .region(Region.of(System.getenv(AWS_REGION_ENVIRONMENT_VARIABLE)))
                                                              .build();
        logsClient.describeLogGroups();
    }
}
