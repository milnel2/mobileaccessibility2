package cs.washington.mobileaccessibility.color;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.app.ProgressDialog;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

/*
 * This class has all the stuff to actually process images, except for some
 * of the details of the decision tree algorithms, which are in a separate
 * DecisionTreeClassifier class.  (And, the actual algorithm to <em>generate</em>
 * the trees is in a separate program in ColorAnalyzer.java)
 * 
 * This class basically serves as a way to encapsulate all the technical details of
 * the color processing away from the main ColorNamer class.
 * 
 * This class is sort of like a singleton, except I cheated and made EVERYTHING
 * static, so there really are no instances of this class.
 * 
 */
public class ImageProcessor {

    // an optimization... so that we don't need to reallocate arrays each time
    static int [][] reds, greens, blues = null; 
    static int width, height; // of the preceding arrays
    // actually, no!  width/3-width/6 is the width of the arrays, and likewise
    // for length!!
    // TODO: correct that madness!
    
    // the data in the file converted_4096.txt, which has to be loaded in
    // via loadProbabilities
	private static int [][][][] probabilities = new int[16][16][16][11];
	
	// a flag that indicates that the process of loading the previous array
	// needs to stop because e.g. the program is ending
	private static boolean interrupted = true;
    
    // is this shared with some other classes??  Not really.
	// Unfortunately, everyone has a separate way to order the colors
    static final String [] names = {"black","blue","brown","gray","green","orange","pink","purple","red","white","yellow"};
	
    // This is true iff the probabilities array has been loaded
    public static boolean loaded = false;
    
    // This gets called when the application is closed
    public static void stopLoading() {
    	interrupted = true;
    }
    
    // this gets called when it's time to load the probabilities
    // It is called in a separate thread, so we can take all the time in the world
    // However, we need to keep tabs on the interrupted flag, to make sure that we aren't supposed
    // to be stopping
    //
    // This returns true iff we were not interrupted
    public static boolean loadProbabilities(ProgressDialog loadingDialog, InputStream is) {
    	interrupted = false;
    	int [] colors = new int[3];
        boolean anyLoadingErrors = false;
        try {
        	for(int line = 0; line < 4096; line++) {
        		if(line % 256 == 0) {
        			Log.v("ColorNamer","At line " + line + "/4096");
        			loadingDialog.setProgress(line);
        		}
        		for(int j = 0; j < 3; j++) {
        			int d = is.read();
        			colors[j] = d - 'A'; // should be between 0 and 15 inclusive
        		}
        		for(int j = 0; j < 11; j++) {
        			is.read(); // space?
        			int number = 0;
        			for(int mii = 0; mii < 6; mii++) {
        				number = 10*number + (is.read() - '0');
        			}
        			probabilities[colors[0]][colors[1]][colors[2]][j] = number;
        		}
        		is.read(); // cr
        		is.read(); // lf
        		
        		if(interrupted) {
        			is.close();
        			loaded = false;
        			return !interrupted;
        		}
        		
        	}
        	is.close();
        }
        catch(IOException ioe) {
        	Log.e("ColorNamer","Failure reading the file converted_4096.txt: " + ioe);
        	anyLoadingErrors = true;
        }
        
        loaded = !anyLoadingErrors;
        
        return !interrupted;
    }
    
    
    // Whatever is stored in the reds/greens/blues arrays, classify it, according to the
    // ad hoc decision tree
    public static String classifyPictureAdHoc() {
		int color = DecisionTreeClassifier.classifyPicture(reds, greens, blues,height/3-height/6,width/3-width/6,true);
		return DecisionTreeClassifier.getName(color);
    }
    
    // Whatever is stored in the reds/greens/blues arrays, classify it, according to the
    // machine learning decision tree
    public static String classifyPictureLearntTree() {
		int color = DecisionTreeClassifier.classifyPicture(reds, greens, blues,height/3-height/6,width/3-width/6,false);
		return DecisionTreeClassifier.getName(color);
    }
    
    // Whatever is stored in the reds/greens/blues arrays, classify it, according to the
    // EM based algorithm
    public static String classifyPictureEM() {
    	// TODO this is highly redundant with the subsequent function... why??
    	// can laziness be used here?
		double [] scores = new double[11];
		for(int i = 0; i < width/3-width/6; i++) {
			for(int j = 0; j < height/3-height/6; j++) {
				for(int k = 0; k < 11; k++) {
					scores[k] += probabilities[reds[i][j]/16][greens[i][j]/16][blues[i][j]/16][k];
				}
			}
		}
		int bestK = 0;
		double bestScore = scores[0];
		for(int k = 1; k < 11; k++) {
			if(scores[k] > bestScore) {
				bestScore = scores[k];
				bestK = k;
			}
		}
		// I feel like we're about to run the algorithm for three more iterations, on
		// top of the first one which we just completed
    	for(int iter = 0; iter < 3; iter++) {
			double [] newScores = new double[11];
			for(int i = 0; i < width/3-width/6; i++) {
				for(int j = 0; j < height/3-height/6; j++) {
					double total = 0;
					for(int k = 0; k < 11; k++) {
						total += scores[k]*probabilities[reds[i][j]/16][greens[i][j]/16][blues[i][j]/16][k];
					}
					for(int k = 0; k < 11; k++) {
						newScores[k] += scores[k]*probabilities[reds[i][j]/16][greens[i][j]/16][blues[i][j]/16][k]/total;
					}
				}
			}
			double total2 = 0;
			for(int i = 0; i < 11; i++)
				total2 += newScores[i];
			for(int i = 0; i < 11; i++)
				scores[i] = newScores[i]/total2;
		}
		bestK = 0;
		bestScore = scores[0];
		for(int k = 1; k < 11; k++) {
			if(scores[k] > bestScore) {
				bestScore = scores[k];
				bestK = k;
			}
		}

		return names[bestK];
    }
    
    // Whatever is stored in the reds/greens/blues arrays, classify it, according to the
    // EM based algorithm, but only running for one iteration
    public static String classifyPictureProbsum() {
		double [] scores = new double[11];
		for(int i = 0; i < width/3-width/6; i++) {
			for(int j = 0; j < height/3-height/6; j++) {
				for(int k = 0; k < 11; k++) {
					scores[k] += probabilities[reds[i][j]/16][greens[i][j]/16][blues[i][j]/16][k];
				}
			}
		}
		int bestK = 0;
		double bestScore = scores[0];
		for(int k = 1; k < 11; k++) {
			if(scores[k] > bestScore) {
				bestScore = scores[k];
				bestK = k;
			}
		}
		return names[bestK];
    }
    
    
    // Take whatever is stored in the static variable buffers, and
    // save the data to the file peri.txt in a peculiar format
    //
    // for use with ColorAnalyzer.txt
    public static void savePictureData(String colorName) {
    	try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("/sdcard/peri.txt",true)));
			Log.i("ColorNamer", "Storing the data for the color \"" + colorName + "\"");
			pw.println(colorName);
			pw.println("" + (width/3 - width/6) + " " + (height/3 - height/6));
			for(int i = 0; i < width/3 - width/6; i++) {
				for(int j = 0; j < height/3 - height/6; j++) {
					pw.println("" + reds[i][j] + " " + greens[i][j] + " " + blues[i][j]);
				}
			}
			pw.close(); // don't forget this!
			Log.i("ColorNamer", "Finished storing the data!");
		}
		catch(IOException ioe) {
			Log.e("ColorNamer", "Tough Luck!");
		}
    }
    
    // Calculate the percentage of pixels in the image which match the given color.
    // This must use the machine-learning tree.  I'm not even sure whether this
    // would be meaningful for the EM algorithms, and I didn't implement it for
    // my ad-hoc tree.
    public static int evaluatePicture(String colorName) {
		int matches = DecisionTreeClassifier.evaluatePicture(reds, greens, blues, height/3 - height/6, width/3 - width/6, colorName);
		matches = matches * 100 / ((height/3 - height/6)*(width/3 - width/6));
		return matches;
    }
    
    // This is the actual function which takes data straight from the camera
    // and loads it into the static variable buffers
    // There is a camera parameter so we can get the dimensions
    public static void loadRGB(byte [] data, Camera camera) {
    	Size dims = camera.getParameters().getPreviewSize();
		Log.v("ColorNamer","height = " + dims.height + " and width = " + dims.width);
		height = dims.height;
		width = dims.width;

		// now we check that all the arrays are proper lengths,
		// and reallocate if necessary
		
		if(height*width*3/2 != data.length)
			Log.e("ColorNamer","unexpected size of the data array: " + data.length);

		if(reds == null || reds.length != width/3 - width/6 || reds[0].length != height/3 - height/6) {
			reds = new int[width/3 - width/6][height/3 - height/6];
			Log.e("ColorNamer","we had to allocate a new array of reds");
		}
		if(greens == null || greens.length != width/3 - width/6 || greens[0].length != height/3 - height/6) {
			greens = new int[width/3 - width/6][height/3 - height/6];
			Log.e("ColorNamer","We had to allocate a new array of greens");
		}
		if(blues == null || blues.length != width/3 - width/6 || blues[0].length != height/3 - height/6) {
			if(blues != null)
				Log.e("ColorNamer", "We have to allocate a new array of blues because e.g. " + blues.length + "!=" + (width/3 - width/6));
			blues = new int[width/3 - width/6][height/3 - height/6];

		}

		for(int x = width/6; x < width/3; x++) {
			for(int y = height/6; y < height/3; y++) {
				int y1 = data[(2*y)*width + 2*x];
				y1 &= 0xff;
				int y2 = data[(2*y + 1)*width + 2*x];
				y2 &= 0xff;
				y1 = (y1 + y2)/2;
				int cr = data[height*width + 2*(y*(width/2) + x)];
				int cb = data[height*width + 2*(y*(width/2) + x) + 1];
				cb &= 0xff;
				cr &= 0xff;
				cb -= 128;
				cr -= 128;
				int r, g, b;
				// conversions from
				// http://blog.tomgibara.com/post/132956174/yuv420-to-rgb565-conversion-in-android
				// which agree with one of the ones on Wikipedia
				r = y1 + ((359*cr) >> 8);
				g = y1 - ((88*cb + 183*cr)>>8);
				b = y1 + ((454*cb) >> 8);

				if(r > 255)
					r = 255;
				if(r < 0)
					r = 0;
				if(g > 255)
					g = 255;
				if(g < 0)
					g = 0;
				if(b > 255)
					b = 255;
				else if (b < 0)
					b = 0;
				reds[x -width/6][y-height/6] = r;
				greens[x -width/6 ][y-height/6] = g;
				blues[x -width/6][y -height/6] = b;
			}
		}
    }
	
	
}
