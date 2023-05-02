package net.info420.trouveurarticle.views;

/**
 * Interface qui gère les interactions avec les produits
 */
public interface OnProductInteractionListener {
    /**
     * Méthode qui lance la modification d'un produit
     * @param editID ID du produit
     */
    void TriggerEdit(int editID);

    /**
     * Méthode qui confirme la fin de la modification d'un produit
     */
    void EditDone();

    /**
     * Méthode qui ouvre un lien relié à un produit
     * @param link Lien à ouvrir
     */
    void OpenLink(String link);

    /**
     * Méthode qui ouvre une fenêtre pour voir les données d'un produit
     * @param ID ID du produit
     */
    void SeeChart(int ID);
}
