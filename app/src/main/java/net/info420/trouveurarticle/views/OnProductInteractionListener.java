package net.info420.trouveurarticle.views;

// Interface qui gère les interactions avec les produits
public interface OnProductInteractionListener {
    void TriggerEdit(int editID);
    void EditDone();
    void OpenLink(String link);
    void SeeChart(int ID);
}
