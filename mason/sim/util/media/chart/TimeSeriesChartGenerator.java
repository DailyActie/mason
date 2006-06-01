/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.util.media.chart;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

// From MASON (cs.gmu.edu/~eclab/projects/mason/)
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;

// From JFreeChart (jfreechart.org)
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.general.*;

// from iText (www.lowagie.com/iText/)
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
   TimeSeriesChartGenerator is a ChartGenerator which displays a time-series chart using the JFreeChart library.
   The generator uses the XYSeriesCollection as its dataset, and thus holds some N XYSeriesDataset objects, each
   representing a time series displayed on the chart.  You add series to the generator with the <tt>addSeries</tt>
   method.
   
   <p>TimeSeriesChartGenerator creates attributes components in the form of TimeSeriesAttributes, which work with
   the generator to properly update the chart to reflect changes the user has made to its display.
*/

public class TimeSeriesChartGenerator extends ChartGenerator
    {
    /** The dataset.  Generated in buildChart(). */
    protected XYSeriesCollection dataset;
    /** A list of SeriesChangeListeners, one per element in the dataset, and indexed in the same way.
        When an element is removed from the dataset and deleted from the chart, its corresponding
        SeriesChangeListener will be removed and have seriesChanged(...) called. */
    protected ArrayList stoppables = new ArrayList();
        
    public AbstractSeriesDataset getSeriesDataset() { return dataset; }

    DatasetChangeEvent updateEvent;
    // We issue a datset change event because various changes may have been made by our attributes
    // objects and they haven't informed the graph yet.   That way we can bulk up lots of changes
    // before we do a redraw.
    public void update()
        {
        if (updateEvent == null)
            updateEvent = new DatasetChangeEvent(chart.getPlot(), null);
        chart.getPlot().datasetChanged(updateEvent);
        }

    public void removeSeries(int index)
        {
        // stop the inspector....
        Object tmpObj = stoppables.remove(index);
        if( ( tmpObj != null ) && ( tmpObj instanceof SeriesChangeListener ) )
            ((SeriesChangeListener)tmpObj).seriesChanged(new SeriesChangeEvent(this));
        
        // remove from the dataset.  This is easier done in some JFreeChart plots than others, dang coders
        dataset.removeSeries(index);
                
        // remove the attribute
        seriesAttributes.remove(index);
                
        // shift all the seriesAttributes' indices down so they know where they are
        Component[] c = seriesAttributes.getComponents();
        for(int i = index; i < c.length; i++)  // do for just the components >= index in the seriesAttributes
            {
            if (i >= index) 
                {
                SeriesAttributes csa = (SeriesAttributes)(c[i]);
                csa.setSeriesIndex(csa.getSeriesIndex() - 1);
                csa.rebuildGraphicsDefinitions();
                }
            }
        revalidate();
        }
                
    protected void buildChart()
        {
        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart("Untitled Chart","Untitled X Axis","Untitled Y Axis",dataset,
                                               PlotOrientation.VERTICAL, false, true, false);
        ((XYLineAndShapeRenderer)(((XYPlot)(chart.getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);

        chart.setAntiAlias(false);
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setPreferredSize(new java.awt.Dimension(640,480));
        chartPanel.setMinimumDrawHeight(10);
        chartPanel.setMaximumDrawHeight(2000);
        chartPanel.setMinimumDrawWidth(20);
        chartPanel.setMaximumDrawWidth(2000);
        chartHolder.getViewport().setView(chartPanel);
        }


    /** Adds a series, plus a (possibly null) SeriesChangeListener which will receive a <i>single</i>
        event if/when the series is deleted from the chart by the user.  The series should have a key
        in the form of a String.  Returns the series index number. */
    public int addSeries( final XYSeries series, final org.jfree.data.general.SeriesChangeListener stopper)
        {
        int i = dataset.getSeriesCount();
        dataset.addSeries(series);
        SeriesAttributes csa = new TimeSeriesAttributes(this, series, i); 
        seriesAttributes.add(csa);
        stoppables.add( stopper );
        revalidate();
        return i;
        }
        

    }
