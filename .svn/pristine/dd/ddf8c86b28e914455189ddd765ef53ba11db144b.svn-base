package cs.washington.mobileaccessibility.color;

import android.util.Log;

/*
 * This class consolidates all the decision-tree based algorithms,
 * which used to be in separate classes
 * 
 * The very first algorithm I wrote had a bunch of color on top of the basic 11
 * So the ad-hoc algorithm can give things like chartreuse, teal, magenta dye, etc.
 * It tries not to make this change the overall color, however
 * 
 * This 'class' is kind of pseudo-singleton.  I mean, I just made all the state static,
 * rather than having a single instance, which is all I would need anyways.
 */
public class DecisionTreeClassifier {
	
	// notice how the numbers are assigned!
	// dropping the ones' digit converts to
	// the canonical colors
	public static final int WHITE = 0;
	public static final int GRAY = 10;
	public static final int BLACK = 20;
	public static final int RED = 30;
	public static final int MAROON = 31;
	public static final int GREEN = 40;
	public static final int OLIVE = 41;
	public static final int CHARTREUSE = 42;
	public static final int EMERALD = 43;
	public static final int SPRING_GREEN = 44;
	public static final int BLUE = 50;
	public static final int NAVY_BLUE = 51;
	public static final int CYAN = 52;
	public static final int TEAL = 53;
	public static final int YELLOW = 60;
	public static final int TRADITIONAL_CHARTREUSE = 61;
	public static final int BEIGE = 62;
	public static final int ORANGE = 70;
	public static final int BROWN = 100;
	public static final int PURPLE = 80;
	public static final int MAGENTA = 81;
	public static final int VIOLET = 82;
	public static final int MAGENTA_DYE = 83;
	public static final int PINK = 90;
	public static final int ROSE = 91;
	
	public static String getName(int color) {
		switch(color) {
		case WHITE:
			return "white";
		case GRAY:
			return "gray";
		case BLACK:
			return "black";
		case RED:
			return "red";
		case MAROON:
			return "maroon";
		case GREEN:
			return "green";
		case OLIVE:
			return "olive";
		case CHARTREUSE:
			return "chartreuse";
		case EMERALD:
			return "emerald";
		case SPRING_GREEN:
			return "spring green";
		case BLUE:
			return "blue";
		case NAVY_BLUE:
			return "navy blue";
		case CYAN:
			return "cyan";
		case TEAL:
			return "teal";
		case YELLOW:
			return "yellow";
		case TRADITIONAL_CHARTREUSE:
			return "traditional chartreuse";
		case BEIGE:
			return "beige";
		case ORANGE:
			return "orange";
		case BROWN:
			return "brown";
		case PURPLE:
			return "purple";
		case MAGENTA:
			return "magenta";
		case VIOLET:
			return "violet";
		case MAGENTA_DYE:
			return "magenta_dye";
		case PINK:
			return "pink";
		case ROSE:
			return "rose";
		default:
			return "unknown";
			
		}
	}
	
	// there is a distinction between the fine grain colors,
	// and the 11 coarse ones.  These arrays are probably static
	// to prevent frequent reallocations.
	// fineGrainCounts clearly has way more entries than is necessary.
	private static int [] fineGrainCounts = new int[110];
	private static int [] coarseCounts = new int[11];
	
	
	private static void clearCounts() {
		fineGrainCounts = new int[110];
		coarseCounts = new int[11];
	}
	
	// taken from Wikipedia
	private static void calculateHSV(int red, int green, int blue, int [] hsv) {

		int [] rgb = new int[3];
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
		int max = Math.max(rgb[0],rgb[1]);
		max = Math.max(max,rgb[2]);
		int min = Math.min(rgb[0],rgb[1]);
		min = Math.min(min,rgb[2]);
		if(max == min)
			hsv[0] = 0;
		else if(max == rgb[0])
			hsv[0] = 60*(rgb[1] - rgb[2])/(max - min) + 360;
		else if(max == rgb[1])
			hsv[0] = 60*(rgb[2] - rgb[0])/(max - min) + 120;
		else
			hsv[0] = 60*(rgb[0] - rgb[1])/(max - min) + 240;
		hsv[0] %= 360;
		if(max == 0)
			hsv[1] = 0;
		else
			hsv[1] = 100*(max - min)/max;
		hsv[2] = max*100/256;
	}

	// this calculates something proportional to the magnitude squared
	// of the UV component.  I was looking for something that would measure
	// the extent to which the color was chromatic, as opposed to white/black/gray,
	// for all of which this function yields zero, ideally
	private static double calculateUV(int red, int green, int blue) {
		int [] rgb = new int[3];
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
		double total = 0;
		double totalsq = 0;
		for(int i = 0; i < 3; i++) {
			double x = rgb[i];
			total += x;
			totalsq += x*x;
		}
		total/=3;
		totalsq/=3;
		totalsq -= total*total;
		return totalsq;
	}

	// classify a single pixel
	// if adHoc is true, this uses the arbitrary algorithm I made by hand
	// if not, it uses one that I got by applying a machine-learning algorithm
	// (the one written in the ColorAnalyzer class)
	private static int classifyPixel(int red, int green, int blue, boolean adHoc) {
		int [] hsv = new int[3];
		calculateHSV(red,green,blue,hsv);
		int hue = hsv[0];
		int sat = hsv[1];
		int val = hsv[2];
		int chrome = (int) calculateUV(red, green, blue);
		int cohue = (hue + 180)%360;

		if(adHoc) {
			if(chrome < 100) {
				if(val > 75)
					return WHITE;
				if(val < 26)
					return BLACK;
				return GRAY;
			}

			if(hue > 160 && hue <= 210) {
				if(val > 60)
					return CYAN;
				else
					return TEAL;
			}
			if(hue > 210 && hue <= 260) {
				if(val > 60)
					return BLUE;
				else
					return NAVY_BLUE;
			}
			if((hue > 300 || hue <= 15) && sat < 60 && val > 60)
				return PINK;
			if(hue > 260 && hue <=347) {
				if(val > 75) {
					if(hue >= 330)
						return ROSE;
					else
						return MAGENTA;
				}
				if(hue < 285)
					return VIOLET;
				if(hue > 315)
					return MAGENTA_DYE;
				return PURPLE;
			}
			if(hue > 15 && hue <= 45 && val < 70 || sat < 50) {
				return BROWN;
			}
			if(hue > 347 || hue <= 15) {
				if(val > 60)
					return RED;
				else
					return MAROON;
			}
			if(hue > 15 && hue <= 45) {
				return ORANGE;
			}
			if(hue > 45 && hue <= 80 && val <= 70)
				return OLIVE;
			if(hue > 45 && hue <= 80) {
				if(sat < 40 && hue <= 67)
					return BEIGE;
				if(hue > 67)
					return TRADITIONAL_CHARTREUSE;
				return YELLOW;
			}
			if(hue > 80 && hue <= 100) {
				return CHARTREUSE;
			}
			if(hue > 100 && hue <= 160) {
				if(hue >= 140 && val > 75) {
					if(sat < 70)
						return EMERALD;
					else
						return SPRING_GREEN;
				}
				return GREEN;
			}
			return -1;
		}

		else {
			if(cohue > 210) {
				if(chrome > 622) {
					if(chrome > 1861) {
						if(hue > 34)
							return YELLOW;
						else
							return BROWN;
					}
					else {
						if(hue > 39)
							return GREEN;
						else
							return BROWN;
					}
				}
				else {
					if(val > 20){
						if(val > 51)
							return WHITE;
						else
							return GRAY;
					}
					else
						return BLACK;
				}
			}
			else {
				if(cohue > 180) {
					if(hue > 23) {
						if(chrome > 2648)
							return ORANGE;
						else
							return BROWN;
					}
					else {
						if(sat > 74)
							return RED;
						else
							return PINK;
					}
				}
				else {
					if(blue > 71) {
						if(blue > 183)
							return PURPLE;
						else
							return BLUE;
					}
					else {
						if(hue > 199)
							return PURPLE;
						else
							return GRAY;
					}
				}
			}
		}
	}
	
	
	// To classify a picture, we classify each pixel, and then choose the most popular
	// coarse color (one of the 11)
	// If there are different variants of this color (like spring green vs chartreuse), we choose
	// the most popular of those, but only if the subvariant accounts for more than 25% of the image.
	// if not, we just stick to the coarse color
	// If we aren't using the ad hoc algorithm, only the coarse colors occur anyways
	public static int classifyPicture(int [][] red, int [][] green, int [][] blue, int height, int width, boolean adHoc) {
		clearCounts();
		int bestCount = 0;
		int bestColor = -1;
		for(int scale = 1; scale < 2; scale*=2) {
			for(int x = 0; x < width; x+=scale) {
				for(int y = 0; y < height; y+=scale) {
					int color = classifyPixel(red[x][y],green[x][y],blue[x][y],adHoc);
					fineGrainCounts[color]++;
					color /= 10;
					int i = ++coarseCounts[color];
					if(i > bestCount) {
						bestColor = color;
						bestCount = i;
					}


				}
			}
		}
		if(adHoc) {
			// now, let's find the best detailed version of things
			int bestSubColor = bestColor*10;
			int bestSubCount = fineGrainCounts[bestSubColor];
			for(int i = 0; i < 10; i++) {
				if(fineGrainCounts[i + bestColor*10] > bestSubCount) {
					bestSubColor = i + bestColor*10;
					bestSubCount = fineGrainCounts[i + bestColor*10];
				}
			}
			if(bestSubCount > height*width/4) {
				Log.i("Color Classifier","It turned out that the best color was " + bestSubColor + " which occurred " + bestSubCount + " times out of " + height*width);
				return bestSubColor;
			}
		}
		Log.i("Color Classifier","It turned out that the best coarse color was " + bestColor + " which occurred " + bestCount + " times out of " + height*width + " pixels");
		return bestColor*10;
	}

	
	
	// This takes a picture and a color, and returns the number of pixels which have the given
	// color.
	// It's used for the mode where the user is trying to find something of a given color
	public static int evaluatePicture(int [][] red, int [][] green, int [][] blue, int height, int width, String colorName) {
		int d;
		for(d = 0; d < 110; d+=10)
			if(getName(d).equals(colorName))
				break;
		int count = 0;
		for(int scale = 1; scale < 2; scale*=2) {
			for(int x = 0; x < width; x+= scale) {
				for(int y = 0; y < height; y+= scale) {
					int color = classifyPixel(red[x][y], green[x][y],blue[x][y],false);
					if(color == d)
						count++;
				}
			}
		}
		return count;
	}
	

}
