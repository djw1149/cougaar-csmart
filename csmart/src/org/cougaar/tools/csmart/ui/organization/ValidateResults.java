package org.cougaar.tools.csmart.ui.organization;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: travers
 * Date: Apr 8, 2003
 * Time: 10:22:41 PM
 * To change this template use Options | File Templates.
 */
public class ValidateResults extends JDialog {

  public ValidateResults(ArrayList errorAgentNames, ArrayList providersNeeded, ArrayList echelonsNeeded,
                         ArrayList successAgentNames, ArrayList providerTypes, ArrayList providers) {
    setTitle("Service Discovery Validation");
    JTable errorTable = null;
    JTable successTable = null;
    if (errorAgentNames != null) {
      Vector columnNames = new Vector(3);
      columnNames.add("Agent");
      columnNames.add("Provider Needed");
      columnNames.add("Echelon Needed");
      Vector rowData = new Vector(errorAgentNames.size());
      for (int i = 0; i < errorAgentNames.size(); i++) {
        Vector row = new Vector(2);
        row.add(errorAgentNames.get(i));
        row.add(providersNeeded.get(i));
        row.add(echelonsNeeded.get(i));
        rowData.add(row);
      }
      errorTable = new JTable(rowData, columnNames);
      for (int i = 0; i < 2; i++)
        errorTable.getColumnModel().getColumn(i).setPreferredWidth(150);
      errorTable.revalidate();
    }
    if (successAgentNames != null) {
      Vector columnNames = new Vector(3);
      columnNames.add("Agent");
      columnNames.add("Provider Type");
      columnNames.add("Provider");
      Vector rowData = new Vector(successAgentNames.size());
      for (int i = 0; i < successAgentNames.size(); i++) {
        Vector row = new Vector(3);
        row.add(successAgentNames.get(i));
        row.add(providerTypes.get(i));
        row.add(providers.get(i));
        rowData.add(row);
      }
      successTable = new JTable(rowData, columnNames);
      for (int i = 0; i < 3; i++)
        successTable.getColumnModel().getColumn(i).setPreferredWidth(150);
      successTable.revalidate();
    }
    JPanel panel = new JPanel(new BorderLayout());
    JScrollPane top = new JScrollPane(errorTable);
    JScrollPane bottom = new JScrollPane(successTable);
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(top);
    splitPane.setBottomComponent(bottom);
    panel.add(splitPane, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    JButton dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(dismissButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(panel);
    pack();
    if (errorTable.getModel().getRowCount() != 0)
      splitPane.setDividerLocation(200);
    setSize(600, 400);
    setVisible(true);
  }
}
