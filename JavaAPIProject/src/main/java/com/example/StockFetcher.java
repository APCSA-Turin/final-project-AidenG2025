package com.example;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class StockFetcher 
{

    private static final String API_KEY = "2EaBYgYJPS5mhUzpsJeGNGu3xIYums6P";

    public static List<StockGUI.Candle> fetchCandles(String ticker) throws Exception 
    {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusYears(1).minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String urlString = String.format
        (
            "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=5000&apiKey=%s",
            ticker, startDate.format(formatter), endDate.format(formatter), API_KEY);

        String jsonResponse = API.getData(urlString);

        JSONObject obj = new JSONObject(jsonResponse);

        JSONArray results = obj.optJSONArray("results");
        if (results == null) 
        {
            return new ArrayList<>();
        }

        List<StockGUI.Candle> candles = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) 
        {
            JSONObject day = results.getJSONObject(i);

            double open = day.getDouble("o");
            double high = day.getDouble("h");
            double low = day.getDouble("l");
            double close = day.getDouble("c");
            long timestampMs = day.getLong("t");

            LocalDate date = Instant.ofEpochMilli(timestampMs).atZone(ZoneId.systemDefault()).toLocalDate();

            candles.add(new StockGUI.Candle(date, open, high, low, close));
        }

        return candles;
    }
}