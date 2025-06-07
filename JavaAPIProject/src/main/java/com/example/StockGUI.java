package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockGUI {

    private JTextArea outputArea;
    private GraphPanel graphPanel;

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                new StockGUI().createAndShowGUI();
            }
        });
    }

    public void createAndShowGUI() 
    {
        JFrame frame = new JFrame("Stock Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);

        JLabel label = new JLabel("Enter stock ticker (e.g., AAPL):");
        final JTextField tickerInput = new JTextField(10);
        final JButton fetchButton = new JButton("Get Stock Prices");

        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(680, 300));
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        fetchButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                fetchButton.setEnabled(false);
                final String ticker = tickerInput.getText().toUpperCase().trim();

                if (ticker.isEmpty()) 
                {
                    outputArea.setText("Please enter a ticker symbol.");
                    fetchButton.setEnabled(true);
                    return;
                }

                new Thread(new Runnable() 
                {
                    public void run() 
                    {
                        try {
                            List<Candle> candles = StockFetcher.fetchCandles(ticker);

                            if (candles.isEmpty()) 
                            {
                                SwingUtilities.invokeLater(new Runnable() 
                                {
                                    public void run() 
                                    {
                                        outputArea.setText("No data found for ticker: " + ticker);
                                    }
                                });
                                return;
                            }

                            final Candle last = candles.get(candles.size() - 1);
                            LocalDate lastDate = last.date;
                            LocalDate oneMonthAgoDate = DateUtils.getPreviousValidDate(lastDate.minusMonths(1));
                            LocalDate oneYearAgoDate = DateUtils.getPreviousValidDate(lastDate.minusYears(1));

                            Candle first = candles.get(0);
                            for (Candle c : candles) {
                                if (!c.date.isBefore(oneYearAgoDate)) 
                                {
                                    first = c;
                                    break;
                                }
                            }

                            double percentChange = percentChangeBetween(first.close, last.close);
                            double oneMonthAgoPrice = getClosePriceForDate(candles, oneMonthAgoDate);
                            double oneYearAgoPrice = getClosePriceForDate(candles, oneYearAgoDate);

                            final String result = String.format(
                                "Stock: %s\n" +
                                "Data points: %d\n\n" +
                                "First day (%s) - Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f\n" +
                                "Last day (%s) - Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f\n\n" +
                                "Price one month ago (%s): %.2f\n" +
                                "Price one year ago (%s): %.2f\n\n" +
                                "Percent change from First day to Last day (close): %.2f%%\n" +
                                "Percent change from %s to %s: %.2f%%\n" +
                                "Percent change from %s to %s: %.2f%%",
                                ticker, candles.size(),
                                first.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), first.open, first.high, first.low, first.close,
                                last.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), last.open, last.high, last.low, last.close,
                                oneMonthAgoDate, oneMonthAgoPrice,
                                oneYearAgoDate, oneYearAgoPrice,
                                percentChange,
                                oneMonthAgoDate, last.date, percentChangeBetween(oneMonthAgoPrice, last.close),
                                oneYearAgoDate, last.date, percentChangeBetween(oneYearAgoPrice, last.close)
                            );

                            SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    outputArea.setText(result);
                                    graphPanel.setTicker(ticker);
                                }
                            });

                        } catch (final Exception ex) 
                        {
                            SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    outputArea.setText("Error fetching data: " + ex.getMessage());
                                }
                            });
                        } 
                        finally 
                        {
                            SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    fetchButton.setEnabled(true);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(label);
        inputPanel.add(tickerInput);
        inputPanel.add(fetchButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(graphPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private double getClosePriceForDate(List<Candle> candles, LocalDate targetDate) 
    {
        for (Candle c : candles) 
        {
            if (!c.date.isBefore(targetDate)) 
            {
                return c.close;
            }
        }
        return candles.get(candles.size() - 1).close;
    }

    private double percentChangeBetween(double oldPrice, double newPrice) 
    {
        return ((newPrice - oldPrice) / oldPrice) * 100;
    }

    public static class Candle 
    {
        public LocalDate date;
        public double open, high, low, close;

        public Candle(LocalDate date, double open, double high, double low, double close) 
        {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }
}