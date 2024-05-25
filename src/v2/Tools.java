package src.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fr.ulille.but.sae_s2_2024.ModaliteTransport;
import fr.ulille.but.sae_s2_2024.Chemin;
import fr.ulille.but.sae_s2_2024.Lieu;

/**
 * Classe contenant des outils pour l'application
 */
public class Tools {
    private static final String SEPARATOR = ";";

    private static final int DEPART_IDX = 0;
    private static final int DESTINATION_IDX = 1;
    private static final int MODALITE_IDX = 2;
    private static final int PRIX_IDX = 3;
    private static final int CO2_IDX = 4;
    private static final int TEMPS_IDX = 5;

    public static void main (String[] args) {

        ArrayList<String> data = Tools.getCSV(Voyageur.path);

        String depart, destination ;
        ModaliteTransport modalite;
        double prix, pollution;
        int Duree;

        int thresholdPrix = Integer.MAX_VALUE, thresholdDuree = Integer.MAX_VALUE;
        double thresholdPollution = Double.MAX_VALUE;

        if (donneesValides(data)) {

            Plateforme g = initPlateforme(data);
            
            ModaliteTransport moyen = getModaliteTransport();

            Lieu dep = getLieuDepart(g);

            Lieu dest = getLieuDestination(g, dep);

            if (g.hasPathByModalite(dep.toString(), dest.toString(), moyen)) {
                
                List<TypeCout> criteres = new ArrayList<>();
                criteres.add(TypeCout.PRIX);
                criteres.add(TypeCout.CO2);
                criteres.add(TypeCout.TEMPS);
                
                TypeCout critere = getTypeCout();
                criteres.remove(critere);

                for (TypeCout c : criteres) {
                    switch (c) {
                        case PRIX:
                            thresholdPrix = getThresholdPrix();
                            break;
                        case CO2:
                            thresholdPollution = getThresholdPollution();
                            break;
                        case TEMPS:
                            thresholdDuree = getThresholdDuree();
                            break;
                    }
                }

                

                int nb_trajet = getNbTrajet();


                List<Chemin> chemins = g.getPathByModaliteAndTypeCout(dep.toString(), dest.toString(), moyen, critere, nb_trajet);
                for (TypeCout c : criteres) {
                    switch (c) {
                        case PRIX:
                            applyThreshold(g, chemins, c, thresholdPrix);
                            break;
                        case CO2:
                            applyThreshold(g, chemins, c, thresholdPollution);
                            break;
                        case TEMPS:
                            applyThreshold(g, chemins, c, thresholdDuree);
                            break;
                    }
                }

                if (chemins.size() == 0) {
                    System.out.println("Il n'y a pas de chemin disponible pour les critères que vous avez choisi");
                }
                else {
                    System.out.println("Les trajets recommandés de " + dep + " à " + dest + " sont:");
                    for (int i = 0; i < chemins.size(); i++) {
                        System.out.println(i + 1 + ") " + cheminWithCorre(chemins.get(i), critere));
                    }
                }
                
            }
            else {
                System.out.println("Il n'y a pas de chemin disponible pour le moyen de transport que vous avez choisi");
            }
            
            

            /*List<Chemin> chemins = AlgorithmeKPCC.kpcc(g, new LieuImpl("villeA"), new LieuImpl("villeD"), 3);
            System.out.println("Les 3 trajets recommandés de A à D sont:");
            for (int i = 0; i < chemins.size(); i++) {
                if (chemins.get(i).aretes().get(1).getModalite() == ModaliteTransport.TRAIN) {
                    System.out.println(i + 1 + ") " + chemins.get(i).aretes().get(0).getModalite() + " de " +
                            chemins.get(i).aretes().get(0).getDepart() + " à " + chemins.get(i).aretes().get(0).getArrivee() +
                            " en passant par " + chemins.get(i).aretes().get(1).getDepart() +
                            ". Durée : " + (int) chemins.get(i).poids() + " minutes");
                } else {
                    System.out.println(i + 1 + ") " + chemins.get(i).aretes().get(0).getModalite() + " de " +
                            chemins.get(i).aretes().get(0).getDepart() + " à " + chemins.get(i).aretes().get(0).getArrivee() +
                            " puis " + chemins.get(i).aretes().get(1).getModalite() + " de " +
                            chemins.get(i).aretes().get(1).getDepart() + " à " + chemins.get(i).aretes().get(1).getArrivee() +
                            ". Durée : " + (int) chemins.get(i).poids() + " minutes");
                }
            }*/
        }
    }

    /**
     * Initialise la plateforme avec les données passées en paramètre
     * @param args Données
     * @return Plateforme
     */
    public static Plateforme initPlateforme(ArrayList<String> args) {
        Plateforme g = new Plateforme();
        for (String arg : args) {
            String[] elements = arg.split(SEPARATOR);
            
            String depart = elements[DEPART_IDX];
            String destination = elements[DESTINATION_IDX];
            ModaliteTransport modalite = ModaliteTransport.valueOf(elements[MODALITE_IDX].toUpperCase());
            double prix = Double.parseDouble(elements[PRIX_IDX]);
            double pollution = Double.parseDouble(elements[CO2_IDX]);
            int Duree = Integer.parseInt(elements[TEMPS_IDX]);


            g.ajouterArrete(depart, destination, modalite, prix, pollution, Duree);
            g.ajouterArrete(destination, depart, modalite, prix, pollution, Duree);
        }
        return g;

    }
    
    /**
     * Vérifie si une chaîne de caractères est un nombre
     * @param chaine Chaîne de caractères
     * @return Vrai si la chaîne est un nombre, faux sinon
     */
    public static boolean estNombre(String chaine) {
        try {
            Double.parseDouble(chaine);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Vérifie si les données passées en paramètre sont valides
     * @param args Données
     * @return Vrai si les données sont valides, faux sinon
     */
    public static boolean donneesValides(ArrayList<String> args) {
        if (args.size() == 0) {
            return false;
        }
        for (String arg : args) {
            String[] elements = arg.split(";");
            if (elements.length != 6 || !estNombre(elements[3]) || !estNombre(elements[4]) || !estNombre(elements[5]) || 
            elements[0].length() == 0 || elements[1].length() == 0 || elements[2].length() == 0 || elements[3].length() == 0 || elements[4].length() == 0 || elements[5].length() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Récupère une chaîne de caractères entrée par l'utilisateur
     * @return Chaîne de caractères
     */
    public static String getUserInuput() {
        Scanner scanner = new Scanner(System.in);
        String r = scanner.next();
        scanner.close();
        return r;
        
    }

    /**
     * Récupère le moyen de transport entré par l'utilisateur
     * @return Moyen de transport
     */
    public static ModaliteTransport getModaliteTransport() {
        System.out.println("Quel moyen de transport voulez-vous utiliser?");
        for (ModaliteTransport m : ModaliteTransport.values()) {
            System.out.println("\t- " + m);
        }
        //System.out.println("\t- Tous");
        System.out.println("Entrez le moyen de transport:");
        String moyen = getUserInuput();
        if (moyen.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getModaliteTransport();
        }
        // if (moyen.toLowerCase().equals("tous")) {
        //     return null;
        // }
        try {
            return ModaliteTransport.valueOf(moyen.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Le moyen de transport que vous avez entré n'existe pas");
            return getModaliteTransport();
        }
    }

    /**
     * Récupère le lieu de départ entré par l'utilisateur
     * @param g Plateforme
     * @return Lieu de départ
     */
    public static Lieu getLieuDepart(Plateforme g) {
        System.out.println("Ville de départ disponible:");
        for (Lieu l : g.getLieux()) {
            System.out.println("\t- " + l);
        }
        System.out.println("Entrez la ville de départ:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getLieuDepart(g);
        }
        if (!Plateforme.contientLieux(g.getG1(), r)) {
            System.out.println("La ville que vous avez entré n'existe pas");
            return getLieuDepart(g);
        }
        return new LieuImpl(r);
    }

    /**
     * Récupère le lieu de destination entré par l'utilisateur
     * @param g Plateforme
     * @param depart Lieu de départ
     * @return Lieu de destination
     */
    public static Lieu getLieuDestination(Plateforme g, Lieu depart) {
        System.out.println("Ville de destination disponible:");
        for (Lieu l : g.getLieux(depart))  {
            System.out.println("\t- " + l);
        }
        System.out.println("Entrez la ville de destination:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getLieuDestination(g, depart);
        }
        if (!Plateforme.contientLieux(g.getG1(), r)) {
            System.out.println("La ville que vous avez entré n'existe pas");
            return getLieuDestination(g, depart);
        }
        return new LieuImpl(r);
    }

    /**
     * Récupère le critère entré par l'utilisateur
     * @return Critère
     */
    public static TypeCout getTypeCout() {
        System.out.println("TypeCout disponible:");
        for (TypeCout l : TypeCout.values()) {
            System.out.println("\t- " + l);
        }
        System.out.println("Entrez le critere:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getTypeCout();
        }
        try {
            return TypeCout.valueOf(r.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Le critere que vous avez entré n'existe pas");
            return getTypeCout();
        }
    }

    /**
     * Récupère le nombre de trajets entré par l'utilisateur
     * @return Nombre de trajets
     */
    public static int getNbTrajet() {
        System.out.println("Combien de trajet voulez-vous?");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getNbTrajet();
        }
        if (!estNombre(r)) {
            System.out.println("Vous devez entrer un nombre");
            return getNbTrajet();
        }
        return Integer.parseInt(r);
    }

    /**
     * Récupère le seuil de prix entré par l'utilisateur
     * @return Seuil de prix
     */
    public static int getThresholdPrix() {
        System.out.println("Entrez le prix maximum que vous êtes prêt à payer:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getThresholdPrix();
        }
        if (!estNombre(r)) {
            System.out.println("Vous devez entrer un nombre");
            return getThresholdPrix();
        }
        return Integer.parseInt(r);
    }

    /**
     * Récupère le seuil de durée entré par l'utilisateur
     * @return Seuil de durée
     */
    public static int getThresholdDuree() {
        System.out.println("Entrez la durée maximum que vous êtes prêt à passer:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getThresholdDuree();
        }
        if (!estNombre(r)) {
            System.out.println("Vous devez entrer un nombre");
            return getThresholdDuree();
        }
        return Integer.parseInt(r);
    }

    /**
     * Récupère le seuil de pollution entré par l'utilisateur
     * @return Seuil de pollution
     */
    public static double getThresholdPollution() {
        System.out.println("Entrez la pollution maximum que vous êtes prêt à subir:");
        String r = getUserInuput();
        if (r.length() == 0) {
            System.out.println("Vous n'avez rien entré");
            return getThresholdPollution();
        }
        if (!estNombre(r)) {
            System.out.println("Vous devez entrer un nombre");
            return getThresholdPollution();
        }
        return Double.parseDouble(r);
    }

    /**
     * Applique un seuil sur les chemins
     * @param g Plateforme
     * @param chemins Chemins
     * @param critere Critère
     * @param threshold Seuil
     */
    public static void applyThreshold(Plateforme g, List<Chemin> chemins, TypeCout critere, int threshold) {
        List<Chemin> toRemove = new ArrayList<>();
        for (Chemin che : chemins) {
            double poidsByTypeCout = g.getPoidsByTypeCout(che, critere);
            if (poidsByTypeCout > threshold) {
                toRemove.add(che);

            }
        }
        chemins.removeAll(toRemove);

    }

    /**
     * Applique un seuil sur les chemins
     * @param g Plateforme
     * @param chemins Chemins
     * @param critere Critère
     * @param threshold Seuil
     */
    public static void applyThreshold(Plateforme g, List<Chemin> chemins, TypeCout critere, double threshold) {
        List<Chemin> toRemove = new ArrayList<>();
        for (Chemin che : chemins) {
            double poidsByTypeCout = g.getPoidsByTypeCout(che, critere);
            if (poidsByTypeCout > threshold) {
                toRemove.add(che);
            }
        }
        chemins.removeAll(toRemove);

    }

    /**
     * Retourne une chaîne de caractères représentant un chemin
     * @param che Chemin
     * @param critere Critère
     * @return Chaîne de caractères
     */
    public static String cheminWithCorre(Chemin che, TypeCout critere) {
        String r = "";
        // on enleve les arrete vers alpha et omega
        che.aretes().remove(0);
        che.aretes().remove(che.aretes().size()-1);

        for (Chemin cheModal : CheminImpl.splitByModalite(che)) {
            if (!r.isEmpty()) {
                r += " puis ";
            }
            r += cheModal.aretes().get(0).getModalite() + " de " +
                    cheModal.aretes().get(0).getDepart() + " à " + cheModal.aretes().get(cheModal.aretes().size()-1).getArrivee() + " ";
            if (cheModal.aretes().size() > 1) {
                r += "en passant par";
                for (int i = 1; i < cheModal.aretes().size(); i++) {
                    r += " " + cheModal.aretes().get(i).getDepart() + "";
                    if (i < cheModal.aretes().size() - 1) {
                        r += ",";
                    }
                    
                }
            }
            
        }
        return r + " totale: " + che.poids() + " " +TypeCout.getUnit(critere) ;
    }

    public static ArrayList<String> getCSV(String path) {
        try {
            Scanner scanner = new Scanner(new File(path));
            ArrayList<String> contenu = new ArrayList<String>();
            while (scanner.hasNextLine()) {
                contenu.add(scanner.nextLine());
            }
            scanner.close();
            return contenu;
        } catch (Exception e) {
            System.out.println("Fichier introuvable");
        }
        return null;
    }
}
