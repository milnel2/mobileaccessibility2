package cs.washington.edu.buddies;

import android.content.Context;
import android.content.SharedPreferences;


/* Static data management class for Pet */
public class PetData {

//	// possible bark type values
//	public static final int HIGH = 0;
//	public static final int MIDDLE = 1;
//	public static final int LOW = 2;
	
	// possible breed values;
	public static final int POMERANIAN = 1;
	public static final int RETRIEVER = 2;
	public static final int PITBULL = 3;
	
	// gender values
	public static final int MALE = 0;
	public static final int FEMALE = 1;
	
	// setting key names and default values
	private static final String OPT_NAME = "name";
	private static final String OPT_NAME_DEF = "Fido";
//	private static final String OPT_BARK = "bark";
//	private static final int OPT_BARK_DEF = MIDDLE;
	private static final String OPT_GENDER = "gender";
	private static final int OPT_GENDER_DEF = MALE;
	private static final String OPT_BREED = "breed";
	private static final int OPT_BREED_DEF = RETRIEVER;
	private static final String OPT_FIRST = "first";
	
	// Preference file name that holds persistent data
	public static final String PETDATA_FILENM = "BBPetData";

	/* Used to create pet for the first time */
	public static boolean getFirstTime(Context context) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		boolean first = pet.getBoolean(OPT_FIRST, true);
		return first;
	}
	
	/* Retrieves pet's name */
	public static String getName(Context context) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		return pet.getString(OPT_NAME, OPT_NAME_DEF);
	}
	
	/* Retrieves whether pet is male */
	public static int getGender(Context context) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		return pet.getInt(OPT_GENDER, OPT_GENDER_DEF);
	}

//	/* Retrieves pet's bark type */
//	public static int getBark(Context context) {
//		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
//		return pet.getInt(OPT_BARK, OPT_BARK_DEF);
//	}

	/* Retrieves pet's breed type */
	public static int getBreed(Context context) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		return pet.getInt(OPT_BREED, OPT_BREED_DEF);
	}
	
//	/* Saves pet's bark type */
//	protected static boolean setBark(Context context, int bark) {
//		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
//		SharedPreferences.Editor ed = pet.edit();
//		boolean result = false;
//		switch (bark) {
//		case HIGH:
//		case MIDDLE:
//		case LOW:
//			ed.putInt(OPT_BARK, bark);
//			result = ed.commit();
//		}
//		return result;
//	}
	
	/* Saves pet's breed */
	protected static boolean setBreed(Context context, int breed) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		SharedPreferences.Editor ed = pet.edit();
		boolean result = false;
		switch (breed) {
		case POMERANIAN:
		case RETRIEVER:
		case PITBULL:
			ed.putInt(OPT_BREED, breed);
			result = ed.commit();
		}
		return result;
	}
	
	/* Saves whether pet is male */
	protected static boolean setGender(Context context, int gender) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		SharedPreferences.Editor ed = pet.edit();
		boolean result = false;
		switch (gender) {
		case MALE:
		case FEMALE:
			ed.putInt(OPT_GENDER, gender);
			result = ed.commit();
		}
		return result;
	}
	
	/* Saves pet's name */
	protected static boolean setName(Context context, String name) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
		SharedPreferences.Editor ed = pet.edit();
		boolean result = false;
		ed.putString(OPT_NAME, name);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setFirstTime(Context context, boolean first) {
		SharedPreferences pet = context.getSharedPreferences(PETDATA_FILENM, 0);
	 	SharedPreferences.Editor ed = pet.edit();
	 	boolean result = false;
		ed.putBoolean(OPT_FIRST, false);
		result = ed.commit();
		return result;
	}
	
}