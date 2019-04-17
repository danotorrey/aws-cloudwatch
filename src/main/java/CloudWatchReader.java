import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

public class CloudWatchReader {

    public static final String AWS_REGION_ENVIRONMENT_VARIABLE = "AWS_REGION";
    public static final String LOG_GROUP_NAME = "AWS_LOG_GROUP";

    /**
     * Simple sample client that reads logs from an AWS log group.
     */
    public static void main(String[] args) {

        // Credentials are explicitly provided by environment variables.
        // See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html
        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                                                              .region(Region.of(System.getenv(AWS_REGION_ENVIRONMENT_VARIABLE)))
                                                              .build();
        DescribeLogStreamsRequest build = DescribeLogStreamsRequest.builder()
                .logGroupName("integrations-flowlogs")
                .build();

        // Get the first log stream name. We'll pull logs from this for now.
        String firstLogStreamName = logsClient.describeLogStreams(build).logStreams().get(0).logStreamName();

        // Get a page of log events from the first stream in the log group and print it to the console.
        logsClient.getLogEvents(GetLogEventsRequest.builder()
                                                   .logStreamName(firstLogStreamName)
                                                   .logGroupName(getLogGroupName())
                                                   .build())
                  .events().stream().forEach(System.out::println);

    }

    /**
     * @return Resolve the log group name from an environment variable.
     */
    public static String getLogGroupName() {
        return System.getenv(LOG_GROUP_NAME);
    }
}
