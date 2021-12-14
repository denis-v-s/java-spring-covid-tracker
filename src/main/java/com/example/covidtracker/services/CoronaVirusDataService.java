package com.example.covidtracker.services;

import com.example.covidtracker.models.LocationStats;
import org.apache.commons.csv.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
//    @Scheduled(cron = "* * * * * *") // continuously run this method every second
    @Scheduled(cron = "* * 1 * * *") // continuously run this method on the first hour of every day
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.Builder
                .create(CSVFormat.EXCEL)
                .setHeader()
                .build()
                .parse(csvBodyReader);

        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));

            int latestTotal = Integer.parseInt(record.get(record.size() - 1));
            int prevTotal = Integer.parseInt(record.get(record.size() - 2));

            locationStat.setLatestTotalCases(latestTotal);
            locationStat.setDiffFromPrevDay(latestTotal - prevTotal);

            newStats.add(locationStat);
        }
        allStats = newStats;
    }

}
