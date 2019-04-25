import org.joda.time.DateTime;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogRecordRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetRolePolicyRequest;

import java.util.Arrays;

/**
 * A simple AWS CloudWatch client that writes a log entry, then reads all logs in the group back.
 *
 * This code requires that the following environment variables are specified:
 *
 * AWS_ACCESS_KEY_ID
 * AWS_SECRET_ACCESS_KEY
 * AWS_REGION (eg. us-east-1)
 * AWS_LOG_GROUP (eg. test-logs)
 */
public class CloudWatchTester {

    public static final String AWS_REGION_ENVIRONMENT_VARIABLE = "AWS_REGION";
    public static final String LOG_GROUP_NAME = "AWS_LOG_GROUP";

    /**
     * Write a log, then read logs back.
     */
    public static void main(String[] args) {

        checkPermissions();
        writeLogs();
        getLogs();
    }

    /**
     * Check the permissions of the current user.
     */
    private static void checkPermissions() {

        IamClient client = IamClient.builder().region(Region.AWS_GLOBAL).build();
        client.getAccountSummary();
    }

    /**
     * Write a log message into CloudWatch.
     */
    private static void writeLogs() {
        CloudWatchLogsClient cwe =
                CloudWatchLogsClient.builder().build();

        String logMessageText = "{ \"key1\": \"value11\", \"key2\": \"value2\" }";
        InputLogEvent logEvent = InputLogEvent.builder().message(logMessageText).timestamp(DateTime.now().getMillis()).build();

        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                                                              .region(Region.US_EAST_1)
                                                              .build();

        // Get the first log stream name. We'll pull logs from this for now.
        DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                                                                               .logGroupName(getLogGroupName())
                                                                               .build();

        String sequenceToken = logsClient.describeLogStreams(logStreamsRequest).logStreams().get(0).uploadSequenceToken();

        PutLogEventsRequest request = PutLogEventsRequest.builder()
                                                         .logEvents(Arrays.asList(logEvent))
                                                         .logGroupName(getLogGroupName())
                                                         .logStreamName(getLogGroupName())
                                                         // Sequence token is required so that the log can be written to the latest location.
                                                         .sequenceToken(sequenceToken)
                                                         .build();
        cwe.putLogEvents(request);
    }

    /**
     * Query the log messages from CloudWatch.
     */
    private static void getLogs() {
        // Credentials are explicitly provided by environment variables.
        // See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html
        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                                                              .region(Region.of(System.getenv(AWS_REGION_ENVIRONMENT_VARIABLE)))
                                                              .build();
        DescribeLogStreamsRequest build = DescribeLogStreamsRequest.builder()
                                                                   .logGroupName(getLogGroupName())
                                                                   .build();

        // Get the first log stream name. We'll pull logs from this for now.
        String firstLogStreamName = logsClient.describeLogStreams(build).logStreams().get(0).logStreamName();

        // Get a page of log events from the first stream in the log group and print it to the console.
        logsClient.getLogEvents(GetLogEventsRequest.builder()
                                                   .logStreamName(firstLogStreamName)
                                                   .logGroupName(getLogGroupName())
                                                   .build())
                  .events().forEach(System.out::println);
    }

    /**
     * @return Resolve the log group name from an environment variable.
     */
    public static String getLogGroupName() {
        return System.getenv(LOG_GROUP_NAME);
    }
}
