import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@SpringBootApplication
@RestController
public class AverageCalculatorApplication {

    private static final int NUMBER_CACHE_SIZE = 10;
    private static final String NUMBER_SOURCE_API = "http://20.244.56.144/test";

    private List<Integer> numberCache = new ArrayList<>();
    @GetMapping("/numbers/{numberType}")
    public Response getNumbers(@PathVariable String numberType) {
        long startTime = System.currentTimeMillis();
        if (!isValidNumberType(numberType)) {
            return new Response("Invalid number type", null, null, null, null);
        }
        String apiEndpoint = getApiEndpoint(numberType);
        List<Integer> newNumbers = fetchNumbersFromSource(apiEndpoint);
        if (newNumbers == null) {
            return new Response("Error fetching numbers", null, null, null, null);
        }

        storeNumbers(newNumbers);
        List<Integer> previousState = getPreviousState();
        List<Integer> currentState = getCurrentState();
        double average = calculateAverage();
        Response response = new Response(previousState, currentState, newNumbers, average);
        if (System.currentTimeMillis() - startTime > 500) {
            return new Response("Response time exceeded 500ms", null, null, null, null);
        }
        return response;
    }
     private List<Integer> fetchNumbersFromSource(String apiEndpoint) {
        try {
            String url = NUMBER_SOURCE_API + "/" + apiEndpoint;
            String response = restTemplate.getForObject(url, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            return null;
        }
    }
    private List<Integer> parseResponse(String response) {
        // parse JSON response and extract numbers
        // ...
    }

    private void storeNumbers(List<Integer> newNumbers) {
        for (int num : newNumbers) {
            if (!numberCache.contains(num)) {
                numberCache.add(num);
            }
        }
        if (numberCache.size() > NUMBER_CACHE_SIZE) {
            numberCache = numberCache.subList(numberCache.size() - NUMBER_CACHE_SIZE, numberCache.size());
        }
    }

    private List<Integer> getPreviousState() {
        return numberCache.subList(0, numberCache.size() - newNumbers.size());
    }

    private List<Integer> getCurrentState() {
        return numberCache;
    }

    private double calculateAverage() {
        return numberCache.stream().mapToDouble(i -> i).average().orElse(0.0);
    }

    public static void main(String[] args) {
        SpringApplication.run(AverageCalculatorApplication.class, args);
    }
}

class Response {
    private String error;
    private List<Integer> previousState;
    private List<Integer> currentState;
    private List<Integer> numbers;
    private double average;

    public Response(String error, List<Integer> previousState, List<Integer> currentState, List<Integer> numbers, double average) {
        this.error = error;
        this.previousState = previousState;
        this.currentState = currentState;
        this.numbers = numbers;
        this.average = average;
    }
