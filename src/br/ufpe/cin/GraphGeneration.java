/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 */
package br.ufpe.cin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * Demonstrates the AggregateLayout
 * class. In this demo, vertices are visually clustered as they
 * are selected. The cluster is formed in a new Layout centered at the
 * middle locations of the selected vertices. The size and layout
 * algorithm for each new cluster is selectable.
 * 
 * @author Tom Nelson
 * 
 */
@SuppressWarnings("serial")
public class GraphGeneration extends JApplet {

    String instructions =
        "<html>"+
        "Use the Layout combobox to select the "+
        "<p>underlying layout."+
        "<p>Use the SubLayout combobox to select "+
        "<p>the type of layout for any clusters you create."+
        "<p>To create clusters, use the mouse to select "+
        "<p>multiple vertices, either by dragging a region, "+
        "<p>or by shift-clicking on multiple vertices."+
        "<p>After you select vertices, use the "+
        "<p>Cluster Picked button to cluster them using the "+
        "<p>layout and size specified in the Sublayout comboboxen."+
        "<p>Use the Uncluster All button to remove all"+
        "<p>clusters."+
        "<p>You can drag the cluster with the mouse." +
        "<p>Use the 'Picking'/'Transforming' combo-box to switch"+
        "<p>between picking and transforming mode.</html>";
    /**
     * the graph
     */
    Graph<String,Weight> graph;
    
    Map<Graph<String,Weight>,Dimension> sizes = new HashMap<Graph<String,Weight>,Dimension>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	java.lang.Class<Layout>[] layoutClasses = new java.lang.Class[] {
		CircleLayout.class,SpringLayout.class,FRLayout.class,KKLayout.class
    };
    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String,Weight> vv;

    AggregateLayout<String,Weight> clusteringLayout;
    
    Dimension subLayoutSize;
    
    PickedState<String> ps;
    
    @SuppressWarnings("rawtypes")
    java.lang.Class<CircleLayout> subLayoutType = CircleLayout.class;
    
    Map<String, Color>colors = new HashMap<String, Color>();
    Map<String, Color>customColors = new HashMap<String, Color>();
    Map<String, Color>auxColors = new HashMap<String, Color>();
    
    private ClassParser classParser;
    private List<List<Class>> circular = new ArrayList<List<Class>>();
    private Properties config = Properties.getInstance();
    
    /**
     * create an instance of a simple graph with controls to
     * demo the zoomand hyperbolic features.
     * 
     */
    public GraphGeneration() {
        
        // create a simple graph for the demo
        //graph = TestGraphs.getOneComponentGraph();
    	this.createGraph();

        // ClusteringLayout is a decorator class that delegates
        // to another layout, but can also sepately manage the
        // layout of sub-sets of vertices in circular clusters.
        clusteringLayout = new AggregateLayout<String,Weight>(new FRLayout<String,Weight>(graph));
        	//new SubLayoutDecorator<String,Number>(new FRLayout<String,Number>(graph));

        Dimension preferredSize = new Dimension(3000,3000);
        final VisualizationModel<String,Weight> visualizationModel = 
            new DefaultVisualizationModel<String,Weight>(clusteringLayout, preferredSize);
        vv =  new VisualizationViewer<String,Weight>(visualizationModel, preferredSize);
        
        ps = vv.getPickedVertexState();
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Weight>(vv.getPickedEdgeState(), Color.black, Color.red));
        /*vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(vv.getPickedVertexState(), 
                Color.red, Color.yellow));*/
        
		vv.getRenderContext().setVertexFillPaintTransformer(new Function<String,Paint>() {
			public Paint apply(String v) {
				Color color = colors.get(v);
				return color != null ? color : Color.RED;
			}
		});
        vv.setBackground(Color.white);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        
        Function<Weight,String> stringer = new Function<Weight,String>(){
            public String apply(Weight e) {
                if(e.getTypeRelationship() == Weight.TypeRelationship.association)
                	return "A";
                else if(e.getTypeRelationship() == Weight.TypeRelationship.generalization)
                	return "G";
                else if(e.getTypeRelationship() == Weight.TypeRelationship.implementation)
                	return "IM";
                else if(e.getTypeRelationship() == Weight.TypeRelationship.interfacing)
                	return "I";                
                else if(e.getTypeRelationship() == Weight.TypeRelationship.callMethod)
                	return "CM";
                else
                	return "E";
            }
        };
        vv.getRenderContext().setEdgeLabelTransformer(stringer);
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve(this.graph));
        
        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        
        /**
         * the regular graph mouse for the normal view
         */
        final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<Object, Object>();

        vv.setGraphMouse(graphMouse);
        
        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        content.add(gzsp);
        
        JComboBox<?> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        
        JComboBox<?> layoutTypeComboBox = new JComboBox<Object>(layoutClasses);
        layoutTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        layoutTypeComboBox.setSelectedItem(FRLayout.class);
        layoutTypeComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					java.lang.Class<CircleLayout> clazz = (java.lang.Class<CircleLayout>)e.getItem();
					try {
						Layout<String,Weight> layout = getLayoutFor(clazz, graph);
						layout.setInitializer(vv.getGraphLayout());
						clusteringLayout.setDelegate(layout);
						vv.setGraphLayout(clusteringLayout);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}});
        
        JComboBox<?> subLayoutTypeComboBox = new JComboBox<Object>(layoutClasses);
        
        subLayoutTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        subLayoutTypeComboBox.addItemListener(new ItemListener() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					subLayoutType = (java.lang.Class<CircleLayout>)e.getItem();
				}
			}});
        
        JComboBox<?> subLayoutDimensionComboBox = 
        	new JComboBox<Object>(new Dimension[]{
        			new Dimension(75,75),
        			new Dimension(100,100),
        			new Dimension(150,150),
        			new Dimension(200,200),
        			new Dimension(250,250),
        			new Dimension(300,300),
        			new Dimension(3000,3000)
        	}	
        	);
        subLayoutDimensionComboBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('['));
                valueString = valueString.replaceAll("idth", "");
                valueString = valueString.replaceAll("eight","");
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        subLayoutDimensionComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					subLayoutSize = (Dimension)e.getItem();
				}
			}});
        subLayoutDimensionComboBox.setSelectedIndex(1);

        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog((JComponent)e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE);
            }
        });
        
        JButton calculateMetrics = new JButton("Calculate Metrics");
        calculateMetrics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	FileWriter fileWriter = null;
            	try {
            		fileWriter = new FileWriter(config.getPathToComponentsMetricsCSVFile());
            		
            		List<Color> usedColors = new ArrayList<Color>();
            		
            		for(Color color: colors.values()){
            			if(!usedColors.contains(color))
            				usedColors.add(color);
            		}
            		
            		fileWriter.append("Component;csc;component size\n");
            		System.out.println("colors " + usedColors.size());
            		
            		for(Color color : usedColors){
                    	int csc = 0;
                    	int componentSize = 0;
                    	String classes = "";
                    	
                    	for(Class classType : classParser.getClasses().values()){
                    		
                    		if(classType.getTypeClass().equals(Class.TypeClass.business) && classType.getColor().equals(color)){
                    			csc += classType.getInnerClassDependencies().size() * 2;
                    			componentSize++;
                        		classes += classType.getName() + ", ";
                        		
                        		//System.out.println(classType.getName() + " - dam: " + classType.getDAM() + " - dcc: " + classType.getAmountBusinessDependencies() + " - ana: " + classType.getANA() + " - cam: " + classType.getCAM() + " - moa: " + classType.getMOA() + " - mfa: " + classType.getMFA() + " - nop: " + classType.getNOP() + " - cis:" + classType.getCIS());
                    		}
                    	}
                    	
                    	if(csc > 0)
                    		fileWriter.append(classes + ";" + csc + ";" + componentSize + "\n");
                    }
        		} catch (Exception ex) {
        			ex.printStackTrace();
        		} finally{
                    try {
        	            fileWriter.flush();
        	            fileWriter.close();
        	        } catch (IOException iex) {
        	            System.out.println("Error while flushing/closing fileWriter !!!");
        	            iex.printStackTrace();
        	        }
        		}
            	
                
            }
        });
        
        JButton createComponents = new JButton("CreateComponents");
        createComponents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		List<Color> usedColors = new ArrayList<Color>();
        		
        		for(Color color: colors.values()){
        			if(!usedColors.contains(color))
        				usedColors.add(color);
        		}
        		
        		for(Color color : usedColors){
        			for(Class classType : classParser.getClasses().values()){
	            		if(classType.getTypeClass().equals(Class.TypeClass.business) && classType.getColor().equals(color)){
	            			ComponentsCreator.create(classType);
	            		}
        			}
        		}
            }
        });
        
        /*final ColorChooserButton changeColor = new ColorChooserButton(Color.BLACK, "Change Color");
        changeColor.addColorChangedListener(new ColorChangedListener() {
			@Override
			public void colorChanged(Color newColor) {
				changeColorPicked(newColor);
			}});*/
        
        this.vv.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				List<Class> classes = new ArrayList<Class>();
				for(String vertex : ps.getPicked()){
					Color color = colors.get(vertex) == null ? Color.BLACK : colors.get(vertex);
					//changeColor.setSelectedColor(color, false);
					
					for(Class classType : classParser.getClasses().values()){
						if(classType.getColor() != null && classType.getColor().equals(color)){
							classes.add(classType);
						}
					}
				}
				final DendrogramPanel dendrogramPanel = new DendrogramPanel(((DirectedSparseMultigraph<String, Weight>)graph));
				dendrogramPanel.init(classes);
				
				dendrogramPanel.addMouseListener(new MouseListener(){

					@Override
					public void mouseClicked(MouseEvent e) {
						dendrogramPanel.getComponent().setDotRadius(2, true);
						dendrogramPanel.getComponent().setSelected(false, true);
						ClusterComponent clusterComponent = dendrogramPanel.getComponent().getComponent(e.getX(), e.getY());
						
						if (e.getClickCount() == 1 && e.getButton() == 3 && clusterComponent != null && !clusterComponent.getCluster().isLeaf()) {
							dendrogramPanel.getComponent().resetColor();
							clusterComponent.setDotRadius(4);
							clusterComponent.setSelected(true);
							
							clusterComponent.setColor(getColor(), true);
							
							ClusterComponent auxComponent = clusterComponent.getParent();
							while(auxComponent != null){
								auxComponent.setColor(getColor(), true);
								auxComponent = auxComponent.getParent();
							}
						}
						else if(e.getClickCount() == 2 && e.getButton() == 1 && clusterComponent != null){
							changeColorCluster(dendrogramPanel.getComponent());
							
							vv.repaint();
							
							Window w = SwingUtilities.getWindowAncestor(dendrogramPanel);
					        w.setVisible(false);
					        dendrogramPanel.setVisible(false);
						}
						//dendrogramPanel.repaint();
					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
					}

					@Override
					public void mouseExited(MouseEvent e) {
						System.out.println("mouseExited");
						//component.setDotRadius(2);
					}});
		    	
				dendrogramPanel.addMouseMotionListener(new MouseMotionListener(){

					@Override
					public void mouseDragged(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseMoved(MouseEvent e) {
						dendrogramPanel.getComponent().setDotRadius(2, true);
						ClusterComponent clusterComponent = dendrogramPanel.getComponent().getComponent(e.getX(), e.getY());
						if (clusterComponent != null && !clusterComponent.getCluster().isLeaf() && !clusterComponent.isSelected()) 
							clusterComponent.setDotRadius(6);
						dendrogramPanel.repaint();
						
					}});
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
        
        Dimension space = new Dimension(20,20);
        Box controls = Box.createVerticalBox();
        controls.add(Box.createRigidArea(space));
        
        JPanel zoomControls = new JPanel(new GridLayout(1,2));
        zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomControls.add(plus);
        zoomControls.add(minus);
        heightConstrain(zoomControls);
        controls.add(zoomControls);
        controls.add(Box.createRigidArea(space));
        
        JPanel layoutControls = new JPanel(new GridLayout(0,1));
        layoutControls.setBorder(BorderFactory.createTitledBorder("Layout"));
        layoutControls.add(layoutTypeComboBox);
        heightConstrain(layoutControls);
        controls.add(layoutControls);

        JPanel subLayoutControls = new JPanel(new GridLayout(0,1));
        subLayoutControls.setBorder(BorderFactory.createTitledBorder("SubLayout"));
        subLayoutControls.add(subLayoutTypeComboBox);
        subLayoutControls.add(subLayoutDimensionComboBox);
        heightConstrain(subLayoutControls);
        controls.add(subLayoutControls);
        controls.add(Box.createRigidArea(space));
        
        JPanel modePanel = new JPanel(new GridLayout(1,1));
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(modeBox);
        heightConstrain(modePanel);
        controls.add(modePanel);
        controls.add(Box.createRigidArea(space));
        
        JPanel clusterControls = new JPanel(new GridLayout(0,1));
        clusterControls.setBorder(BorderFactory.createTitledBorder("Actions"));
        clusterControls.add(calculateMetrics);
        clusterControls.add(createComponents);
        heightConstrain(clusterControls);
        controls.add(clusterControls);
        controls.add(Box.createRigidArea(space));

        controls.add(help);
        controls.add(Box.createVerticalGlue());
        content.add(controls, BorderLayout.EAST);
    }
    
    public Color getColor(){
    	Random rand = new Random();
    	
    	float r = rand.nextFloat();
    	float g = rand.nextFloat();
    	float b = rand.nextFloat();
    	
    	Color randomColor = new Color(r, g, b);
    	
    	for(Color color : colors.values())
    		if(color.equals(randomColor))
    			randomColor = this.getColor();
    	
    	return randomColor;
    }
    
    private void createGraph(){
    	this.graph = new DirectedSparseMultigraph<String,Weight>();
    	classParser = new ClassParser();
    	
    	for(Class classe : classParser.getClasses().values()){
    		if(classe.isInProject() && !classe.isIgnored()&& classe.getTypeClass().equals(Class.TypeClass.business)){
    			Color color = colors.get(classe.getName());
    			
    			if(color == null){
    				color = this.getColor();
    				//System.out.println("início classe " + classe.getName());
    				this.createGraph(classe, color, new ArrayList<Class>());
    				//System.out.println("término classe " + classe.getName());
    			}
    						
    		}
		}
    	
    	for(Map.Entry<String, Color> entry : colors.entrySet()){
    		checkMultipleDependencies(entry.getKey());
    	}
    	
    }
    
    private void checkMultipleDependencies(String vertex){
	
    	//if vertex has more than 1 incoming association put red
		int countAssociationIncoming = 0;
    	
    	if(((DirectedSparseMultigraph<String, Weight>)graph).getInEdges(vertex) != null){
    		Color lastColor = null;
	    	for(Weight weight : ((DirectedSparseMultigraph<String, Weight>)graph).getInEdges(vertex)){
	    		if(weight.getTypeRelationship().equals(Weight.TypeRelationship.association)){
	    			if(lastColor == null || !lastColor.equals(colors.get(weight.getOrigin()))){
		    			countAssociationIncoming++;
		    			lastColor = colors.get(weight.getOrigin());
	    			}
	    		}
	    	}
    	}
    	
    	if(countAssociationIncoming > 1){
    		//colors.put(vertex, Color.RED);
    		//changeColor(vertex, Color.RED, new ArrayList<String>());
    	}
    }
    
    /*private void changeColor(String vertex, Color color, List<String> calculated){
    	changeColor(vertex, color, calculated, true);
    }
    
    private void changeColor(String vertex, Color color, List<String> calculated, boolean ignoreCustomColor){
    	if(calculated.contains(vertex))
    		return;
    	
    	if(customColors.get(vertex)!= null)
    		return;
    	
    	if(!color.equals(Color.RED))
    		auxColors.put(vertex, color);
    	else
    		auxColors.put(vertex, null);
    	
    	colors.put(vertex, color);
    	classParser.getClassType(vertex).setColor(color);
    	calculated.add(vertex);
    	
    	if(((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertex) != null){
	    	for(Weight weight : ((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertex))
	    		changeColor(weight.getDestination(), color, calculated, ignoreCustomColor);
    	}
    }*/
    
    private void createGraph(Class classType, Color color, List<Class> calculated){
    	//System.out.println("cor " + classType.getName() + " - " + color);
    	//System.out.println("createGraph " + classType.getName());
    	if(calculated.contains(classType)){
    		//System.out.println("contains " + classType.getName());
    		if(!circular.contains(calculated)){
    			calculated.add(classType);
	    		circular.add(calculated);
    		}
    		return;
    	}
    	
    	calculated.add(classType);

		colors.put(classType.getName(), color);
		classType.setColor(color);
		
		this.createVertex(classType.getName());

    	//System.out.println("------" + classe.getNomeQualificado() + (classe.getHerancaClasse() != null ? " extends " + classe.getHerancaClasse().getNomeQualificado() : "") + " v=" + classe.getVariaveis().values().size() + " m=" + classe.getMethods().values().size());
		if(classType.getSuperClass() != null && classType.getSuperClass().isInProject() && !classType.getSuperClass().isIgnored() && classType.getSuperClass().getTypeClass().equals(Class.TypeClass.business)){
			this.createEdge(classType.getName(), classType.getSuperClass().getName(), Weight.TypeRelationship.generalization);
			//colors.put(classType.getInheritage().getName(), color);
			this.createGraph(classType.getSuperClass(), color, calculated);
		}
		
		/*for(Class interfac : classType.getInterfaces()){
			if(interfac.isInProject() && !interfac.isIgnored()){
				this.createEdge(classType.getName(), interfac.getName(), Weight.TypeRelationship.interfacing);
				if(colors.get(interfac.getName()) == null){
					colors.put(interfac.getName(), color);
					interfac.setColor(color);
				}
			}
		}*/
		
		for(Class implementsClass : classType.getImplementClass()){
			this.createEdge(classType.getName(), implementsClass.getName(), Weight.TypeRelationship.implementation);
			this.createGraph(implementsClass, color, calculated);
			
		}
		
		//Map.Entry<String, String> entry : map.entrySet()
		for(Class variable : classType.getVariables()){
			//System.out.println((entry.getValue() != null ? entry.getValue().getNomeQualificado() : entry.getKey().getNomeQualificado()) + "-" + entry.getKey().getQuantidade() + ",");
			if(variable.isInProject() && !variable.isIgnored() && (variable.getTypeClass().equals(Class.TypeClass.business) || variable.isInterface())){
				if(!variable.isInterface()){
					this.createEdge(classType.getName(), variable.getName(), Weight.TypeRelationship.association);
					//colors.put(variable.getName(), color);
					this.createGraph(variable, color, calculated);
				}
				
				for(Class implementsClass : variable.getImplementClass()){
					//this.createEdge(variable.getName(), implementsClass.getName(), Weight.TypeRelationship.implementation);
					this.createEdge(classType.getName(), implementsClass.getName(), Weight.TypeRelationship.association);
					//colors.put(implementsClass.getName(), color);
					this.createGraph(implementsClass, color, calculated);
					
				}
			}
		}
		//System.out.println("");
		
		for(Method method : classType.getMethods().values()){
			//System.out.print("---" + method.getRetorno() + " " + method.getNomeQualificado() + "(");
			
			for(Class variable : method.getInstantiationsType()){
				//System.out.println("CM " + (entry.getValue() != null ? entry.getValue().getNomeQualificado() : entry.getKey().getNomeQualificado()) + "-" + entry.getKey().getQuantidade() + "," + Weight.TypeRelationship.callMethod);
				if(variable.isInProject() && !variable.isIgnored() && (variable.getTypeClass().equals(Class.TypeClass.business) || variable.isInterface())){
					this.createEdge(classType.getName(), variable.getName(), Weight.TypeRelationship.callMethod);
					//colors.put(variable.getName(), color);
					this.createGraph(variable, color, calculated);
					
					for(Class implementsClass : variable.getImplementClass()){
						//this.createEdge(variable.getName(), implementsClass.getName(), Weight.TypeRelationship.implementation);
						this.createEdge(classType.getName(), implementsClass.getName(), Weight.TypeRelationship.association);
						//colors.put(implementsClass.getName(), color);
						this.createGraph(implementsClass, color, calculated);
					}
				}
			}					
		}
		
		/*for(List<Class> list : circular){
			System.out.println("Início");
			for(Class aux : list){
				System.out.println("classe " + aux.getName());
				colors.put(aux.getName(), Color.BLACK);
			}
			System.out.println("Término");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}*/
    }
    
    private void heightConstrain(Component component) {
    	Dimension d = new Dimension(component.getMaximumSize().width,
    			component.getMinimumSize().height);
    	component.setMaximumSize(d);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Layout<String, Weight> getLayoutFor(java.lang.Class<CircleLayout> layoutClass, Graph<String, Weight> graph) throws Exception {
    	Object[] args = new Object[]{graph};
    	Constructor<CircleLayout> constructor = layoutClass.getConstructor(new java.lang.Class[] {Graph.class});
    	return  constructor.newInstance(args);
    }
    
    /*private void clusterPicked() {
    	cluster(true);
    }
    
    private void uncluster() {
    	cluster(false);
    }
    
    private void changeColorPicked(Color color) {
    	Collection<String> picked = ps.getPicked();
    	for(String vertex : picked){
    		changeColor(vertex, color, new ArrayList<String>(), false);
    		
        	//if(((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertex) != null){
    	    	//for(Weight weight : ((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertex)){
    	    		//checkMultipleDependencies(weight.getDestination());
    	    	//}
        	//}
    		List<String> calculated = new ArrayList<String>();
    		calculated.add(vertex);
    		
    		//if class is an interface change the color of the implementation too
    		Class interfaceClass = classParser.getClasses().get(vertex);
    		if(interfaceClass.isInterface() && interfaceClass.getImplementClass().size() > 0){
    			for(Class implementationClass : interfaceClass.getImplementClass()){
    				calculated.add(implementationClass.getName());
    				changeColor(implementationClass.getName(), color, calculated, false);
    			}
    		}
    		
    		//if vertex has more than 1 incoming association put red
    		List<String> vertices = new ArrayList<String>();
    		vertices.addAll(calculated);
    		for(String vertexAux : vertices){
    			if(((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertexAux) != null){
        	    	for(Weight weight : ((DirectedSparseMultigraph<String, Weight>)graph).getOutEdges(vertexAux)){
        	    		int countAssociationIncoming = 0;
        	        	
        	        	if(((DirectedSparseMultigraph<String, Weight>)graph).getInEdges(weight.getDestination()) != null){
        	        		Color lastColor = null;
        	        		String lastInVertex = null;
        	    	    	for(Weight weightAux : ((DirectedSparseMultigraph<String, Weight>)graph).getInEdges(weight.getDestination())){
        	    	    		if(weightAux.getTypeRelationship().equals(Weight.TypeRelationship.association)){
        	    	    			//if(lastColor == null || !lastColor.equals(colors.get(weight.getOrigin()))){
        	    	    			if(lastInVertex == null || !lastInVertex.equals(weightAux.getOrigin())){
        	    		    			countAssociationIncoming++;
        	    		    			lastColor = colors.get(weightAux.getOrigin());
        	    		    			lastInVertex = weightAux.getOrigin();
        	    	    			}
        	    	    			//}
        	    	    		}
        	    	    	}
        	        	}
        	        	
        	        	//System.out.println(vertex + " - " + weight.getDestination() + " - " + countAssociationIncoming);
        	        	
        	        	if(countAssociationIncoming > 1){
        	        		colors.put(weight.getDestination(), Color.RED);
        	        		classParser.getClassType(weight.getDestination()).setColor(color);
        	        		//changeColor(weight.getDestination(), Color.RED, calculated, false);
        	        	}
        	    	}
            	}	
    		}
    	}
    	
    	customColors = auxColors;
    	auxColors = new HashMap<String, Color>();

    	vv.repaint();
    }

    @SuppressWarnings("unchecked")
	private void cluster(boolean state) {
    	if(state == true) {
    		// put the picked vertices into a new sublayout 
    		Collection<String> picked = ps.getPicked();
    		if(picked.size() > 1) {
    			Point2D center = new Point2D.Double();
    			double x = 0;
    			double y = 0;
    			for(String vertex : picked) {
    				Point2D p = clusteringLayout.apply(vertex);
    				x += p.getX();
    				y += p.getY();
    			}
    			x /= picked.size();
    			y /= picked.size();
				center.setLocation(x,y);

    			Graph<String, Weight> subGraph;
    			try {
    				subGraph = graph.getClass().newInstance();
    				for(String vertex : picked) {
    					subGraph.addVertex(vertex);
    					Collection<Weight> incidentEdges = graph.getIncidentEdges(vertex);
    					for(Weight edge : incidentEdges) {
    						Pair<String> endpoints = graph.getEndpoints(edge);
    						if(picked.containsAll(endpoints)) {
    							// put this edge into the subgraph
    							subGraph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
    						}
    					}
    				}

    				Layout<String,Weight> subLayout = getLayoutFor(subLayoutType, subGraph);
    				subLayout.setInitializer(vv.getGraphLayout());
    				subLayout.setSize(subLayoutSize);
    				clusteringLayout.put(subLayout,center);
    				vv.setGraphLayout(clusteringLayout);

    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	} else {
    		// remove all sublayouts
    		this.clusteringLayout.removeAll();
    		vv.setGraphLayout(clusteringLayout);
    	}
    }*/
    
    public void createVertex(String v1){
    	//layout.lock(true);
        //add a vertex
        //Integer v1 = new Integer(g.getVertexCount());

        //Relaxer relaxer = vv.getModel().getRelaxer();
        //relaxer.pause();
        if(!graph.containsVertex(v1))
        	graph.addVertex(v1);
        
        System.err.println("added node " + v1);
    }
    
    public void createEdge(String v_prev, String v1, Weight.TypeRelationship typeRelationship){
        //Relaxer relaxer = vv.getModel().getRelaxer();
        //relaxer.pause();
        // wire it to some edges
        if (v_prev != null) {
        	Weight weight = new Weight(v_prev, v1, typeRelationship);
        	
        	//if(!graph.containsEdge(weight)){
        		graph.addEdge(weight, v_prev, v1);
        	//}
        }

        v_prev = v1;
    }

    public static void main() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new GraphGeneration());
        f.pack();
        f.setVisible(true);
    }

	public Graph<String, Weight> getGraph() {
		return graph;
	}

	public void setGraph(Graph<String, Weight> graph) {
		this.graph = graph;
	}
	
	public void changeColorCluster(ClusterComponent clusterComponent){
		if(clusterComponent.getCluster().isLeaf()){
			this.colors.put(clusterComponent.getCluster().getName(), clusterComponent.getColor());
			Class classType = classParser.getClasses().get(clusterComponent.getCluster().getName());
			
			classType.setColor(clusterComponent.getColor());
			//System.out.println("changeColorCluster " + clusterComponent.getCluster().getName() + " - " + clusterComponent.getColor());
		}
		
		for(ClusterComponent child : clusterComponent.getChildren())
			this.changeColorCluster(child);
	}
	
}
