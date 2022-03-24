package org.matsim.core.router.util;

public class TouristChoiceCoefficients {
    public static boolean landmarkVisibility = true;
    public static boolean euclideanDistanceAttenuation = true;

    public static double ASC_Straight = 0d;
    public static double ASC_Left = -1.44;
    public static double ASC_Right = -1.41;
    public static double ASC_Back = -2.44;
    public static double widthCoeficient = 0.2;
    public static double beta_D = landmarkVisibility ? -2.44 / Math.PI : 0d;



}
