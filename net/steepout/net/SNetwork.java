package net.steepout.net;

import net.steepout.net.unicast.CommonSocket;
import net.steepout.net.unicast.UnicastSocket;

public class SNetwork {
	static {

	}
	
	public UnicastSocket createUnicast(){
		return new CommonSocket();
	}
}
