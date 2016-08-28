package afdx.report;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class DrawGraphs {
	String xAxisLabel = "", yAxisLabel = "";

	public void saveGraph2jpg(String filename, List<YIntervalSeries> series, double min, double max, int width) {
		if (width < 1000)
			width = 1000;
		
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		for (YIntervalSeries yIntervalSeries : series) {
			dataset.addSeries(yIntervalSeries);
		}

		boolean createLegend = false;

		// create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart("AFDX", this.xAxisLabel, this.yAxisLabel, dataset,
				PlotOrientation.VERTICAL, createLegend, true, false);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customization...
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

		plot = chart.getXYPlot();
		plot.setNoDataMessage("No data");
		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		// xAxis.setLabel("VL Ids");
		xAxis.setRange(0, series.get(0).getItemCount());
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		// yAxis.setLabel("Latency (ms)");
		yAxis.setRange(min * .9, max * 1.1);

		DeviationRenderer coPolRenderer1 = new DeviationRenderer(true, false);
		coPolRenderer1.setSeriesFillPaint(0, new Color(0, 255, 0));
		coPolRenderer1.setSeriesPaint(0, new Color(0, 0, 255));
		plot.setRenderer(0, coPolRenderer1);
		DeviationRenderer coPolRenderer2 = new DeviationRenderer(true, false);
		coPolRenderer2.setSeriesFillPaint(1, new Color(255, 0, 0));
		coPolRenderer2.setSeriesPaint(1, new Color(0, 255, 0));
		plot.setRenderer(1, coPolRenderer2);

		// XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)
		// plot.getRenderer();
		// renderer.setShapesVisible(true);
		// renderer.setShapesFilled(true);
		// // change the auto tick unit selection to integer units only...
		// NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// DeviationRenderer renderer = new DeviationRenderer(true, false);
		// Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND);
		// if (dataset.getSeriesCount() == 1) {
		// renderer.setSeriesStroke(0, stroke);
		// renderer.setSeriesPaint(0, Color.RED);
		// renderer.setSeriesFillPaint(0, Color.RED);
		// } else {
		// for (int i1 = 0; i1 < dataset.getSeriesCount(); i1++) {
		// renderer.setSeriesStroke(i1, stroke);
		// Color color = getColorProvider().getPointColor((double) i1 / (double)
		// (dataset.getSeriesCount() - 1));
		// renderer.setSeriesPaint(i1, color);
		// renderer.setSeriesFillPaint(i1, color);
		// }
		// }
		// renderer.setAlpha(0.12f);
		//
		// plot.setRenderer(renderer);
		//
		// ValueAxis valueAxis = plot.getRangeAxis();
		// valueAxis.setLabelFont(LABEL_FONT_BOLD);
		// valueAxis.setTickLabelFont(LABEL_FONT);

		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.substring(0, path.lastIndexOf("/") + 1);
		String dir = path + filename + ".jpg";

		try {
			ChartUtilities.saveChartAsJPEG(new File(dir), chart, width, 500);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
