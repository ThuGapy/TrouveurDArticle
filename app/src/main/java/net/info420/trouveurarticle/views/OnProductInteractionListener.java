package net.info420.trouveurarticle.views;

public interface OnProductInteractionListener {
    void TriggerEdit(int editID);
    void EditDone();
    void OpenLink(String link);
    void SeeChart(int ID);
}
