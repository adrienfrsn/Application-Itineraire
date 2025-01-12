package src;

import java.util.ArrayList;
import java.util.List;

import fr.ulille.but.sae_s2_2024.Chemin;
import src.exception.CheminInexistantException;

public class VoyageurIHM extends VoyageurCorrespondance {

    private PlateformeCorrespondance g;

    public VoyageurIHM(String username, TypeCout typeCout) {
        super(username, typeCout);
        g = ToolsCorrespondance.initPlateforme(getDATA(), getCORRESPONDANCE());
    }

    public VoyageurIHM(String username, TypeCout typeCout, ArrayList<String> csvData, ArrayList<String> csvCorrespondance) {
        super(username, typeCout, csvData, csvCorrespondance);
        g = ToolsCorrespondance.initPlateforme(csvData, csvCorrespondance);
    }

    @Override
    public List<Chemin> computeBestPathTrigger() throws CheminInexistantException {
        if (ToolsCorrespondance.donneesValides(this.getDATA())) {
            if (g.hasPathByModalite(this.depart, this.arrivee, this.modalite)) {

                List<Chemin> chemins = g.getPathByModaliteAndTypeCout(this.depart, this.arrivee, this.modalite,
                        this.critere, this.nb_trajet * 100);
                chemins = ToolsCorrespondance.removeDuplicates(chemins, this.nb_trajet);
                for (TypeCout c : new TypeCout[] { TypeCout.PRIX, TypeCout.CO2, TypeCout.TEMPS }) {
                    switch (c) {
                        case PRIX:
                            Tools.applyThreshold(g, chemins, c, this.thresholdPrix);
                            break;
                        case CO2:
                            Tools.applyThreshold(g, chemins, c, this.thresholdCO2);
                            break;
                        case TEMPS:
                            Tools.applyThreshold(g, chemins, c, this.thresholdTemps);
                            break;
                    }
                }
                if (chemins.isEmpty()) {
                    throw new CheminInexistantException();
                }
                return chemins;
            } else {

                throw new CheminInexistantException();
            }
        } else {
            System.out.println("Données invalides");
            return null;
        }

    }

    public PlateformeCorrespondance getPlateforme() {
        return g;
    }
}
