package kim.biryeong.perfume.layering.model;

import java.util.Map;

public record RoleVector(
    double topLift,
    double heartBridge,
    double baseAnchor,
    double sweetness,
    double freshness,
    double darkness) {

  public static RoleVector from(Map<String, Double> weights) {
    double citrus = weight(weights, "Citrus");
    double fresh = weight(weights, "Fresh");
    double green = weight(weights, "Green");
    double fruity = weight(weights, "Fruity");
    double aromatic = weight(weights, "Aromatic");
    double floral = weight(weights, "Floral");
    double spicy = weight(weights, "Spicy");
    double sweet = weight(weights, "Sweet");
    double woody = weight(weights, "Woody");
    double musky = weight(weights, "Musky");
    double earthy = weight(weights, "Earthy/Smoky");
    double gourmand = weight(weights, "Gourmand");
    return new RoleVector(
        citrus + fresh + green + fruity + 0.5 * aromatic,
        floral + aromatic + spicy + 0.5 * sweet,
        woody + musky + earthy + gourmand + 0.5 * sweet,
        sweet + gourmand + 0.5 * fruity,
        citrus + fresh + green + aromatic,
        earthy + woody + spicy);
  }

  private static double weight(Map<String, Double> weights, String accord) {
    return weights.getOrDefault(accord, 0.0);
  }
}
