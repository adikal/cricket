package cricket.graphstats;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

import cricket.model.GameEvent;
import cricket.model.MatchState;
import cricket.model.ResultHistory;
import cricket.model.Team;
import cricket.utils.Pair;

public class DrawScoringCharts {

	public static class SeriesInfo {
		XYSeries teamScoring;
		List<XYSeries> wickets = new ArrayList<>();
		DefaultCategoryDataset bowlingInfo;

		public SeriesInfo(XYSeries ts, List<XYSeries> wkts,
				DefaultCategoryDataset bowling) {
			this.teamScoring = ts;
			this.wickets = wkts;
			this.bowlingInfo = bowling;
		}
	}

	public static List<SeriesInfo> createAllSeriesInfoDataSet(
			Map<Team, Pair<ResultHistory, MatchState>> team2History) {
		List<SeriesInfo> ret = new ArrayList<>();
		int index = 0;
		for (Map.Entry<Team, Pair<ResultHistory, MatchState>> entry : team2History
				.entrySet()) {
			Team team = entry.getKey();
			ResultHistory history = entry.getValue().getFirst();
			MatchState finalMatchState = entry.getValue().getSecond();
			String goal = index == 0 ? " (Batting 1st: " : " (Batting 2nd: ";
			goal += finalMatchState.getRunsScored()
					+"/"+finalMatchState.getWicketsFallen()+")";
			// create series info
			XYSeries series = new XYSeries(team.getName() + goal);
			List<XYSeries> wickets = new ArrayList<>();
			DefaultCategoryDataset bowlingInfo = new DefaultCategoryDataset();
			// iterate through innings history for team and build up series info
			int runsStartOver = 0;
			int overNumber = 1;
			for (GameEvent ge : history.getGameEvents()) {
				series.add(ge.getMatchState().getTotalBalls(), ge
						.getMatchState().getRunsScored());

				if (ge.getOutcome().isWicketFell()) {
					XYSeries wicket = new XYSeries(ge.getOutcome().getStriker()
							.getName()
							+ " (" + String.valueOf(ge.getMatchState().getRunsScored())
							+ "/"+String.valueOf(wickets.size() + 1) 
							+", "+String.valueOf(ge.getMatchState().getOversCompleted())+"."+ge.getMatchState().getBallsInOver() 
							+")");
					wicket.add(ge.getMatchState().getTotalBalls(), ge
							.getMatchState().getRunsScored());
					wickets.add(wicket);
					// dataset.addSeries(wicket);
				}

				if (ge.getMatchState().getBallsInOver() == 0) {
					runsStartOver = ge.getMatchState().getRunsScored();
				}

				if (ge.getMatchState().getBallsInOver() == 5) {
					String bowler = ge.getMatchState().getCurrentBowler()
							.getName();
					int runsInOver = ge.getMatchState().getRunsScored()
							+ ge.getOutcome().getRunsScored() - runsStartOver;
					bowlingInfo.addValue(runsInOver, String.valueOf(overNumber)
							+ ":" + bowler, "Overs");
					overNumber++;
				}

				/*
				 * if (ge.getMatchState().getBallsInOver()==0) { XYSeries bowler
				 * = new
				 * XYSeries(ge.getMatchState().getCurrentBowler().getName());
				 * bowler.add(ge.getMatchState().getTotalBalls(),
				 * ge.getMatchState().getRunsScored());
				 * dataset.addSeries(bowler); }
				 */
			}
			series.add(finalMatchState.getTotalBalls(),
					finalMatchState.getRunsScored());

			ret.add(new SeriesInfo(series, wickets, bowlingInfo));

			// dataset.addSeries(series);
			index++;
		}

		return ret;
	}

	public static void renderDataset(
			Map<Team, Pair<ResultHistory, MatchState>> team2History) {
		renderDataset(createAllSeriesInfoDataSet(team2History));
	}

	public static void renderDataset(List<SeriesInfo> seriesInfo) {

		Shape ellipse = new Ellipse2D.Double(0, 0, 5, 5);
		Shape cross = ShapeUtilities.createDiagonalCross(3, 1);

		XYSeriesCollection dataset = new XYSeriesCollection();
		for (SeriesInfo si : seriesInfo) {
			dataset.addSeries(si.teamScoring);
			for (XYSeries wicket : si.wickets) {
				dataset.addSeries(wicket);
			}
		}

		NumberAxis xAxis = new NumberAxis("balls");
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis("runs");
		yAxis.setAutoRangeIncludesZero(false);

		XYSplineRenderer spline = new XYSplineRenderer();
		XYPlot xyplot = new XYPlot(dataset, xAxis, yAxis, spline);
		xyplot.setBackgroundPaint(Color.lightGray);
		xyplot.setDomainGridlinePaint(Color.white);
		xyplot.setRangeGridlinePaint(Color.white);
		xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

		// set series display options
		spline.setSeriesShapesVisible(0, false);
		spline.setSeriesStroke(0, new BasicStroke(4f));
		spline.setSeriesPaint(0, ChartColor.LIGHT_GREEN);

		int team2SeriesIndex = seriesInfo.get(0).wickets.size() + 1;
		spline.setSeriesShapesVisible(team2SeriesIndex, false);
		spline.setSeriesStroke(team2SeriesIndex, new BasicStroke(4f));
		spline.setSeriesPaint(team2SeriesIndex, ChartColor.LIGHT_CYAN);

		JFreeChart chart = new JFreeChart("Scoring Chart",
				JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
		ChartPanel chartpanel = new ChartPanel(chart);
		chartpanel.setDomainZoomable(true);

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout());
		jPanel.add(chartpanel, BorderLayout.NORTH);

		ChartPanel barchartpanel1 = createBarChartPanel(seriesInfo.get(0).bowlingInfo);
		ChartPanel barchartpanel2 = createBarChartPanel(seriesInfo.get(1).bowlingInfo);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(barchartpanel1, BorderLayout.WEST);
		southPanel.add(barchartpanel2, BorderLayout.EAST);
		southPanel.validate();

		jPanel.add(southPanel, BorderLayout.SOUTH);

		JFrame frame = new JFrame();
		frame.getContentPane().add(jPanel);
		frame.pack();
		frame.setSize(1280, 1024);
		frame.setVisible(true);
	}

	private static ChartPanel createBarChartPanel(DefaultCategoryDataset dataset) {
		JFreeChart barChart = ChartFactory.createBarChart("Bowling", "Bowlers",
				"Runs", dataset, PlotOrientation.VERTICAL, true, // legend
				true, // tooltips
				false // url
				);

		CategoryPlot plot = barChart.getCategoryPlot();
		BarRenderer renderer = new CustomRenderer();
		renderer.setBaseItemLabelGenerator(new CustomLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		plot.setRenderer(renderer);
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);
		
		ChartPanel barchartpanel = new ChartPanel(barChart);
		barchartpanel.setDomainZoomable(true);
		return barchartpanel;
	}

	public static class CustomRenderer extends BarRenderer {
		private static final long serialVersionUID = 1L;

		public CustomRenderer() {
		}

		public Paint getItemPaint(final int row, final int column) {
			// return (column >= 8) ? ChartColor.LIGHT_RED :
			// ChartColor.LIGHT_BLUE;
			CategoryDataset l_jfcDataset = getPlot().getDataset();
			String l_rowKey = (String) l_jfcDataset.getRowKey(row);
			String l_colKey = (String) l_jfcDataset.getColumnKey(column);
			double l_value = l_jfcDataset.getValue(l_rowKey, l_colKey)
					.doubleValue();
			return l_value >= 8 ? ChartColor.DARK_BLUE : ChartColor.LIGHT_CYAN;
		}

		public double getItemMargin() {
			return 0.1d;
		}
	}

	public static class CustomCategorySeriesLabelGenerator extends StandardCategorySeriesLabelGenerator {
		
		private static final long serialVersionUID = 1L;

		@Override
		public String generateLabel(CategoryDataset dataset, int series) {
			return String.valueOf(series);
		}
		
	}
	
	public static class CustomLabelGenerator extends
			AbstractCategoryItemLabelGenerator implements
			CategoryItemLabelGenerator {

		private static final long serialVersionUID = 1L;

		public CustomLabelGenerator() {
			super("", NumberFormat.getInstance());

		}

		/**
		 * Generates a label for the specified item. The label is typically a
		 * formatted version of the data value, but any text can be used.
		 *
		 * @param dataset
		 *            the dataset (<code>null</code> not permitted).
		 * @param series
		 *            the series index (zero-based).
		 * @param category
		 *            the category index (zero-based).
		 *
		 * @return the label (possibly <code>null</code>).
		 */
		public String generateLabel(CategoryDataset dataset, int series,
				int category) {

			String result = null;

			Number value = dataset.getValue(series, category);

			//result = String.valueOf(series+1);  // could apply formatting here
			result = "("+value.toString()+")";
			
			return result;
		}
	}

}
