package code.codevomit.covid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class RequestController {

    int patientCounter = 0;
    Map<String, Patient> allPatients = new HashMap<>();

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patient {
        @JsonProperty("id") String id;
        @JsonProperty("city") String city;
        @JsonProperty("state") String state;
        @JsonProperty("country") String country;
        @JsonProperty("status") String status;
        @JsonProperty("infected_date") Long infected_date;
        @JsonProperty("updated_at") Long updated_at;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRequest {
        @JsonProperty("city") String city;
        @JsonProperty("state") String state;
        @JsonProperty("country") String country;
        @JsonProperty("status") String status;
        @JsonProperty("infected_date") Long infected_date;
        @JsonProperty("updated_at") Long updated_at;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @JsonProperty("status") String status;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountResponse {
        @JsonProperty("count") int count;
    }

    @RequestMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping(path="/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> add(@RequestBody AddRequest request) {
        Patient p = new Patient();
        synchronized (allPatients)  {
            patientCounter = patientCounter +1;
            String id = Integer.toString(patientCounter);
            p.id = id;
            p.city = request.city;
            p.state = request.state;
            p.country = request.country;
            p.status = request.status;
            p.infected_date = request.infected_date;
            p.updated_at = request.updated_at;
            allPatients.put(p.id, p);
            System.out.println(allPatients);
        }
        return ResponseEntity.of(Optional.of(p));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Patient> update(@PathVariable String id,@RequestBody UpdateRequest updateRequest) {
        synchronized (allPatients) {
            Patient patient = allPatients.get(id);
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            patient.status = updateRequest.status;
            patient.updated_at = System.currentTimeMillis();
            allPatients.put(patient.id, patient);

            return ResponseEntity.of(Optional.of(patient));
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Patient> get(@PathVariable String id) {
        synchronized (allPatients) {
            Patient patient = allPatients.get(id);
            System.out.println(allPatients);
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(Optional.of(patient));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<CountResponse> count(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city
    ) {
        synchronized (allPatients) {
            int count = 0;
            for (Map.Entry<String,Patient> patient : allPatients.entrySet()) {
                boolean flag1=true, flag2=true, flag3=true, flag4=true;
                if (!StringUtils.isEmpty(status)) {
                    if (!patient.getValue().status.equals(status)) {
                        flag1 = false;
                    }
                }
                if (!StringUtils.isEmpty(country)) {
                    if (!patient.getValue().country.equals(country)) {
                        flag2 = false;
                    }
                }
                if (!StringUtils.isEmpty(state)) {
                    if (!patient.getValue().state.equals(state)) {
                        flag3 = false;
                    }
                }
                if (!StringUtils.isEmpty(city)) {
                    if (!patient.getValue().city.equals(city)) {
                        flag4 = false;
                    }
                }

                if(flag1 && flag2 && flag3 && flag4) {
                    count++;
                }
            }
            CountResponse r = new CountResponse(count);
            return ResponseEntity.of(Optional.of(r));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity reset() {
        synchronized (allPatients) {
            allPatients.clear();
            return ResponseEntity.ok().build();
        }
    }
}
