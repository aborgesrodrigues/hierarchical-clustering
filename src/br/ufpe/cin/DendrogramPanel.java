/*******************************************************************************
 * Copyright 2013 Lars Behnke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package br.ufpe.cin;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.FastClusteringAlgorithm;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class DendrogramPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    final static BasicStroke solidStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    private Cluster model;
    private ClusterComponent component;
    private Color lineColor = Color.BLACK;
    private boolean showDistanceValues = false;
    private boolean showScale = true;
    private int borderTop = 20;
    private int borderLeft = 20;
    private int borderRight = 20;
    private int borderBottom = 20;
    private int scalePadding = 10;
    private int scaleTickLength = 4;
    private int scaleTickLabelPadding = 4;
    private double scaleValueInterval = 0;
    private int scaleValueDecimals = 0;

    private double xModelOrigin = 0.0;
    private double yModelOrigin = 0.0;
    private double wModel = 0.0;
    private double hModel = 0.0;
    private Map<String, Cluster> clusters = new HashMap<String, Cluster>();
    private int totalEdges;
    private DirectedSparseMultigraph<String, Weight> graph;
    
    public DendrogramPanel(DirectedSparseMultigraph<String, Weight> graph){
    	totalEdges = 0;
    	clusters = new HashMap<String, Cluster>();
    	this.graph = graph;
    }

    public boolean isShowDistanceValues() {
        return showDistanceValues;
    }

    public void setShowDistances(boolean showDistanceValues) {
        this.showDistanceValues = showDistanceValues;
    }

    public boolean isShowScale() {
        return showScale;
    }

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public int getScalePadding() {
        return scalePadding;
    }

    public void setScalePadding(int scalePadding) {
        this.scalePadding = scalePadding;
    }

    public int getScaleTickLength() {
        return scaleTickLength;
    }

    public void setScaleTickLength(int scaleTickLength) {
        this.scaleTickLength = scaleTickLength;
    }

    public double getScaleValueInterval() {
        return scaleValueInterval;
    }

    public void setScaleValueInterval(double scaleTickInterval) {
        this.scaleValueInterval = scaleTickInterval;
    }

    public int getScaleValueDecimals() {
        return scaleValueDecimals;
    }

    public void setScaleValueDecimals(int scaleValueDecimals) {
        this.scaleValueDecimals = scaleValueDecimals;
    }

    public int getBorderTop() {
        return borderTop;
    }

    public void setBorderTop(int borderTop) {
        this.borderTop = borderTop;
    }

    public int getBorderLeft() {
        return borderLeft;
    }

    public void setBorderLeft(int borderLeft) {
        this.borderLeft = borderLeft;
    }

    public int getBorderRight() {
        return borderRight;
    }

    public void setBorderRight(int borderRight) {
        this.borderRight = borderRight;
    }

    public int getBorderBottom() {
        return borderBottom;
    }

    public void setBorderBottom(int borderBottom) {
        this.borderBottom = borderBottom;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Cluster getModel() {
        return model;
    }

    public void setModel(Cluster model) {
        this.model = model;
        component = createComponent(model);
        updateModelMetrics();
    }

    private void updateModelMetrics() {
        double minX = component.getRectMinX();
        double maxX = component.getRectMaxX();
        double minY = component.getRectMinY();
        double maxY = component.getRectMaxY();

        xModelOrigin = minX;
        yModelOrigin = minY;
        wModel = maxX - minX;
        hModel = maxY - minY;
    }

    private ClusterComponent createComponent(Cluster cluster, VCoord initCoord, double clusterHeight) {

        ClusterComponent comp = null;
        if (cluster != null) {
            comp = new ClusterComponent(cluster, cluster.isLeaf(), initCoord);
            double leafHeight = clusterHeight / cluster.countLeafs();
            double yChild = initCoord.getY() - (clusterHeight / 2);
            double distance = cluster.getLevel();
            for (Cluster child : cluster.getChildren()) {
                int childLeafCount = child.countLeafs();
                double childHeight = childLeafCount * leafHeight;
                double childDistance = child.getLevel();
                VCoord childInitCoord = new VCoord(initCoord.getX() + (distance - childDistance), yChild + childHeight
                        / 2.0);
                yChild += childHeight;

                /* Traverse cluster node tree */
                ClusterComponent childComp = createComponent(child, childInitCoord, childHeight);

                childComp.setLinkPoint(initCoord);
                comp.getChildren().add(childComp);
                childComp.setParent(comp);
            }
        }
        return comp;

    }

    private ClusterComponent createComponent(Cluster model) {

        double virtualModelHeight = 1;
        VCoord initCoord = new VCoord(0, virtualModelHeight / 2);

        ClusterComponent comp = createComponent(model, initCoord, virtualModelHeight);
        comp.setLinkPoint(initCoord);
        return comp;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(lineColor);
        g2.setStroke(solidStroke);

        int wDisplay = getWidth() - borderLeft - borderRight;
        int hDisplay = getHeight() - borderTop - borderBottom;
        int xDisplayOrigin = borderLeft;
        int yDisplayOrigin = borderBottom;

        if (component != null) {

            int nameGutterWidth = component.getMaxNameWidth(g2, false) + component.getNamePadding();
            wDisplay -= nameGutterWidth;

            if (showScale) {
                Rectangle2D rect = g2.getFontMetrics().getStringBounds("0", g2);
                int scaleHeight = (int) rect.getHeight() + scalePadding + scaleTickLength + scaleTickLabelPadding;
                hDisplay -= scaleHeight;
                yDisplayOrigin += scaleHeight;
            }

            /* Calculate conversion factor and offset for display */
            double xFactor = wDisplay / wModel;
            double yFactor = hDisplay / hModel;
            int xOffset = (int) (xDisplayOrigin - xModelOrigin * xFactor);
            int yOffset = (int) (yDisplayOrigin - yModelOrigin * yFactor);
            component.paint(g2, xOffset, yOffset, xFactor, yFactor, showDistanceValues);

            if (showScale) {
                int x1 = xDisplayOrigin;
                int y1 = yDisplayOrigin - scalePadding;
                int x2 = x1 + wDisplay;
                int y2 = y1;
                g2.drawLine(x1, y1, x2, y2);

                double totalDistance = component.getCluster().getLevel();
                double xModelInterval;
                if (scaleValueInterval <= 0) {
                    xModelInterval = totalDistance / 10.0;
                } else {
                    xModelInterval = scaleValueInterval;
                }

                int xTick = xDisplayOrigin + wDisplay;
                y1 = yDisplayOrigin - scalePadding;
                y2 = yDisplayOrigin - scalePadding - scaleTickLength;
                double distanceValue = 0;
                double xDisplayInterval = xModelInterval * xFactor;
                while (xTick >= xDisplayOrigin) {
                    g2.drawLine(xTick, y1, xTick, y2);

                    String distanceValueStr = String.format("%." + scaleValueDecimals + "f", distanceValue);
                    Rectangle2D rect = g2.getFontMetrics().getStringBounds(distanceValueStr, g2);
                    g2.drawString(distanceValueStr, (int) (xTick - (rect.getWidth() / 2)), y2 - scaleTickLabelPadding);
                    xTick -= xDisplayInterval;
                    distanceValue += xModelInterval;
                }

            }
        } else {

            /* No data available */
            String str = "No data";
            Rectangle2D rect = g2.getFontMetrics().getStringBounds(str, g2);
            int xt = (int) (wDisplay / 2.0 - rect.getWidth() / 2.0);
            int yt = (int) (hDisplay / 2.0 - rect.getHeight() / 2.0);
            g2.drawString(str, xt, yt);
        }
    }

    public void init(List<Class> classes) {
    	
        JFrame frame = new JFrame();
        frame.setSize(1000, 600);
        //frame.setLocation(1000, 1000);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JPanel content = new JPanel();
        //DendrogramPanel dp = new DendrogramPanel();

        frame.setContentPane(content);
        content.setBackground(Color.red);
        content.setLayout(new BorderLayout());
        content.add(this, BorderLayout.CENTER);
        this.setBackground(Color.WHITE);
        this.setLineColor(Color.BLACK);
        this.setScaleValueDecimals(0);
        this.setScaleValueInterval(1);
        this.setShowDistances(false);

        System.out.println("create cluster");
        Cluster cluster = createCluster(graph, classes);
        System.out.println("cluster created");
        this.setModel(cluster);
        frame.setVisible(true);
    }
    
    private Cluster createCluster(DirectedSparseMultigraph<String, Weight> graph, List<Class> classes) {
    	List<Cluster> clustersList = new ArrayList<Cluster>();
    	//initiate the clusters
    	for(Class classType : classes){
    		Cluster cluster = new Cluster(classType.getName(), 0);
    		cluster.setColor(classType.getColor());
    		clusters.put(classType.getName(), cluster);
    		clustersList.add(cluster);
    	}
    	
    	for(Cluster cluster : clusters.values()){
	    	if(graph.getOutEdges(cluster.getName()) != null){
	    		for(Weight weight : graph.getOutEdges(cluster.getName())){
	    			addCluster(cluster, weight.getDestination());
	    		}
	    	}
	    	
	    	if(graph.getInEdges(cluster.getName()) != null){
	    		for(Weight weight : graph.getInEdges(cluster.getName())){
	    			addCluster(cluster, weight.getOrigin());
	    		}
	    	}
    	}
    	
    	for(Cluster cluster : clustersList)
    		cluster.setTotalEdges(this.totalEdges);
    	
    	
        FastClusteringAlgorithm alg = new FastClusteringAlgorithm();
        Cluster clusterRoot = alg.performClustering(clustersList);
        clusterRoot.toConsole(0);
        return clusterRoot;
    }
    
    private void addCluster(Cluster mainCluster, String vertex){
		Cluster cluster = clusters.get(vertex);
		if(cluster != null){
			if(!mainCluster.getOutputVertices().contains(cluster) && !cluster.getOutputVertices().contains(mainCluster))
				this.totalEdges++;
			if(!mainCluster.getOutputVertices().contains(cluster)){
				mainCluster.getOutputVertices().add(cluster);
				mainCluster.setOutputEdges(mainCluster.getOutputEdges() + 1);
				
			}
		}
    }

    private static Cluster createSampleCluster() {
    	/*double[][] distances = new double[][] { 
        	{  0,  1,  9,  7, 11, 14 }, 
        	{  1,  0,  4,  3,  8, 10 }, 
        	{  9,  4,  0,  9,  2,  8 },
            {  7,  3,  9,  0,  6, 13 }, 
            { 11,  8,  2,  6,  0, 10 }, 
            { 14, 10,  8, 13, 10,  0 } 
        };
        String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
    	double[][] distances = new double[][] { 
        	{  0,  4,  3,  2,  1,  4,  3,  3 }, 
        	{  4,  0,  1,  2,  3,  2,  3,  1 }, 
        	{  3,  1,  0,  1,  2,  3,  2,  2 },
            {  2,  2,  1,  0,  1,  2,  1,  1 }, 
            {  1,  3,  2,  1,  0,  3,  2,  2 }, 
            {  4,  2,  3,  2,  3,  0,  3,  1 },
            {  3,  3,  2,  1,  3,  3,  0,  2 },
            {  3,  1,  2,  1,  2,  1,  2,  0 }
        };
    	String[] names = new String[] { 
    			"ManterConvenioEJB", 
    			"ManterProjetoExtensaoEJB", 
    			"IManterProjetoExtensaoEJB", 
    			"ManterParticipacaoEJB", 
    			"IManterConvenioEJB", 
    			"ManterProgramaEJB", 
    			"IManterParticipacaoEJB" ,
    			"IManterProgramaEJB"
    			};*/
    	
    	List<Cluster> clusters = new ArrayList<Cluster>();
    	List<String> vertices;
    	Cluster clusterManterConvenioEJB = new Cluster("ManterConvenioEJB", 8);
    	Cluster clusterManterProjetoExtensaoEJB = new Cluster("ManterProjetoExtensaoEJB", 8);
    	Cluster clusterIManterProjetoExtensaoEJB = new Cluster("IManterProjetoExtensaoEJB", 8);
    	Cluster clusterManterParticipacaoEJB = new Cluster("ManterParticipacaoEJB", 8);
    	Cluster clusterIManterConvenioEJB = new Cluster("IManterConvenioEJB", 8);
    	Cluster clusterManterProgramaEJB = new Cluster("ManterProgramaEJB", 8);
    	Cluster clusterIManterParticipacaoEJB = new Cluster("IManterParticipacaoEJB", 8);
    	Cluster clusterIManterProgramaEJB = new Cluster("IManterProgramaEJB", 8);
    	
    	/*cluster = new Cluster("ManterConvenioEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("ManterConvenioEJB");
    	vertices.add("IManterConvenioEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterManterConvenioEJB.getOutputVertices().add(clusterIManterConvenioEJB);
    	clusterManterConvenioEJB.setOutputEdges(1);
    	clusters.add(clusterManterConvenioEJB);
    	
    	/*cluster = new Cluster("ManterProjetoExtensaoEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("ManterProjetoExtensaoEJB");
    	vertices.add("IManterProjetoExtensaoEJB");
    	vertices.add("IManterProgramaEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterManterProjetoExtensaoEJB.getOutputVertices().add(clusterIManterProjetoExtensaoEJB);
    	clusterManterProjetoExtensaoEJB.getOutputVertices().add(clusterIManterProgramaEJB);
    	clusterManterProjetoExtensaoEJB.setOutputEdges(2);
    	clusters.add(clusterManterProjetoExtensaoEJB);
    	
    	/*cluster = new Cluster("IManterProjetoExtensaoEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("IManterProjetoExtensaoEJB");
    	vertices.add("ManterProjetoExtensaoEJB");
    	vertices.add("ManterParticipacaoEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterIManterProjetoExtensaoEJB.getOutputVertices().add(clusterManterProjetoExtensaoEJB);
    	clusterIManterProjetoExtensaoEJB.getOutputVertices().add(clusterManterParticipacaoEJB);
    	clusterIManterProjetoExtensaoEJB.setOutputEdges(2);
    	clusters.add(clusterIManterProjetoExtensaoEJB);
    	
    	/*cluster = new Cluster("ManterParticipacaoEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("ManterParticipacaoEJB");
    	vertices.add("IManterConvenioEJB");
    	vertices.add("IManterProjetoExtensaoEJB");
    	vertices.add("IManterProgramaEJB");
    	vertices.add("IManterParticipacaoEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterManterParticipacaoEJB.getOutputVertices().add(clusterIManterConvenioEJB);
    	clusterManterParticipacaoEJB.getOutputVertices().add(clusterIManterProjetoExtensaoEJB);
    	clusterManterParticipacaoEJB.getOutputVertices().add(clusterIManterProgramaEJB);
    	clusterManterParticipacaoEJB.getOutputVertices().add(clusterIManterParticipacaoEJB);
    	clusterManterParticipacaoEJB.setOutputEdges(4);
    	clusters.add(clusterManterParticipacaoEJB);
    	
    	/*cluster = new Cluster("IManterConvenioEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("IManterConvenioEJB");
    	vertices.add("ManterConvenioEJB");
    	vertices.add("ManterParticipacaoEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterIManterConvenioEJB.getOutputVertices().add(clusterManterConvenioEJB);
    	clusterIManterConvenioEJB.getOutputVertices().add(clusterManterParticipacaoEJB);
    	clusterIManterConvenioEJB.setOutputEdges(2);
    	clusters.add(clusterIManterConvenioEJB);
    	
    	/*cluster = new Cluster("ManterProgramaEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("ManterProgramaEJB");
    	vertices.add("IManterProgramaEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterManterProgramaEJB.getOutputVertices().add(clusterIManterProgramaEJB);
    	clusterManterProgramaEJB.setOutputEdges(1);
    	clusters.add(clusterManterProgramaEJB);
    	
    	/*cluster = new Cluster("IManterParticipacaoEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("IManterParticipacaoEJB");
    	vertices.add("ManterParticipacaoEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterIManterParticipacaoEJB.getOutputVertices().add(clusterManterParticipacaoEJB);
    	clusterIManterParticipacaoEJB.setOutputEdges(1);
    	clusters.add(clusterIManterParticipacaoEJB);
    	
    	/*cluster = new Cluster("IManterProgramaEJB", 8);
    	vertices = new ArrayList<String>();
    	//vertices.add("IManterProgramaEJB");
    	vertices.add("ManterProgramaEJB");
    	vertices.add("ManterProjetoExtensaoEJB");
    	vertices.add("ManterParticipacaoEJB");
    	cluster.setOutputVertices(vertices);
    	clusters.add(cluster);*/
    	clusterIManterProgramaEJB.getOutputVertices().add(clusterManterProgramaEJB);
    	clusterIManterProgramaEJB.getOutputVertices().add(clusterManterProjetoExtensaoEJB);
    	clusterIManterProgramaEJB.getOutputVertices().add(clusterManterParticipacaoEJB);
    	clusterIManterProgramaEJB.setOutputEdges(3);
    	clusters.add(clusterIManterProgramaEJB);
    	
        FastClusteringAlgorithm alg = new FastClusteringAlgorithm();
        Cluster clusterRoot = alg.performClustering(clusters);
        clusterRoot.toConsole(0);
        return clusterRoot;
    }

	public ClusterComponent getComponent() {
		return component;
	}

	public void setComponent(ClusterComponent component) {
		this.component = component;
	}

}
