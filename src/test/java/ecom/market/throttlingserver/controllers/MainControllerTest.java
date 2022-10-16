package ecom.market.throttlingserver.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void handleMultipleGetRequests() {
        final int testRequestsUpperBound = 5;
        final String textControllerUrl = "http://localhost:8888/test_throttling_controller";
        final int httpOkStatus = 200;
        //NOTE: see https://stackoverflow.com/a/9236244
        final int octetBound = 256;
        Random r = new Random();

        IntStream.rangeClosed(1, testRequestsUpperBound)
                .parallel()
                .forEach(i -> {
                    String randomIpAddress = r.nextInt(octetBound) + "." + r.nextInt(octetBound) + "." + r.nextInt(octetBound) +
                            "." + r.nextInt(octetBound);
                    try {
//                        System.out.println("mockMvc started, i = " + i);
                        mockMvc.perform(get(textControllerUrl).header("REMOTE_ADDR", randomIpAddress))
                                .andExpect(status().is(httpOkStatus));
//                        System.out.println("mockMvc ended, i = " + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

//        ExecutorService executor = Executors.newFixedThreadPool(testRequestsUpperBound);
//        Callable<String> callableTask = () -> {
//            String statusCode = "";
//            try {
//                String randomIpAddress = r.nextInt(octetBound) + "." + r.nextInt(octetBound) + "." + r.nextInt(octetBound) +
//                        "." + r.nextInt(octetBound);
//
////                CloseableHttpClient httpClient = HttpClients.createDefault();
////                HttpGet httpGetRequest = new HttpGet(textControllerUrl);
////                httpGetRequest.addHeader("REMOTE_ADDR", randomIpAddress);
////                HttpResponse response = httpClient.execute(httpGetRequest);
//
//                mockMvc.perform(get(textControllerUrl).header("REMOTE_ADDR", randomIpAddress))
//                        .andExpect(status().is(httpOkStatus));
//
////                statusCode = String.valueOf(response.getStatusLine().getStatusCode());
////                System.out.println("statusCode: " + statusCode);
////                assertEquals(statusCode, HttpStatus.SC_BAD_REQUEST);
////                httpClient.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return statusCode;
//        };
//        List<Callable<String>> callableTasks = new ArrayList<>();
//        for (int i = 0; i < testRequestsUpperBound; i++) {
//            callableTasks.add(callableTask);
//        }
//
//        try {
//            List<Future<String>> futures = executor.invokeAll(callableTasks);
//            String statuses = futures.stream().map(f -> {
//                try {
//                    return f.get();
//                } catch (Exception e) {
//                    throw new IllegalStateException(e);
//                }
//            }).collect(Collectors.joining(","));
//            System.out.println(statuses);
//        } catch (InterruptedException e) {// thread was interrupted
//            e.printStackTrace();
//        } finally {
//            // shut down the executor manually
//            executor.shutdown();
//        }
    }

}