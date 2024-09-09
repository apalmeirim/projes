package net.sf.freecol.client.gui.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.PanelUI;

import net.miginfocom.swing.MigLayout;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.plaf.FreeColSelectedPanelUI;
import net.sf.freecol.common.i18n.Messages;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Unit;



public class PlayableUnitsPanel extends FreeColPanel {

    private static final Logger logger = Logger.getLogger(PlayableUnitsPanel.class.getName());

    public static class UnitWrapper {

        public final Unit unit;
        public final String name;
        public final String location;


        public UnitWrapper(Unit unit) {
            this.unit = unit;
            this.name = unit.getDescription(Unit.UnitLabelType.NATIONAL);
            this.location = Messages.message(unit.getLocation()
                    .getLocationLabelFor(unit.getOwner()));
        }


        // Override Object

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return name;
        }
    }



    private class UnitCellRenderer implements ListCellRenderer<UnitWrapper> {

        public UnitCellRenderer() {

        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends UnitWrapper> list,
                                                      UnitWrapper value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            final JLabel imageLabel = new JLabel();
            imageLabel.setIcon(new ImageIcon(getImageLibrary().getSmallerUnitImage(value.unit)));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            final JLabel nameLabel = new JLabel();
            nameLabel.setText(value.name);

            final JLabel locationLabel = new JLabel();
            locationLabel.setFont(locationLabel.getFont().deriveFont(Font.ITALIC));
            locationLabel.setText(value.location);

            final JPanel panel;
            if (isSelected) {
                panel = new MigPanel(new MigLayout("", "[fill]"));
                panel.setOpaque(false);
                panel.setUI((PanelUI)FreeColSelectedPanelUI.createUI(panel));
            } else {
                panel = new MigPanel(new MigLayout("", "[fill]"));
                panel.setOpaque(false);
            }

            final Dimension largestIconSize = largestIconSize(list);
            panel.add(imageLabel, "center, width " + largestIconSize.width + "px!, height " + largestIconSize.height + "px!");
            panel.add(nameLabel, "split 2, flowy, grow");
            panel.add(locationLabel, "grow");

            return panel;
        }

        private Dimension largestIconSize(JList<? extends UnitWrapper> list) {
            final ListModel<? extends UnitWrapper> model = list.getModel();
            int largestWidth = 0;
            int largestHeight = 0;
            for (int i=0; i<model.getSize(); i++) {
                final UnitWrapper value = model.getElementAt(i);
                final BufferedImage image = getImageLibrary().getSmallerUnitImage(value.unit);
                if (image.getWidth() > largestWidth) {
                    largestWidth = image.getWidth();
                }
                if (image.getHeight() > largestHeight) {
                    largestHeight = image.getHeight();
                }
            }
            return new Dimension(largestWidth, largestHeight);
        }
    }

    /** The list of units to display. */
    private final JList<UnitWrapper> unitList;


    public PlayableUnitsPanel(FreeColClient freeColClient, List<Unit> units) {
        super(freeColClient, null, new BorderLayout());

        final Player player = getMyPlayer();

        // Title
        JLabel header = Utility.localizedHeader("Units",
                Utility.FONTSPEC_TITLE);

        DefaultListModel<PlayableUnitsPanel.UnitWrapper> model = new DefaultListModel<>();
        for (Unit unit : units) {
            model.addElement(new PlayableUnitsPanel.UnitWrapper(unit));
        }

        this.unitList = new JList<>(model);
        this.unitList.setCellRenderer(new PlayableUnitsPanel.UnitCellRenderer());
        this.unitList.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "select");
        this.unitList.getActionMap().put("select", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                selectUnit();
            }
        });
        this.unitList.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "quit");
        this.unitList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                selectUnit();
        }
        });
        JScrollPane listScroller = new JScrollPane(this.unitList);

        JPanel panel = new MigPanel(new MigLayout("wrap 1, fill",
                "[align center]"));
        panel.add(header);
        panel.add(listScroller, "newline 10");
        panel.setSize(panel.getPreferredSize());

        add(okButton, BorderLayout.PAGE_END);

        setEscapeAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                okButton.doClick();
            }
        });

        add(panel, BorderLayout.CENTER);
    }


    /**
     * Select the current unit in the list.
     */
    private void selectUnit() {
        UnitWrapper wrapper = this.unitList.getSelectedValue();
        if (wrapper != null && wrapper.unit != null) {
            if (wrapper.unit.isInEurope()) {
                getGUI().showEuropePanel();
            } else {
                getGUI().changeView(wrapper.unit, false);
                if (wrapper.unit.getColony() != null) {
                    getGUI().showColonyPanel(wrapper.unit.getColony(),
                            wrapper.unit);
                }
            }
        }

    }
}
