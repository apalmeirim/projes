package net.sf.freecol.client.gui.panel;

import java.awt.*;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.ImageLibrary;
import net.sf.freecol.client.gui.label.MarketLabel;
import net.sf.freecol.client.gui.panel.*;
import net.sf.freecol.client.gui.panel.report.ReportPanel;
import net.sf.freecol.common.debug.FreeColDebugger;
import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.*;
import net.sf.freecol.common.model.FoundingFather.FoundingFatherType;

import static net.sf.freecol.common.util.CollectionUtils.*;

public final class GameStatisticsPanel extends ReportPanel {

    private final List<Colony> colonies;

    /**
     * The constructor that will add the items to this panel.
     *
     * @param freeColClient The {@code FreeColClient} for the game.
     */
    public GameStatisticsPanel(FreeColClient freeColClient) {
        super(freeColClient, "GameStatistics");

        final Player player = getMyPlayer();

        this.colonies = player.getColonyList();

        JPanel goodsHeader = new MigPanel("GameStatisticsPanelUI");
        goodsHeader.setBorder(new EmptyBorder(20, 20, 0, 20));
        scrollPane.setColumnHeaderView(goodsHeader);

        final Specification spec = getSpecification();
        List<GoodsType> storableGoods = spec.getStorableGoodsTypeList();
        Market market = player.getMarket();

        // Display Panel
        reportPanel.removeAll();
        goodsHeader.removeAll();

        String layoutConstraints = "insets 0, gap 0 0";
        String columnConstraints = "[25%!, fill]["
                + (int)Math.round(ImageLibrary.ICON_SIZE.width * 1.25)
                + "!, fill]";
        String rowConstraints = "[fill]";

        reportPanel.setLayout(new MigLayout(layoutConstraints,
                columnConstraints, rowConstraints));
        goodsHeader.setLayout(new MigLayout(layoutConstraints,
                columnConstraints, rowConstraints));
        goodsHeader.setOpaque(true);

        JLabel emptyLabel = createLeftLabel("Good Type");
        emptyLabel.setBorder(Utility.getTopLeftCellBorder());
        goodsHeader.add(emptyLabel, "cell 0 0");

        JLabel jl = createLeftLabel("report.trade.unitsSold");
        jl.setBorder(Utility.getTopLeftCellBorder());
        JLabel jl2 = createLeftLabel("report.trade.totalUnits");
        jl2.setBorder(Utility.getTopLeftCellBorder());
        reportPanel.add(jl2, "cell 0 0");
        reportPanel.add(createLeftLabel("Score: " + player.getScore() + "; Gold: " + player.getGold()), "cell 0 1");

        TypeCountMap<GoodsType> totalUnits = new TypeCountMap<>();
        TypeCountMap<GoodsType> deltaUnits = new TypeCountMap<>();
        TypeCountMap<GoodsType> cargoUnits = new TypeCountMap<>();

        for (Unit unit : transform(player.getUnits(), Unit::isCarrier)) {
            for (Goods goods : unit.getCompactGoodsList()) {
                cargoUnits.incrementCount(goods.getType(), goods.getAmount());
                totalUnits.incrementCount(goods.getType(), goods.getAmount());
            }
        }

        int column = 0;
        for (GoodsType goodsType : storableGoods) {
            column++;
            int sales = player.getSales(goodsType);
            goodsHeader.add(new MarketLabel(freeColClient, goodsType, market)
                    .addBorder());

            jl = createNumberLabel(sales);
            jl.setBorder(Utility.getTopCellBorder());
            reportPanel.add(jl, "cell " + column + " 0");
        }

        int row = 6;
        boolean first = true;
        for (Colony colony : colonies) {
            for (GoodsType goodsType : getSpecification().getGoodsTypeList()) {
                deltaUnits.incrementCount(goodsType, colony.getNetProductionOf(goodsType));
            }
            for (Goods goods : colony.getCompactGoodsList()) {
                totalUnits.incrementCount(goods.getType(), goods.getAmount());
            }
            JButton colonyButton = createColonyButton(colony);
            if (colony.hasAbility(Ability.EXPORT)) {
                colonyButton.setText(colonyButton.getText() + "*");
            }
            colonyButton.setBorder((first) ? Utility.getTopLeftCellBorder()
                    : Utility.getLeftCellBorder());
            reportPanel.add(colonyButton, "cell 0 " + row + " 1 2");

            column = 0;

            for (GoodsType goodsType : storableGoods) {
                column++;
                reportPanel.add(createNumberLabel(totalUnits.getCount(goodsType)),
                        "cell " + column + " 4");
            }
        }
    }

    private JLabel createLeftLabel(String key) {
        JLabel result = Utility.localizedLabel(key);
        result.setBorder(Utility.getLeftCellBorder());
        return result;
    }

    private JLabel createNumberLabel(int value) {
        return createNumberLabel(value, false);
    }

    private JLabel createNumberLabel(int value, boolean alwaysAddSign) {
        JLabel result = new JLabel(String.valueOf(value), JLabel.TRAILING);
        result.setBorder(Utility.getCellBorder());
        if (value < 0) {
            result.setForeground(Color.RED);
        } else if (alwaysAddSign && value > 0) {
            result.setText("+" + value);
        }
        return result;
    }
}

