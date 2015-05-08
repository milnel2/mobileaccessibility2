package cs.washington.edu.buddies;

import java.util.Random;



import android.content.Intent;

/* Maintains the current status of the animal */
public class Animal {

	// Status flags
	private boolean needsFood;
	private boolean needsPlay;
	private boolean needsGrooming; 
//	private boolean needsSleep;
//	private boolean needsVet;
//
//	private boolean needsFriend;
	
	// Factor in milliseconds to calculate new times
	private static final int FEED_FACTOR = 50000;
	private static final int PLAY_FACTOR = 15500;
	private static final int GROOM_INTERVAL = 1000;
	// For future release
//	private static final int VET_INTERVAL = 2000;
//	private static final int INTERACTION_FACTOR = 5000;
	
	// Time when animal next needs something
	private long nextFeedTime;
	private long nextPlayTime;
	private long nextGroomingTime;
	
	// For future release
//	private long nextVetTime;
//	private long nextFriendTime;
	
	private Random random;
		
	
	public Animal () {
		this("Unnamed", true);
	}
	
	public Animal (String name, boolean male) {
		random = new Random();
		needsFood = true;
		play(50);
		groom();
		// For future release
//		seeVet();
//		interactWithFriend(25);
	}
	
	protected synchronized boolean checkStatus() {
		boolean notify = false;
		if (!needsFood && System.currentTimeMillis() > nextFeedTime) {
			needsFood = true;
			notify = true;
		}
		if (!needsPlay && System.currentTimeMillis() > nextPlayTime) {
			needsPlay = true;
			notify = true;
		}
		if (!needsGrooming && System.currentTimeMillis() > nextGroomingTime) {
			needsGrooming = true;
			notify = true;
		}
//		if (!needsVet && System.currentTimeMillis() > nextVetTime) {
//			needsVet = true;
//			notify = true;
//		}
//		if (!needsFriend && System.currentTimeMillis() > nextFriendTime) {
//			needsFriend = true;
//			notify = true;
//		}
		return notify;
	}
	
	protected synchronized void feed(int amount) { 
		needsFood = false;
		nextFeedTime = System.currentTimeMillis() + (FEED_FACTOR * (random.nextInt(100) + amount));
	}
	
	protected synchronized void play(int amount) { 
		needsPlay = false;
		nextPlayTime = System.currentTimeMillis() + (PLAY_FACTOR * (random.nextInt(100) + amount));		
	}
	
	protected synchronized void groom() { 
		nextGroomingTime = System.currentTimeMillis() + ((10 + random.nextInt(100)) * GROOM_INTERVAL);
		needsGrooming = false;
	}
	
	// For future release
//	protected synchronized void seeVet() { 
//		nextVetTime = System.currentTimeMillis() + ((10 + random.nextInt(100)) * VET_INTERVAL);
//		needsVet = false;
//	}
	
	// For future release
//	protected synchronized void interactWithFriend(int amount) { 
//		nextFriendTime = System.currentTimeMillis() + (INTERACTION_FACTOR * amount);
//		needsFriend = false;
//	}
	
	protected boolean getFeedStatus() {
		return needsFood;
	}
	
	protected boolean getPlayStatus() {
		return needsPlay;
	}
	
	protected boolean getGroomingStatus() {
		return needsGrooming;
	}
	
	
	// For future release
//	protected boolean getVetStatus() {
//		return needsVet;
//	}
	
	// For future release
//	protected boolean getInteractionStatus() {
//		return needsFriend;
//	}
	
	protected void speak() {
		// Override in derived class
	}
	
	
}

