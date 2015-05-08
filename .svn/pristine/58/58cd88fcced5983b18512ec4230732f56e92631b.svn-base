package firbi.base.com;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * The original version of this file was from firbi.  You can find it at
 * http://firbi.googlecode.com/svn/trunk/firbi_public/src/firbi/base/com/Bus.java
 * I don't remember whether I made any changes to this particular file.
 */

public class Bus {
	
	private GregorianCalendar mScheduledTime;
	private GregorianCalendar mPredictedTime;
	private BusRoute mRoute;
	private BusStop mStop;
	private String mDestination;
	
	public Bus(BusRoute route, BusStop stop, GregorianCalendar scheduledTime, GregorianCalendar predictedTime, String destination) {
		mRoute = route;
		mStop = stop;
		mScheduledTime = scheduledTime;
		mPredictedTime = predictedTime;
		mDestination = destination;
	}
	
	public GregorianCalendar getScheduledTime() {
		if(mScheduledTime!=null)
			return (GregorianCalendar) mScheduledTime.clone();
		else
			return null;
	}
	
	public GregorianCalendar getPredictedTime() {
		if(mPredictedTime!=null)
			return (GregorianCalendar) mPredictedTime.clone();
		else
			return getScheduledTime(); // ?!
	}
	
	public BusRoute getRoute() {
		return mRoute;
	}
	
	public BusStop getStop() {
		return mStop;
	}
	
	public String getDestination() {
		return mDestination;
	}
	
	public String toString() {
		return "Bus{route: "+getRoute().toString()+", stop: "+getStop().toString()+", predicted Time: "+((getPredictedTime()!=null)? (getPredictedTime().get(Calendar.HOUR)+":"+getPredictedTime().get(Calendar.MINUTE)+" "+getPredictedTime().get(Calendar.AM_PM)) : "null")+", scheduled Time: "+((getScheduledTime()!=null)? (getScheduledTime().get(Calendar.HOUR)+":"+getScheduledTime().get(Calendar.MINUTE)+" "+getScheduledTime().get(Calendar.AM_PM)) : "null")+"}";
	}

}
