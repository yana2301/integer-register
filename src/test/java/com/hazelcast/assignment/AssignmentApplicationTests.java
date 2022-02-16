package com.hazelcast.assignment;

import com.hazelcast.assignment.repository.ActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
//@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssignmentApplicationTests {
    public static final int ACTION_VALUE = 2;
    private static final Random RND = new Random();
    @Autowired
    private ActionRepository actionRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void beforeMethod(){
        actionRepository.deleteAll();
    }
    @Test
    void addActionShouldBeIdempotent() {
        Integer actionId = RND.nextInt();
        assertThat(getCurrentValue()).isEqualTo(0);
        sendRequest(actionId, "/action/add", ACTION_VALUE);

        assertThat(getCurrentValue()).isEqualTo(ACTION_VALUE);

        sendRequest(actionId, "/action/add", ACTION_VALUE);

        assertThat(getCurrentValue()).isEqualTo(ACTION_VALUE);
    }

    @Test
    void compensateActionShouldBeAppliedIfSentBeforeMainAction() {
        Integer actionId = RND.nextInt();

        assertThat(getCurrentValue()).isEqualTo(0);
        sendRequest(actionId, "/action/add/compensate", ACTION_VALUE);

        assertThat(getCurrentValue()).isEqualTo(0);

        sendRequest(actionId, "/action/add", ACTION_VALUE);

        assertThat(getCurrentValue()).isEqualTo(0);
    }

    @Test
    void compensateActionShouldBeIdempotent() {
        Integer actionId = RND.nextInt();

        assertThat(getCurrentValue()).isEqualTo(0);
        sendRequest(actionId, "/action/add/compensate", ACTION_VALUE);
        sendRequest(actionId, "/action/add/compensate", ACTION_VALUE);

        assertThat(getCurrentValue()).isEqualTo(0);

    }

    @Test
    void addAndCompensateActionsShouldWorkSimultaneously() throws InterruptedException {
        int numberOfConcurrentCalls = 200;
        Integer actionId = RND.nextInt();
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentCalls);
        for (int i = 0; i < numberOfConcurrentCalls; i++) {
            String endpoint = (i%2 == 0) ? "/action/add/compensate" : "/action/add";
            service.submit(() -> {
                sendRequest(actionId, endpoint, 2);
                latch.countDown();
            });
        }
        latch.await();

        assertThat(getCurrentValue()).isEqualTo(0);
    }

    private void sendRequest(int actionId, String endpoint, int value){
        HttpHeaders headers = new HttpHeaders();
        headers.add("action-id", String.valueOf(actionId));
        HttpEntity<Integer> request = new HttpEntity<>(value, headers);
        this.restTemplate.postForEntity("http://localhost:" + port + endpoint, request, Integer.class);
    }

    private int getCurrentValue(){
       return this.restTemplate.getForObject("http://localhost:" + port + "/action/get", Integer.class);
    }

}
