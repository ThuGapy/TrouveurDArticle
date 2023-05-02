package net.info420.trouveurarticle.views;

/**
 * Interface qui gère quand le rafraichissement des données est demandé
 */
public interface OnRefreshRequestedListener {
    /**
     * Méthode qui rafraichi les données
     */
    void RequestRefresh();
}
