package com.kylantraynor.mangostructures;

public class Utils {
	public static double get2DDistanceSquared(double x1, double y1, double x2, double y2){
		double dx = x2 - x1;
		double dy = y2 - y1;
		return dx*dx + dy*dy;
	}
}
