package net.info420.trouveurarticle;

public class Utils {
    public static String FormatPrice(double price) {
        String priceTotal = String.valueOf((int)Math.floor(price));
        String priceDecimalText = String.valueOf(price);

        int dotIndex = priceDecimalText.indexOf(".");
        if (dotIndex == -1) {
            return String.valueOf(price) + ".00$";
        } else {
            String decimal = priceDecimalText.substring(dotIndex + 1);
            if (decimal.length() == 0) {
                return priceTotal + ".00$";
            } else if (decimal.length() == 1) {
                return priceTotal + "." + decimal + "0$";
            } else {
                return priceTotal + "." + decimal + "$";
            }
        }
    }
}
