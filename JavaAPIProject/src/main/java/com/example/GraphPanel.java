package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class GraphPanel extends JPanel {

    private String ticker = "AAPL";  
    private BufferedImage chartImage;

    public void setTicker(String ticker) 
    {
        this.ticker = ticker;
        loadChartImage();
    }

    private void loadChartImage() 
    {
        try 
        {
            String url = String.format("https://finviz.com/chart.ashx?t=%s&ty=c&ta=1&p=d&s=l", ticker);
            chartImage = ImageIO.read(new URL(url));
            repaint();
        } 
        catch (IOException e) 
        {
            System.err.println("Failed to load chart image: " + e.getMessage());
            chartImage = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if (chartImage != null) 
        {
            g.drawImage(chartImage, 0, 0, getWidth(), getHeight(), this);
        } 
        else 
        {
            g.drawString("Unable to load chart image.", 10, 20);
        }
    }
}