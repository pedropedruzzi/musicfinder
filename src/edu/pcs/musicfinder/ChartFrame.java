package edu.pcs.musicfinder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ChartFrame extends ApplicationFrame {

	private XYDataset dataset;
	
	public ChartFrame(String title, XYDataset dataset) {
		super(title);
		this.dataset = dataset;
		
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
	}
	
	public ChartFrame(String title, double[] data) {
		super(title);
        this.dataset = createDataset(data);
        
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }	
	

    private XYDataset createDataset(double[] data) {
    	TimeSeries series = new TimeSeries("xxxxx", FixedMillisecond.class);
    	for (int i = 0; i < data.length; i++)
    		series.add(new FixedMillisecond(i), data[i]);
    	
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
    
    private JFreeChart createChart(final XYDataset dataset) {
    	
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Time Series Demo 8", 
            "Date", 
            "Value",
            dataset,
            true, 
            true, 
            false
        );
        
        return chart;
    }
    
    public static void main(final String[] args) {
    	
    	double[] data = new double[100];
    	for (int i = 0; i < 100; i++)
			data[i] = (i * i) % 10;
    	
        final ChartFrame demo = new ChartFrame("Time Series Demo 8", data);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
