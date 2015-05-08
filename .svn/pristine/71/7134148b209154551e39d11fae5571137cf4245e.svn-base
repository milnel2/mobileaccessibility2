package cs.washington.mobileaccessibility.locationorienter;
import java.util.Comparator;
import java.util.Map;

public class ResultCompare implements Comparator<Map<String,String>>{

	
	public int compare(Map<String,String> a, Map<String,String> b) {
		if(a.get("relative_loc").equals(b.get("relative_loc"))){
			int distcompare;
			if(a.get("relative_loc").equals("Front"))
				distcompare = new Integer(b.get("distance")).compareTo(new Integer(a.get("distance")));
			else
				distcompare = new Integer(a.get("distance")).compareTo(new Integer(b.get("distance")));
			if(distcompare != 0){
				return distcompare;
			}else{
				return a.get("name").compareTo(b.get("name"));
			}
		}else{
			return b.get("relative_loc").compareTo(a.get("relative_loc"));
		}
	}
	
}
