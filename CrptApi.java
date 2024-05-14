import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private int requestCounter = 0;
    private long lastRequestTime = System.currentTimeMillis();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public synchronized void createDocument(Object document, String signature) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRequestTime;

        if (elapsedTime >= timeUnit.toMillis(1)) {
            requestCounter = 0;
            lastRequestTime = currentTime;
        }

        if (requestCounter >= requestLimit) {
            try {
                wait(timeUnit.toMillis(1) - elapsedTime);
                requestCounter = 0;
                lastRequestTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");

        try {
            Gson gson = new Gson();
            String jsonDocument = gson.toJson(document);

            httpPost.setEntity(new StringEntity(jsonDocument));
            httpPost.setHeader("Content-Type", "application/json");

            HttpResponse response = httpClient.execute(httpPost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }

            System.out.println(responseContent.toString());

            requestCounter++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
