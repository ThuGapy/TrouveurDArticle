package net.info420.trouveurarticle.database;

/**
 * Enum qui contient les status de disponibilité des produits
 */
public enum PriceStatus {
    /**
     * PriceStatus, Si le produit est disponible au prix désiré
     */
    GOOD,
    /**
     * PriceStatus, Si le produit est disponible à un prix plus cher qu'au prix désiré
     */
    OVERPRICED,
    /**
     * PriceStatus, Si l'article est hors de stock
     */
    OOS,
    /**
     * PriceStatus, Aucune information sur le produit
     */
    NO_DATA
}
