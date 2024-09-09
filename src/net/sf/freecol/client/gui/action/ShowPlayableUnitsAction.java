package net.sf.freecol.client.gui.action;

import java.awt.event.ActionEvent;

import net.sf.freecol.client.FreeColClient;

public class ShowPlayableUnitsAction extends FreeColAction{

    public static final String id = "showPlayableUnitsAction";


    /**
     * Creates a new {@code OpenPlayableUnitsPanelAction}.
     *
     * @param freeColClient The {@code FreeColClient} for the game.
     */
    public ShowPlayableUnitsAction(FreeColClient freeColClient) {
        super(freeColClient, id);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        igc().showPlayableUnits();
    }

}
