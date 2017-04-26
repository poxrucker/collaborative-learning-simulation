package de.dfki.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GraphAnalysis extends JFrame {

  private static final long serialVersionUID = 1L;

  private GraphVisualizationApplet showGraph;

  private Container appletPanel;
  private Container controlPanel;
  private Container layerPanel;
  private Container optionsPanel;
  private Container currentOptionsPanel;
  private Container mapOptionsPanel;
  private Container graphOptionsPanel;
  private Container pathOptionsPanel;

  
  public GraphAnalysis() {
    super();
    setTitle("Graph analysis and visualization tool");
    init();
  }

  public void run() {
    setVisible(true);
  }

  private void init() {
    setSize(new Dimension(1024, 768));
    getContentPane().add(createMainPanel());
    showGraph.init();
  }

  private Container createMainPanel() {    
    // Initialize center graphical panel showing the graph
    appletPanel = new JPanel();
    appletPanel.setLayout(new BorderLayout());
    showGraph = new GraphVisualizationApplet();
    appletPanel.add(showGraph);

    // Initialize control panel
    controlPanel = createControlPanel();
 
    // Add both panels to split pane
    JSplitPane ret = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, appletPanel, controlPanel);
    ret.setOneTouchExpandable(true);
    ret.setDividerLocation(getWidth() / 4 * 3);
    ret.setResizeWeight(1.0);
    
    //Provide minimum sizes for the two components in the split pane
    Dimension minimumSize = new Dimension(250, 50);
    appletPanel.setMinimumSize(minimumSize);
    controlPanel.setMinimumSize(minimumSize);
    return ret;
  }
  
  private Container createControlPanel() {
    JPanel ret = new JPanel();
    optionsPanel = createOptionsPanel();
    layerPanel = createLayerPanel();
    
    // Create layout and add controls
    GroupLayout layout = new GroupLayout(ret);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(layerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(optionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(layerPanel)
        .addComponent(optionsPanel));
    ret.setLayout(layout);
    return ret;
  }
  
  private AbstractListModel<JCheckBox> createInitialListModel() {
    DefaultListModel<JCheckBox> model = new DefaultListModel<>();
    final JCheckBox map = new JCheckBox("Map");
    map.setSelected(true);
    map.addItemListener(new ItemListener() {
      
      @Override
      public void itemStateChanged(ItemEvent e) {
        showGraph.toggleShowMap();
      }
    });
    
    final JCheckBox graph = new JCheckBox("Graph");
    graph.setSelected(false);
    graph.addItemListener(new ItemListener() {
      
      @Override
      public void itemStateChanged(ItemEvent e) {
        showGraph.toggleShowGraph(); 
      }
    });
    
    final JCheckBox dataSet = new JCheckBox("Dataset");
    dataSet.setSelected(false);
    dataSet.addItemListener(new ItemListener() {
      
      @Override
      public void itemStateChanged(ItemEvent e) {
        showGraph.toggleShowDataset(); 
      }
    });
    
    final JCheckBox paths = new JCheckBox("Paths");
    paths.setSelected(false);
    paths.addItemListener(new ItemListener() {
      
      @Override
      public void itemStateChanged(ItemEvent e) {
        
      }
    });
    model.addElement(map);
    model.addElement(graph);
    model.addElement(dataSet);
    model.addElement(paths);
    return model;
  }
  
  private Container createLayerPanel() {
    // Panel to return
    JPanel ret = new JPanel();
    
    // Headline label
    JLabel headline = new JLabel("Layer");
    
    // Layer selection checkbox list
    CheckBoxList temp = new CheckBoxList(createInitialListModel());
    temp.setMinimumSize(new Dimension(100, 100));
    temp.addListSelectionListener(new ListSelectionListener() {
      
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
          return;
                
        switch(temp.getSelectedIndex()) {
        case 0:
          currentOptionsPanel.removeAll();
          currentOptionsPanel.add(mapOptionsPanel);
          currentOptionsPanel.revalidate();
          break;
        case 1:
          currentOptionsPanel.removeAll();
          currentOptionsPanel.add(graphOptionsPanel);
          currentOptionsPanel.revalidate();
          break;
        case 2:
          currentOptionsPanel.removeAll();
          currentOptionsPanel.add(pathOptionsPanel);
          currentOptionsPanel.revalidate();
          break;
        }
        temp.previousIndex = temp.getSelectedIndex();

      }
    });
    // Add layer button
    JButton btnAddLayer = new JButton("Add");
    btnAddLayer.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        
      }
    });
    
    // Remove layer button
    JButton btnRemoveLayer = new JButton("Remove");
    btnRemoveLayer.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        
      }
    });
    
    // Create layout and add controls
    GroupLayout layout = new GroupLayout(ret);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(headline, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(temp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnAddLayer)
                .addComponent(btnRemoveLayer))));
    
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(headline)
        .addComponent(temp)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(btnAddLayer)
            .addComponent(btnRemoveLayer)));
    layout.linkSize(btnAddLayer, btnRemoveLayer);
    ret.setLayout(layout);
    temp.setSelectedIndex(0);
    return ret;
  }
  
  private Container createOptionsPanel() {
    Container ret = new JPanel();
    JLabel headline = new JLabel("Options");
    
    // Setup option panels
    currentOptionsPanel = new JPanel();
    mapOptionsPanel = createMapOptionsPanel();
    graphOptionsPanel = createGraphOptionsPanel();
    pathOptionsPanel = createPathOptionsPanel();
    currentOptionsPanel.setSize(new Dimension(100, 100));

    // Create layout and add controls
    GroupLayout layout = new GroupLayout(ret);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(headline, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(currentOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(headline)
        .addComponent(currentOptionsPanel));
    ret.setLayout(layout);
    return ret;
  }
  
  private Container createMapOptionsPanel() {
    Container ret = new JPanel();
    return ret;
  }
  
  private Container createGraphOptionsPanel() {
    Container ret = new JPanel();
    
    // Vertex configuration
    JLabel vertexHeadline = new JLabel("Vertices");
    JCheckBox showVertices = new JCheckBox("Show vertices");
    showVertices.setSelected(false);
    showVertices.addItemListener(new ItemListener() {
      
      @Override
      public void itemStateChanged(ItemEvent e) {
        showGraph.toggleShowVertices();
      }
    });
    JLabel lblVertexColor = new JLabel("Vertex color:");
    JComboBox<String> colors = new JComboBox<>(new String[] { "Black", "Gray", "Dark gray", "Light gray" }); 
    colors.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String c = (String)cb.getSelectedItem();
        Color color = Color.BLACK;
        
        switch (c) {
        case "Black":
          color = Color.BLACK;
          break;
        case "Gray":
          color = Color.GRAY;
          break;
        case "Dark gray":
          color = Color.DARK_GRAY;
          break;
        case "Light gray":
          color = Color.LIGHT_GRAY;
          break;
        }
        showGraph.setVertexColor(color.getRGB());  
      }
    });
    
    GroupLayout layout = new GroupLayout(ret);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(vertexHeadline, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(showVertices, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(layout.createSequentialGroup()
            .addComponent(lblVertexColor)
            .addComponent(colors)));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(vertexHeadline)
        .addComponent(showVertices)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(lblVertexColor)
            .addComponent(colors)));
    ret.setLayout(layout);
    return ret;
  }
  
  private ListModel<PathView> pathViewModel = new DefaultListModel<>();
  
  private Container createPathOptionsPanel() {
    Container ret = new JPanel();
    JLabel lblPaths = new JLabel("Paths");
    JList<PathView> lbPaths = new JList<>(pathViewModel);
    return ret;
  }
}
