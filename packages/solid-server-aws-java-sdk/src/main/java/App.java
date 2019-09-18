import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
        SynchronousQueue<Runnable> queue = new SynchronousQueue<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 48, 15, TimeUnit.SECONDS, queue, Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        try (BufferedReader reader = new BufferedReader(new FileReader("domains"))) {
            while (reader.ready()) {
                String line = reader.readLine();
                executor.execute(() -> {
                    s3.deleteObject("yodata-dev-solid-server-storage", "entities/" + line + "/data/by-id/profile/card");
                    System.out.println("Deleted " + line);
                });
            }
        }

        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

}
