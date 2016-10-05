package allow.adaptation.test;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class UITest {

	private final int hGap = 5;
	private final int vGap = 5;

	private String[] borderConstraints = { BorderLayout.PAGE_START,
			BorderLayout.LINE_START, BorderLayout.CENTER,
			BorderLayout.LINE_END, BorderLayout.PAGE_END };

	private JButton[] buttons;

	private GridBagConstraints gbc;

	private JPanel borderPanel;
	private JPanel flowPanel;
	private JPanel gridPanel;
	private JPanel gridBagPanel;
	private JPanel cardPanel;

	public UITest() {
		buttons = new JButton[16];
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.insets = new Insets(hGap, vGap, hGap, vGap);
	}

	private void displayGUI() {
		JFrame frame = new JFrame("Layout Example");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel contentPane = new JPanel(new GridLayout(0, 1, hGap, vGap));
		contentPane.setBorder(BorderFactory.createEmptyBorder(hGap, vGap, hGap,
				vGap));
		// borderPanel = new JPanel(new BorderLayout(hGap, vGap));
		// borderPanel.setBorder(BorderFactory.createTitledBorder("BorderLayout"));
		// borderPanel.setOpaque(true);
		// borderPanel.setBackground(Color.WHITE);
		// for (int i = 0; i < 5; i++) {
		// buttons[i] = new JButton(borderConstraints[i]);
		// borderPanel.add(buttons[i], borderConstraints[i]);
		// }
		// contentPane.add(borderPanel);

		// flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hGap,
		// vGap));
		// flowPanel.setBorder(BorderFactory.createTitledBorder("FlowLayout"));
		// flowPanel.setOpaque(true);
		// flowPanel.setBackground(Color.WHITE);
		// for (int i = 5; i < 8; i++) {
		// buttons[i] = new JButton(Integer.toString(i));
		// flowPanel.add(buttons[i]);
		// }
		// contentPane.add(flowPanel);

		gridPanel = new JPanel(new GridLayout(2, 2, hGap, vGap));
		gridPanel.setBorder(BorderFactory.createTitledBorder("GridLayout"));
		gridPanel.setOpaque(true);
		gridPanel.setBackground(Color.WHITE);
		for (int i = 8; i < 12; i++) {
			buttons[i] = new JButton(Integer.toString(i));
			gridPanel.add(buttons[i]);
		}
		contentPane.add(gridPanel);

		frame.setContentPane(contentPane);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	private JPanel getPanel(Color bColor) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, hGap, vGap));
		panel.setOpaque(true);
		panel.setBackground(bColor.darker().darker());
		JButton swapperButton = new JButton("Next");
		swapperButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
				cardLayout.next(cardPanel);
			}
		});

		panel.add(swapperButton);

		return panel;
	}

	private void addComp(JPanel panel, JComponent comp, int x, int y,
			int gWidth, int gHeight, int fill, double weightx, double weighty) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = gWidth;
		gbc.gridheight = gHeight;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;

		panel.add(comp, gbc);
	}

	public static void main(String[] args) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				new UITest().displayGUI();
			}
		};
		EventQueue.invokeLater(runnable);
	}
}