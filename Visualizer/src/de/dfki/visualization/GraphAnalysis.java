package de.dfki.visualization;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import de.dfki.data.Graph.Edge;

public class GraphAnalysis extends JFrame {

  private GraphVisualization showGraph;

  private JPanel rightPanel;

  public GraphAnalysis() {
    super();
    setTitle("Graph analysis and visulization tool");
    init();
  }

  public void run() {
    setVisible(true);
  }

  private void init() {
    setSize(new Dimension(1024, 768));
    getContentPane().add(makeMainTab());
    showGraph.init();
    //pack();
  }

  private Container makeMainTab() {
    Container pane = new JPanel();
    pane.setLayout(new BorderLayout());

    // Init center graphical panel
    showGraph = new GraphVisualization();
    pane.add(showGraph, BorderLayout.CENTER);

    // init left panel
    rightPanel = new JPanel();
    rightPanel.setLayout(new BorderLayout());
    pane.add(rightPanel, BorderLayout.EAST);
    initControlButtons();
    return pane;
  }

  private void initControlButtons() {
   
    JButton savePath = new JButton("Save path");
    savePath.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        List<Edge> path = new ArrayList<>(showGraph.selectedEdges);

        for (Edge edge : path) {
          System.out.println(edge.toString());
        }
      }
    });

    JButton clearPath = new JButton("Clear path");
    clearPath.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        showGraph.selectedEdges.clear();
      }
    });
    JLabel headline = new JLabel("Paths");
    JList<String> test = new JList<>(new String[] { "Path1", "Path2", "Path3" });
 
    JPanel pathPanel = new JPanel();
    GroupLayout layout = new GroupLayout(pathPanel);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(headline, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(test, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(savePath)
                .addComponent(clearPath))));
    
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(headline)
        .addComponent(test)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(savePath)
            .addComponent(clearPath)));
    
    pathPanel.setLayout(layout);
    rightPanel.add(pathPanel, BorderLayout.CENTER);
  }
}
